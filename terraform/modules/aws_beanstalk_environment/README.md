# Module for creating Elastic Beanstalk environment in AWS

This module is responsbile for creating a new environment in the Elastic Beanstalk applications with big count of options using input `env_settings`

Options for beanstalk environments are documented here: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/command-options-general.html

## Usage:

```
module "elastic-beanstalk-environment" {
  source  = "./modules/aws_beanstalk_environment"
  app_name = module.elastic-beanstalk-application.elastic_beanstalk_application_name
  env_name = "${module.elastic-beanstalk-application.elastic_beanstalk_application_name}env"
  solution_stack = "64bit Amazon Linux 2 v3.2.11 running Corretto 11"
  vpc_ids = [aws_default_vpc.default.id]
  subnets_ids = [aws_default_subnet.az1.id]
  security_group_ids = [module.appsg.security_group_id,module.appdata_efs.ec2_security_group_id]
  port = 8080
  env_vars = {
    FILE_SYSTEM_ID = module.appdata_efs.file_system_id
  }
}
```

---

## Required inputs

__app_name__(string)

Description: specify application name where module create environment

__env_name__(string)

Description: specify environment name

__solution_stack__(string)

Description: specify solution stack name for your environment

__vpc_ids__(list)

Description: specify VPC ids for your environment

__subnets_ids__(list)

Description: specify subnets ids for your environment

__secutiry_group_ids__(list)

Description: specify security group ids for your environment

---

## Optional inputs

__timeout__(string)

Description: specify timeout of creating environment

default: `"20m"`

__port__(integer)

Description: specify port which your application use

default: `5000`


__env_vars__(map(string))

Description: specify environment variables

default: '{}'


__env_settings__(list(string))

Description: specify some additional settings using

default: '[ "aws:elasticbeanstalk:application:environment", "BUNDLE_WITHOUT", "test:development"]'


__instance_type__(string)

Description: specify instance type of environment

default: 't2.micro'


__tags__(map(string))

Description: specify tags

default: ```{
     "Provided_by" = "Terraform" 
}```

---

## Outputs

__env_name__

Description: environment name

__url__

Description: URL of your environment
