output "env_name" {
   value = aws_elastic_beanstalk_environment.default.name
}

output "eip" {
  value = aws_eip.this.public_ip
}

output "url" {
  value = aws_elastic_beanstalk_environment.default.cname
}