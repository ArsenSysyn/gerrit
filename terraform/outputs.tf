output "eip" {
  value = aws_eip.gerrit-app.public_ip
}

output "cacheip" {
  value = data.aws_instance.foo.public_ip
}

output "appname" {
    value = aws_elastic_beanstalk_application.GerritApp.name
}

output "envname" {
    value = aws_elastic_beanstalk_environment.GerritApp-env.name
}

output "appdata"{
    value = aws_efs_file_system.appdata.id
}
