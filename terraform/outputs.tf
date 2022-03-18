output "cacheip" {
   value = data.aws_instance.cache.public_ip
}

output "eip" {
  value = module.elastic-beanstalk-environment.eip
}

output "appname" {
  value = module.elastic-beanstalk-application.elastic_beanstalk_application_name
}

output "envname" {
  value = module.elastic-beanstalk-environment.env_name
}