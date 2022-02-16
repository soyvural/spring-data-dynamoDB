package com.mvs.dynamodb.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvs.dynamodb.DynamoDBSpringDataDemoApplication;
import com.mvs.dynamodb.config.SecurityTestContextConfiguration;
import com.mvs.dynamodb.model.Product;
import com.mvs.dynamodb.service.ProductService;
import com.mvs.dynamodb.web.exception.ProductNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ProductController.class)
@Import({ SecurityTestContextConfiguration.class })
@ContextConfiguration(classes = DynamoDBSpringDataDemoApplication.class)
@ActiveProfiles("test")
public class ProductControllerTest {
        @Autowired
        private MockMvc mvc;

        @MockBean
        private ProductService productService;

        @Autowired
        ObjectMapper objectMapper;

        @Before
        public void setUp() {
                reset(productService);
        }

        @Test
        public void whenCallEndpointsWithoutToken_thenReturnHTTPUnauthorized() throws Exception {
                mvc.perform(get("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());

                mvc.perform(post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());

                mvc.perform(put("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());

                mvc.perform(delete("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        public void whenCallEndpointsWithInsufficientRole_thenReturnHTTPForbidden() throws Exception {
                var authHeader = "Bearer " + SecurityTestContextConfiguration.TEST_USER_TOKEN;
                var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                                1000d);

                mvc.perform(post("/api/v1/products")
                                .content(objectMapper.writeValueAsString(iphone13Pro))
                                .contentType(MediaType.APPLICATION_JSON).header("Authorization", authHeader))
                                .andExpect(status().isForbidden());

                mvc.perform(put("/api/v1/products/{id}", "f03f8643-d547-435c-a980-d9c013c86de4")
                                .content(objectMapper.writeValueAsString(iphone13Pro))
                                .contentType(MediaType.APPLICATION_JSON).header("Authorization", authHeader))
                                .andExpect(status().isForbidden());

                mvc.perform(delete("/api/v1/products/{id}", "f03f8643-d547-435c-a980-d9c013c86de4")
                                .contentType(MediaType.APPLICATION_JSON).header("Authorization", authHeader))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void givenProducts_whenGetAll_thenReturnJsonArray() throws Exception {
                var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                                1000d);
                var macBookPro = new Product("14668529-0e4c-4368-abd8-f88a8c22c891", "MacBook Pro", "Laptop", 8000d);
                List<Product> allProducts = Arrays.asList(iphone13Pro, macBookPro);

                String returnAll = objectMapper.writeValueAsString(allProducts);

                given(productService.getAll()).willReturn(allProducts);

                String actualProducts = mvc.perform(get("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_USER_TOKEN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andReturn().getResponse().getContentAsString();

                assertThat(actualProducts, is(returnAll));
                verify(productService, VerificationModeFactory.times(1)).getAll();
        }

        @Test
        public void givenValidId_whenById_thenReturnJson() throws Exception {
                var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                                1000d);

                given(productService.getById(iphone13Pro.getId())).willReturn(iphone13Pro);

                mvc.perform(get("/api/v1/products/{id}", "f03f8643-d547-435c-a980-d9c013c86de4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_USER_TOKEN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(iphone13Pro.getId())))
                                .andExpect(jsonPath("$.name", is(iphone13Pro.getName())))
                                .andExpect(jsonPath("$.category", is(iphone13Pro.getCategory())))
                                .andExpect(jsonPath("$.price", is(iphone13Pro.getPrice())));

                verify(productService, VerificationModeFactory.times(1)).getById(iphone13Pro.getId());
        }

        @Test
        public void givenInvalidId_whenGetById_thenReturnHTTPNotFound() throws Exception {
                var id = "a2e945d1-7b47-441e-9732-68b0bb76e411";

                given(productService.getById(any())).willThrow(new ProductNotFoundException(id));

                mvc.perform(get("/api/v1/products/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_USER_TOKEN))
                                .andExpect(status().isNotFound());

                verify(productService, VerificationModeFactory.times(1)).getById(id);
        }

        @Test
        public void givenProduct_whenCreate_thenReturnJson() throws Exception {
                var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                                1000d);

                given(productService.create(iphone13Pro)).willReturn(iphone13Pro);

                mvc.perform(post("/api/v1/products")
                                .content(objectMapper.writeValueAsString(iphone13Pro))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_ADMIN_TOKEN))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location",
                                                containsString("/api/v1/products/" + iphone13Pro.getId())));

                verify(productService, VerificationModeFactory.times(1)).create(iphone13Pro);
        }

        @Test
        public void givenValidProduct_whenUpdate_thenReturnJson() throws Exception {
                var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                                1000d);

                given(productService.update(iphone13Pro)).willReturn(iphone13Pro);

                mvc.perform(put("/api/v1/products/{id}", "f03f8643-d547-435c-a980-d9c013c86de4")
                                .content(objectMapper.writeValueAsString(iphone13Pro))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_ADMIN_TOKEN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is(iphone13Pro.getId())))
                                .andExpect(jsonPath("$.name", is(iphone13Pro.getName())))
                                .andExpect(jsonPath("$.category", is(iphone13Pro.getCategory())))
                                .andExpect(jsonPath("$.price", is(iphone13Pro.getPrice())));

                verify(productService, VerificationModeFactory.times(1)).update(iphone13Pro);
        }

        @Test
        public void givenInvalidProduct_whenUpdate_thenReturnHTTPNotFound() throws Exception {
                var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone",
                                1000d);

                given(productService.update(iphone13Pro))
                                .willThrow(new ProductNotFoundException("f03f8643-d547-435c-a980-d9c013c86de4"));

                mvc.perform(put("/api/v1/products/{id}", "f03f8643-d547-435c-a980-d9c013c86de4")
                                .content(objectMapper.writeValueAsString(iphone13Pro))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_ADMIN_TOKEN))
                                .andExpect(status().isNotFound());

                verify(productService, VerificationModeFactory.times(1)).update(iphone13Pro);
        }

        @Test
        public void givenValidId_whenDelete_thenReturnHTTPNoContent() throws Exception {
                var id = "f03f8643-d547-435c-a980-d9c013c86de4";

                willDoNothing().given(productService).delete(id);

                mvc.perform(delete("/api/v1/products/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_ADMIN_TOKEN))
                                .andExpect(status().isNoContent());

                verify(productService, VerificationModeFactory.times(1)).delete(id);
        }

        @Test
        public void givenInvalidId_whenDelete_thenReturnHTTPNotFound() throws Exception {
                var id = "f03f8643-d547-435c-a980-d9c013c86de4";

                willThrow(new ProductNotFoundException(id)).given(productService).delete(id);

                mvc.perform(delete("/api/v1/products/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + SecurityTestContextConfiguration.TEST_ADMIN_TOKEN))
                                .andExpect(status().isNotFound());

                verify(productService, VerificationModeFactory.times(1)).delete(id);
        }
}