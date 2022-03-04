data "aws_iam_policy_document" "ecs_agent" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

data "aws_instance" "test" {
  instance_tags = {
    "Name" = aws_elastic_beanstalk_environment.GerritApp-env.name
  }
}
data "aws_instance" "foo" {
  instance_tags = {
    "Name" = "CacheContainer"
  }
  depends_on = [
    aws_autoscaling_group.asg_ecs
  ]

}
data "aws_ip_ranges" "codebuild" {
    regions = [ "us-east-1" ]
    services = [ "codebuild" ]
  
}
