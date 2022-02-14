package com.mvs.dynamodb.service;

import com.mvs.dynamodb.model.Product;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface ProductService {
    Product create(@NotNull Product product);

    Product getById(@NotEmpty String id);

    Product update(@NotNull Product product);

    void delete(@NotEmpty String id);

    List<Product> getAll();
}
