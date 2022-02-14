package com.mvs.dynamodb.service;

import com.google.common.collect.Lists;
import com.mvs.dynamodb.web.exception.ProductNotFoundException;
import com.mvs.dynamodb.model.Product;
import com.mvs.dynamodb.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found for id: %s";


    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND_MESSAGE, id)));
    }

    @Override
    public List<Product> getAll() {
        return Lists.newArrayList(productRepository.findAll());
    }

    @Override
    public Product update(Product product) {
        if (!productRepository.existsById(product.getId())) {
            throw new ProductNotFoundException("No data found for id: " + product.getId());
        }
        return productRepository.save(product);
    }

    @Override
    public void delete(String id) {
        productRepository.deleteById(id);
    }
}
