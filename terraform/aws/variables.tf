variable "aws_access_key" {
  type = string
  default = "AKIAZRSZ5C34T225FBOV"
}
variable "aws_secret_key" {
  type = string
  default = "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"
}

variable "app_count" {
  type    = number
  default = 1
}

variable "cidr_block" {
  default = "10.0.0.0/24"
}

variable "availability_zones" {
  default = ["us-east-1b", "us-east-1c"]
}

variable "private_subnet_cidr_block" {
  default = ["10.0.0.0/26", "10.0.0.64/26"]
}

variable "public_subnet_cidr_block" {
  default = ["10.0.0.128/26", "10.0.0.192/26"]
}

variable "container_port" {
  default = 80
}
