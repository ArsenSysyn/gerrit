variable "timeout" {
  description = "Deploy timeout" 
  default     = "20m"
}

variable "instance_type" {
  description = "Specify instance tyoe"
  default = "t2.micro"
}

variable "vpc_id" {
  description = "Specify VPC ID"
  
}
variable "env_settings" {
  type        = list
  description = "A list of 3 item lists in the format env_settings = [ [namespace, name, value] ]"
  default     = [ 
    [ "aws:elasticbeanstalk:application:environment", "BUNDLE_WITHOUT", "test:development"]
  ]
}
variable "security_group_ids" {
  type = list
  description = "Specify security group IDs"
}

variable "subnets_ids" {
  type = list
  description = "Specify subnets_ids"  
}

variable "app_name" { 
  description = "The name of the application to which to attach the environment"
}

variable "env_name" {
  description = "Name for elastic beanstalk environment"
}

variable "solution_stack" {
  description = "Elastic beanstalk environment solution stack"
}

variable "env_vars" {
  type        = map(string)
  default     = {}
  description = "Map of custom ENV variables to be provided to the application running on Elastic Beanstalk, e.g. env_vars = { DB_USER = 'admin' DB_PASS = 'xxxxxx' }"
}

variable "tags" {
  description = "A mapping of tags to apply to resources"
  type = map(string)
  default     = {}
}