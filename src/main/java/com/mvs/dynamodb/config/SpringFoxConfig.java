package com.mvs.dynamodb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Set;

@Configuration
@EnableSwagger2
public class SpringFoxConfig {
    @Bean
    public Docket swaggerAPIv10() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("product-api-v1.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mvs.dynamodb.web.controller"))
                .paths(PathSelectors.regex("/api/v1/product.*"))
                .build()
                .consumes(Set.of("application/json"))
                .consumes(Set.of("application/json"))
                .apiInfo(new ApiInfoBuilder().version("1.0").title("Product API").description("Product API").license("MIT License").build());
    }
}
