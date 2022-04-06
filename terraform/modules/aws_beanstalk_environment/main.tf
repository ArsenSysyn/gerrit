resource "aws_elastic_beanstalk_environment" "default" {
  name                    = var.env_name
  application             = var.app_name
  solution_stack_name     = var.solution_stack
  wait_for_ready_timeout  = var.timeout
  tags = var.tags
### ENVIRONMENT SETTINGS  ###
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name = "IamInstanceProfile"
    value = "aws-elasticbeanstalk-ec2-role"
    resource = ""
  }

  setting {
    namespace = "aws:ec2:vpc"
    name = "VPCId"
    value = join(",",sort(var.vpc_ids))
    resource = ""
   }
  // Subnets ids
  setting {
    namespace = "aws:ec2:vpc"
    name      = "Subnets"
    value     = join(",",sort(var.subnets_ids))
    resource = ""
   }
  // Security group ids
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "SecurityGroups"
    value     = join(",",sort(var.security_group_ids))
    resource = ""
    }
  // app default port
  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "PORT"
    value     = var.port
    resource = ""
    }
  // Environment vars
  dynamic "setting" {
    for_each = var.env_vars
    content {
      namespace = "aws:elasticbeanstalk:application:environment"
      name      = setting.key
      value     = setting.value
      resource = ""
    }
  }

  // additional settings
  dynamic setting {
    for_each = var.env_settings
    content {
      namespace = setting.value[0]
      name      = setting.value[1]
      value     = setting.value[2]
      resource = ""
    }
  }
  lifecycle {
    ignore_changes = [tags]
  }
}