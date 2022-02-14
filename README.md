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
* [Run the application in Local](#run-the-application-in-local)
* [Call API endpoints](#call-api-endpoints)
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
docker run -p 8080:8080 \
  -e JWT_SECRET=${JWT_SECRET} \
  -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
  -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_CESS_KEY} \
   docker.io/soyvural/spring-data-dynamodb
````

## Call API endpoints
1. Authenticate and get a JWT token:
  - User role can only call "GET /products" and "GET /products/{id}" endpoints.
  - So, let's get a JWT token for the admin user and access every endpoint.

```shell
  curl --location --request POST 'http://localhost:8080/authenticate' --header 'Content-Type: application/json' \
  --data-raw '{
      "username": "admin",
      "password": "pwd"
  }''
  
  Response:
  {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0NDg0MDE5NSwiZXhwIjoxNjQ0OTI2NTk1LCJpc3MiOiJjb20ubXZzIn0.D3jONpGmivO_ZrI141LgfC35Hyje_bMW_1D5kzf4_G-xtIH9F4I-FaiyLskZG_tjPUEvQ5O2Xbu-RF2GR3mM7A",
    "expiresIn": "Tue Feb 15 12:03:15 UTC 2022"
  }
```

2. Call "GET /products" and "GET products/{id}" endpoint:
  - We can get all products by calling "GET /products" endpoint.
```shell
curl --location --request GET 'http://localhost:8080/api/v1/products' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0NDgzMDM3MSwiZXhwIjoxNjQ0OTE2NzcxLCJpc3MiOiJjb20ubXZzIn0.n0DI9SMhYzFKCg7K4atg1iaTEfqR1Td8SEbULin-ybeFsNzd9pScoGjAwKypaV-BRhq1Vr2PiLSg7_KMDgb50w'

Response:
[
    {
        "id": "869d9b64-6159-4b5b-9fc3-944c6407ad38",
        "name": "Iphone 13",
        "category": "Mobile Phone",
        "price": 134.0
    },
    {
        "id": "de5d47f1-460a-4217-b862-f1b3a9a1fc05",
        "name": "Iphone 13",
        "category": "Mobile Phone",
        "price": 1302.16
    }
]

curl --location --request GET 'http://localhost:8080/api/v1/products/de5d47f1-460a-4217-b862-f1b3a9a1fc05' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0NDgzMDM3MSwiZXhwIjoxNjQ0OTE2NzcxLCJpc3MiOiJjb20ubXZzIn0.n0DI9SMhYzFKCg7K4atg1iaTEfqR1Td8SEbULin-ybeFsNzd9pScoGjAwKypaV-BRhq1Vr2PiLSg7_KMDgb50w' \
--data-raw ''

Response:
{
    "id": "de5d47f1-460a-4217-b862-f1b3a9a1fc05",
    "name": "Iphone 13",
    "category": "Mobile Phone",
    "price": 1302.16
}
```

3. Call "DELETE products/{id}" endpoint:
```shell
curl --location --request DELETE 'http://localhost:8080/api/v1/products/7c4d88b0-fb2f-45fe-885f-5a8117972d11' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0NDgzMDM3MSwiZXhwIjoxNjQ0OTE2NzcxLCJpc3MiOiJjb20ubXZzIn0.n0DI9SMhYzFKCg7K4atg1iaTEfqR1Td8SEbULin-ybeFsNzd9pScoGjAwKypaV-BRhq1Vr2PiLSg7_KMDgb50w' \
--data-raw ''

Invalid Product Example:
curl --location --request DELETE 'http://localhost:8080/api/v1/products/7c4d88b0-fb2f-45fe-885f-5a81179' \
> --header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0NDgzMDM3MSwiZXhwIjoxNjQ0OTE2NzcxLCJpc3MiOiJjb20ubXZzIn0.n0DI9SMhYzFKCg7K4atg1iaTEfqR1Td8SEbULin-ybeFsNzd9pScoGjAwKypaV-BRhq1Vr2PiLSg7_KMDgb50w' \
> --data-raw ''
{"timestamp":"2022-02-14T12:21:29.159+00:00","message":"Not found","details":"uri=/api/v1/products/7c4d88b0-fb2f-45fe-885f-5a81179"}
```

You can populate for put as well.

## Swagger UI
To access REST API documentation you can use: http://localhost:8080/swagger-ui/
1. To access product-api-v1 version 1.0 documentation by using http://localhost:8080/v2/api-docs?group=product-api-v1.0

![swagger-ui-secreenshot](https://i.ibb.co/6Pm8H4q/Screenshot-2022-02-14-at-12-49-07.png)

## Storage Destroy
To destroy the storage, we need to run the following command:
```shell
sh deploy/storage/destroy.sh
```