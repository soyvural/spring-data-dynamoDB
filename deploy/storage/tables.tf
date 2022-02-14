resource "aws_dynamodb_table" "product" {
  name         = "product"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Name        = "product"
    Environment = "staging"
  }
}