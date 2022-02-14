## Spring Data DynamoDB Repository Demo Application

In this repo we create a table named Product with ID placed the "partition key".
We will use the technologies below to create a demo application:

- **Terraform** to provision DynamoDB table. 
  - Terraform backend will be in aws:s3 and lock will be kept in the DynamoDB table. So, you need to create s3 bucket for the backend and lock table beforehand.
- **Spring Data DynamoDB** for DynamoDB repository module.
   - Spring has no officially spring data module for DynamoDB and suggests using this [External Library](https://github.com/boostchicken/spring-data-dynamodb) in [Spring doc.](https://spring.io/projects/spring-data ) 
- **Spring Actuator** for component and application health status.
- **Spring Web** for Restful API for CRUD operations.
- **Swagger** for Rest API documentation.
- **JWT Token** for API endpoints.
- **Docker** for containerizing our distribution package.

* [Storage Deploy](#storage-deploy)
* [Build and push Docker](#build-and-push-docker)
* [Run and test the application](#run-and-test-the-application)
* [Storage Destroy](#storage-destroy)


## Storage Deploy
In this section we will deploy the storage by using Terraform. We proviosion a DynamoDB table with the following configuration:
```terraform
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
```
To build the storage, we need to run the following command and let the Product table provisioned:
```shell
sh deploy/storage/deploy.sh
```

## Build and push Docker
To build and push the Docker image, we need to run the following command:
```shell
sh deploy/backend/api/image/deploy.sh
```

## Run and test the application
To run the application, we need to run the following command:
```shell

````

## Storage Destroy
To destroy the storage, we need to run the following command:
```shell
sh deploy/storage/destroy.sh
```