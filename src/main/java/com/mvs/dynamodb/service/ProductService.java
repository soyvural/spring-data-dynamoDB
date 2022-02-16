package com.mvs.dynamodb.service;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.mvs.dynamodb.model.Product;

public interface ProductService {
    Product create(@NotNull Product product);

    Product getById(@NotEmpty String id);

    Product update(@NotNull Product product);

    void delete(@NotEmpty String id);

    List<Product> getAll();
}
