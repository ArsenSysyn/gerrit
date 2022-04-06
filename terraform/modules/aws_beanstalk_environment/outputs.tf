output "env_name" {
   value = aws_elastic_beanstalk_environment.default.name
}

output "url" {
  value = aws_elastic_beanstalk_environment.default.cname
}