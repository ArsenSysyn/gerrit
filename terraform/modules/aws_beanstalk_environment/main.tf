resource "aws_elastic_beanstalk_environment" "default" {
  name                    = var.env_name
  application             = var.app_name
  solution_stack_name     = var.solution_stack
  wait_for_ready_timeout  = var.timeout
### ENVIRONMENT SETTINGS  ###
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "IamInstanceProfile"
    value = "aws-elasticbeanstalk-ec2-role"
  }

  setting {
    namespace = "aws:ec2:vpc"
    name = "VPCId"
    value = var.vpc_id
   }
  // Subnets ids
  dynamic setting {
   for_each = var.subnets_ids
    content {
      namespace = "aws:ec2:vpc"
      name      = "Subnets"
      value     = setting.value
   }
  }
  // Security group ids
  dynamic setting {
   for_each = var.security_group_ids
    content {
      namespace = "aws:autoscaling:launchconfiguration"
      name      = "SecurityGroups"
      value     = setting.value
    }
   }
  // Environment vars
  dynamic "setting" {
    for_each = var.env_vars
    content {
      namespace = "aws:elasticbeanstalk:application:environment"
      name      = setting.key
      value     = setting.value
    }
  }

  // additional settings
  dynamic setting {
    for_each = var.env_settings
    content {
      namespace = setting.value[0]
      name      = setting.value[1]
      value     = setting.value[2]
    }
  }
  tags = merge(
    tomap({"Name" = var.env_name}),
    var.tags,
  )
}

data "aws_instance" "this" {
  instance_tags = {
    "Name" = aws_elastic_beanstalk_environment.default.name
  }
  depends_on = [aws_elastic_beanstalk_environment.default]
}

// attach EIP to beanstalk instance
resource "aws_eip" "this" {
  instance = data.aws_instance.this.id
  vpc      = true
  depends_on = [aws_elastic_beanstalk_environment.default]
  tags = {
    Name = "EIP for ${var.env_name}"
  }
}