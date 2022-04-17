# Module for creating EFS in AWS

## Usage

```
module "cache_efs" {
  // creates EFS and mount target for it
  source = "./modules/aws_efs"
  vpc_id = aws_default_vpc.default.id
  subnets = [aws_default_subnet.az1.id]
  name = "Cache_EFS"
  encrypted = true
}
```

You'll then need to add any EC2 instance wanting to access the EFS mount to the `module.efs_mount.ec2_security_group_id` security group

---

## Required Inputs

__name__(string)

Description:  The reference_name of your file system. Also, used in tags.

__subnets__(list)

Description:  A list of subnet ids where mount targets will be.

__vpc_id__(string)

Description:  The VPC ID where NFS security groups will be.

---

## Optional Inputs

__encrypted__(bool)

Description: If true, the disk will be encrypted

Default: false

__tags__ (map(string))

Description: A mapping of tags to apply to resources

Default: {}

---

## Outputs

__file_system_dns_name__

Description: EFS DNS name

__file_system_arn__

Description: EFS ARN name

__file_system_id__

Description: EFS id

__ec2_security_group_id__

Description: security group for accessing to EFS