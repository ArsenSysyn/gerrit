# Terraform for AWS infrastructure
This terraform root module create necessary infrastructure for succesful running of application Gerrit. These are: 

- EFS for application data
- EFS for build-cache data
- ECS cluster with running bazel cache image
- Elastic Beanstalk environment where application will be running
- Security groups with needed rules

And the state of these tasks will be storing in remote state file in S3 bucket, and also we use DynamoDB for locking state. There you can see a configuration of remote state: 
```
 terraform {
   backend "s3" {
     bucket         = "mybucketforterraformstatearsen"
     key            = "first-test-2/terraform.tfstate"
     region         = "us-east-1"
     dynamodb_table = "dynamodb-lock"
     encrypt        = true
   }
}
```
For best practices we use modules for reducing lines of our terraform code. We use modules from terraform registry and another two which I created in my own.
Terraform registry modules:

- terraform-aws-modules/security-group/aws
- trussworks/ecs-cluster/aws
- mergermarket/load-balanced-ecs-service-no-target-group/acuris
- cloudposse/elastic-beanstalk-application/aws

Own modules which are stored in `/terraform/modules` directory:

- ./modules/aws_beanstalk_environment
- ./modules/aws_efs

More detailed information about modules form TF registry you can found on it, and information about own modules you can find in `README.md` files in these directories.

### All terraform code we run using CodeBuild.