variable "name" {
  type        = string
  description = "(Required) The reference_name of your file system. Also, used in tags."
}

variable "subnets" {
  type        = list
  description = "(Required) A list of subnet ids where mount targets will be."
}

variable "vpc_id" {
  type        = string
  description = "(Required) The VPC ID where NFS security groups will be."
}

variable "encrypted" {
  description = "(Optional) If true, the disk will be encrypted"
  default     = false
  type        = bool
}

variable "tags" {
  description = "A mapping of tags to apply to resources"
  type = map(string)
  default     = {}
}