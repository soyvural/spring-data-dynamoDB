terraform {
  required_version = "= 0.13.7"
  backend "s3" {
    encrypt        = true
    bucket         = "mvs-it-terraform"
    region         = "eu-west-2"
    dynamodb_table = "mvs-it-terraform-lock"
    key            = "dynamodb-product-table"
  }
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 2.38.0"
    }
  }
}



