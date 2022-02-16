package com.mvs.dynamodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(basePackages = { "com.mvs" })
public class DynamoDBSpringDataDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamoDBSpringDataDemoApplication.class, args);
    }

}
