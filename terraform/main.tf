 terraform {
   backend "s3" {
#     bucket         = var.bucketstate
     key            = "first-test-1/terraform.tfstate"
     region         = "us-east-1"
#     dynamodb_table = var.dynamodbstate
     encrypt        = true
   }
}
#------------DEFAULT VPC------------#
resource "aws_default_vpc" "default" {}

resource "aws_default_subnet" "az1" {
  availability_zone = "us-east-1a"
}

#------------DEFAULT VPC------------#

#-----------------SECURITY GROUPS FOR APP AND CACHE SERVER-----------------#
resource "aws_security_group" "app" {
  name        = "Allow public access to application"
  description = "Allow public to access to application with port 8080,29418"
  vpc_id = aws_default_vpc.default.id 
  dynamic "ingress" {
      for_each = ["8080","29418","22"]
      content {
          from_port        = ingress.value
          to_port          = ingress.value
          protocol         = "tcp"
          cidr_blocks      = ["0.0.0.0/0"]
      }
  }
  
  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  tags = {
    Name = "AppSG"
  }
}

resource "aws_security_group" "cache" {
  name        = "Allow access to using cache"
  description = "Allow access to using cache"
  vpc_id = aws_default_vpc.default.id

  ingress {
    from_port        = 9090
    to_port          = 9090
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  tags = {
    Name = "CacheSG"
  }
}
#-----------------SECURITY GROUPS-----------------#

#----------------ECS CACHE SYSTEM----------------#

#------ROLES FOR ECS CLUSTER------#

resource "aws_iam_role" "ecs_agent" {
  name               = "ecs_agent"
  assume_role_policy = data.aws_iam_policy_document.ecs_agent.json
}

resource "aws_iam_role_policy_attachment" "ecs_agent" {
  role       = aws_iam_role.ecs_agent.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource "aws_iam_instance_profile" "ecs_agent" {
  name = "ecs-agent"
  role = aws_iam_role.ecs_agent.name
}
#------ROLES FOR ECS CLUSTER------#

#------EFS FOR CACHE------#
resource "aws_efs_file_system" "cache" {
  tags = {
    Name = "CacheBazel"
  }
}

resource "aws_efs_mount_target" "mount" {
  file_system_id = aws_efs_file_system.cache.id
  subnet_id      = aws_default_subnet.az1.id
}
# #------EFS FOR CACHE------#

# #------ECS FOR CACHE------#
resource "aws_ecs_cluster" "cache" {
  name = "CacheCluster"
}

resource "aws_ecs_task_definition" "CacheTaskDF" {
  family        = "cachebazel"

  container_definitions = file("task-definitions/service.json")
  volume {
    name      = "efs-gerrit"
    efs_volume_configuration {
      file_system_id = aws_efs_file_system.cache.id
      root_directory = "/"
    }
  }
}

resource "aws_ecs_service" "cache" {
  name            = "cache"
  cluster         = aws_ecs_cluster.cache.id
  task_definition = aws_ecs_task_definition.CacheTaskDF.arn
  desired_count   = 1
}
#------ECS FOR CACHE------#

#------ASG FOR ECS------#
resource "aws_launch_configuration" "ecs_launch_config" {
    image_id             = "ami-0a5e7c9183d1cea27"
    iam_instance_profile = aws_iam_instance_profile.ecs_agent.name
    security_groups      = [aws_security_group.cache.id]
    user_data            = "#!/bin/bash\necho ECS_CLUSTER=${aws_ecs_cluster.cache.name} >> /etc/ecs/ecs.config"
    instance_type        = "t2.micro"
}

resource "aws_autoscaling_group" "asg_ecs" {
    name                      = "asg"
    vpc_zone_identifier       = [aws_default_subnet.az1.id]
    launch_configuration      = aws_launch_configuration.ecs_launch_config.name

    desired_capacity          = 1
    min_size                  = 1
    max_size                  = 1
    health_check_grace_period = 300
    health_check_type         = "EC2"
    tag {
      key = "Name"
      value = "CacheContainer"
      propagate_at_launch = true
    }
}

#------ASG FOR ECS------#

#----------------ECS CACHE SYSTEM----------------#
#-----EFS FOR APP DATA-----#
resource "aws_efs_file_system" "appdata" {
  tags = {
    Name = "Appdata"
  }
}
#-----EFS FOR APP DATA-----#
#--------------------BEANSTALK ENV----------------------#
resource "aws_elastic_beanstalk_application" "GerritApp" {
  name        = "GerritApp"
  description = "GerritApp"
}

resource "aws_elastic_beanstalk_environment" "GerritApp-env" {
  name                = "GerritApp-env"
  application         = aws_elastic_beanstalk_application.GerritApp.name
  solution_stack_name = "64bit Amazon Linux 2 v3.2.11 running Corretto 11"
   
  setting {
    namespace = "aws:ec2:vpc"
    name = "VPCId"
    value = aws_default_vpc.default.id
  }

  setting {
    namespace = "aws:ec2:vpc"
    name = "Subnets"
    value = aws_default_subnet.az1.id
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "IamInstanceProfile"
    value = "aws-elasticbeanstalk-ec2-role"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "InstanceType"
    value = "t2.micro"
  }
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "SecurityGroups"
    value = aws_security_group.app.id
  }
}

resource "aws_eip" "gerrit-app" {
  instance = data.aws_instance.test.id
  vpc      = true
  depends_on = [aws_elastic_beanstalk_environment.GerritApp-env]
  tags = {
    Name = "EIP for GerritApp-env"
  }
}
#--------------------BEANSTALK ENV----------------------#
