output "cacheip" {
   value = data.aws_instance.cache.public_ip
}

output "appname" {
  value = module.elastic-beanstalk-application.elastic_beanstalk_application_name
}

output "envname" {
  value = module.elastic-beanstalk-environment.env_name
}

output "url" {
  value = module.elastic-beanstalk-environment.url
}

output "region" {
  value = data.aws_region.current.name
}