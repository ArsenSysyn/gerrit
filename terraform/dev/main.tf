 terraform {
   backend "s3" {
     bucket         = "mybucketforterraformstatearsen"
     key            = "first-test-2/terraform.tfstate"
     region         = "us-east-1"
     dynamodb_table = "dynamodb-lock"
     encrypt        = true
   }
}
#------------DEFAULT VPC------------#
resource "aws_default_vpc" "default" {}

resource "aws_default_subnet" "az1" {
  availability_zone = "us-east-1a"
}
#------------DEFAULT VPC------------#
module "appsg" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.8.0"
  name = "AppSG"
  vpc_id = aws_default_vpc.default.id
  ingress_with_cidr_blocks = [
    {
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      description = "User-service ports"
      cidr_blocks = "0.0.0.0/0"
    },
    {
      from_port   = 29418
      to_port     = 29418
      protocol    = "tcp"
      description = "User-service ports"
      cidr_blocks = "0.0.0.0/0"
    },
    {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      description = "SSH-to-server"
      cidr_blocks = "0.0.0.0/0"
    }
  ]
  egress_rules = ["all-all"]
}

module "cachesg" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "4.8.0"
  name = "CacheSG"
  vpc_id = aws_default_vpc.default.id
  ingress_with_cidr_blocks = [
    {
      from_port   = 9090
      to_port     = 9090
      protocol    = "tcp"
      description = "Cache ports"
      cidr_blocks = join(",",data.aws_ip_ranges.codebuild.cidr_blocks)
     }
  ]
  egress_rules = ["all-all"]
}

// own module
module "cache_efs" {
  // creates EFS and mount target for it
  source = "../modules/aws_efs"
  vpc_id = aws_default_vpc.default.id
  subnets = [aws_default_subnet.az1.id]
  name = "Cache_EFS"
  encrypted = true
}

// own module
module "appdata_efs" {
  // creates EFS and mount target for it
  source = "../modules/aws_efs"
  vpc_id = aws_default_vpc.default.id
  subnets = [aws_default_subnet.az1.id]
  name = "Appdata_EFS"
  encrypted = true
}

module "app_ecs_cluster" {
  source = "trussworks/ecs-cluster/aws"

  name        = var.app_ecs_cluster_name
  environment = var.app_ecs_cluster_environment_name

  image_id      = data.aws_ami.ecs_ami.image_id
  instance_type = var.app_ecs_cluster_instance_type

  vpc_id           = aws_default_vpc.default.id
  subnet_ids       = [aws_default_subnet.az1.id]
  desired_capacity = 1
  max_size         = 1
  min_size         = 1
  security_group_ids = [module.cachesg.security_group_id, module.cache_efs.ec2_security_group_id]
}

# #----------------ECS CACHE SYSTEM----------------#
resource "aws_ecs_task_definition" "CacheTaskDF" {
  family        = "bazelcache"

  container_definitions = file("task-definitions/service.json")
  volume {
    name      = "efs-gerrit"
    efs_volume_configuration {
      file_system_id = module.cache_efs.file_system_id
      root_directory = "/"
    }
  }
}

module "ecs-service" {
  source  = "mergermarket/load-balanced-ecs-service-no-target-group/acuris"
  version = "2.2.4"
  
  name = "CacheBazel"
  task_definition = aws_ecs_task_definition.CacheTaskDF.arn
  cluster = module.app_ecs_cluster.ecs_cluster_name
  container_name = "cachebazel"
  container_port = 9090
  desired_count = "1"
  depends_on = [
    module.cache_efs
  ]
}

module "elastic-beanstalk-application" {
  source  = "cloudposse/elastic-beanstalk-application/aws"
  version = "0.11.1"
  name = "gerritapp"
}

// own module
module "elastic-beanstalk-environment" {
  source  = "../modules/aws_beanstalk_environment"
  app_name = module.elastic-beanstalk-application.elastic_beanstalk_application_name
  env_name = "${module.elastic-beanstalk-application.elastic_beanstalk_application_name}env"
  solution_stack = "64bit Amazon Linux 2 v3.2.11 running Corretto 11"
  vpc_id = aws_default_vpc.default.id
  subnets_ids = [aws_default_subnet.az1.id]
  security_group_ids = [module.appsg.security_group_id,module.appdata_efs.ec2_security_group_id]
  env_vars = {
    FILE_SYSTEM_ID = module.appdata_efs.file_system_id
  }
}
