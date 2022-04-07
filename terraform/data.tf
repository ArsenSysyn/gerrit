data "aws_ip_ranges" "codebuild" {
    regions = [ "us-east-1" ]
    services = [ "codebuild" ] 
}


data "aws_ami" "ecs_ami" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn-ami-*-amazon-ecs-optimized"]
  }
}

data "aws_instance" "cache" {
  instance_tags = {
    "Name" = "ecs-${var.app_ecs_cluster_name}-${var.app_ecs_cluster_environment_name}"
  }
  depends_on = [
    module.app_ecs_cluster
  ]
}

data "aws_region" "current" {}