provider "aws" {
  region = "eu-west-2"
}

# using these data sources allows the configuration to be generic for any region.
data "aws_region" "current" {}

data "aws_availability_zones" "available" {}
