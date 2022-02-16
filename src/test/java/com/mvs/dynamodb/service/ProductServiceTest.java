package com.mvs.dynamodb.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mvs.dynamodb.DynamoDBSpringDataDemoApplication;
import com.mvs.dynamodb.model.Product;
import com.mvs.dynamodb.repository.ProductRepository;
import com.mvs.dynamodb.web.exception.ProductNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DynamoDBSpringDataDemoApplication.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "amazon.dynamodb.endpoint=http://localhost:8000/",
        "amazon.aws.accesskey=test1",
        "amazon.aws.secretkey=test231" })
@ActiveProfiles("test")
public class ProductServiceTest {

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Before
    public void setUp() {
        reset(productRepository);
    }

    @Test
    public void given2Products_whenGetAll_thenReturn2Records() {
        var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                1000d);
        var macBookPro = new Product("14668529-0e4c-4368-abd8-f88a8c22c891", "MacBook Pro", "Laptop", 8000d);
        List<Product> expectedProducts = Arrays.asList(iphone13Pro, macBookPro);

        given(productRepository.findAll()).willReturn(expectedProducts);

        List<Product> foundProducts = productService.getAll();

        assertThat(foundProducts, is(expectedProducts));
        verify(productRepository, VerificationModeFactory.times(1)).findAll();
    }

    @Test
    public void givenValidId_whenGetById_thenProductShouldBeFound() {
        var id = "2b2d4766-37ed-4af2-970a-7c64228ab487";
        var iphone13Pro = new Product(id, "Iphone13 Pro", "Mobile Phone", 1000d);

        given(productRepository.findById(iphone13Pro.getId())).willReturn(Optional.of(iphone13Pro));

        Product found = productService.getById(id);

        assertThat(iphone13Pro, is(found));
        verify(productRepository, VerificationModeFactory.times(1)).findById(id);
    }

    @Test
    public void givenInvalidId_whenGetById_thenThrowsProductNotFoundException() {
        var id = "2b2d4766-37ed-4af2-970a-7c64228ab487";

        given(productRepository.findById(id)).willThrow(new ProductNotFoundException(id));

        try {
            productService.getById(id);
            fail("Should throw ProductNotFoundException");
        } catch (ProductNotFoundException ex) {
        }

        verify(productRepository, VerificationModeFactory.times(1)).findById(id);
    }

    @Test
    public void givenValidProduct_whenUpdate_thenProductShouldBeUpdated() {
        var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone", 1000d);

        given(productRepository.existsById(iphone13Pro.getId())).willReturn(true);
        given(productRepository.save(iphone13Pro)).willReturn(iphone13Pro);

        Product updated = productService.update(iphone13Pro);

        assertThat(iphone13Pro, is(updated));
        verify(productRepository, VerificationModeFactory.times(1)).existsById(iphone13Pro.getId());
        verify(productRepository, VerificationModeFactory.times(1)).save(iphone13Pro);
    }

    @Test
    public void givenInvalidProduct_whenUpdate_thenThrowsProductNotFoundException() {
        var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone", 1000d);
        var updatedIphone13Pro = iphone13Pro.toBuilder().price(2000d).build();

        given(productRepository.existsById(updatedIphone13Pro.getId())).willReturn(false);

        try {
            productService.update(updatedIphone13Pro);
            fail("Should throw ProductNotFoundException");
        } catch (ProductNotFoundException ex) {
        }

        verify(productRepository, VerificationModeFactory.times(1)).existsById(updatedIphone13Pro.getId());
        verify(productRepository, VerificationModeFactory.times(0)).save(updatedIphone13Pro);
    }

    @Test
    public void givenValidId_whenDelete_thenProductShouldBeDeleted() {
        var id = "f03f8643-d547-435c-a980-d9c013c86de4";

        willDoNothing().given(productRepository).deleteById(id);

        productService.delete(id);

        verify(productRepository, VerificationModeFactory.times(1)).deleteById(id);
    }

    @Test
    public void givenInvalidId_whenDelete_thenThrowsProductNotFoundException() {
        var id = "f03f8643-d547-435c-a980-d9c013c86de4";

        willThrow(EmptyResultDataAccessException.class).given(productRepository).deleteById(id);

        try {
            productService.delete(id);
            fail("Should throw EmptyResultDataAccessException");
        } catch (EmptyResultDataAccessException ex) {
        }

        verify(productRepository, VerificationModeFactory.times(1)).deleteById(id);
    }

}