package com.mvs.dynamodb.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.mvs.dynamodb.DynamoDBSpringDataDemoApplication;
import com.mvs.dynamodb.model.Product;
import com.mvs.dynamodb.rule.LocalDbCreationRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = DynamoDBSpringDataDemoApplication.class)
@TestPropertySource(properties = {
        "amazon.dynamodb.endpoint=http://localhost:8000/",
        "amazon.aws.accesskey=test1",
        "amazon.aws.secretkey=test231",
})
@ActiveProfiles("test")
public class ProductRepositoryIntegrationTest {

    @ClassRule
    public static LocalDbCreationRule dynamoDB = new LocalDbCreationRule();


    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    ProductRepository repository;


    @Before
    public void setup() throws Exception {
        try {
            dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Product.class);
            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
            // Do nothing, table already created
        }
    }

    @After
    public void tearDown() throws Exception {
        reset(repository);
        dynamoDBMapper.batchDelete(repository.findAll());
    }

    @Test
    public void given2Products_whenRunFindAll_thenReturn2Records() {
        var iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone", 1000d);
        var macBookPro = new Product("14668529-0e4c-4368-abd8-f88a8c22c891", "MacBook Pro", "Laptop", 8000d);
        List<Product> expectedProducts = Arrays.asList(iphone13Pro, macBookPro);

        repository.save(iphone13Pro);
        repository.save(macBookPro);
        List<Product> result = (List<Product>) repository.findAll();

        assertThat(result.size(), is(2));
        assertThat(result, is(expectedProducts));
        verify(repository, VerificationModeFactory.times(1)).findAll();
    }

    @Test
    public void givenProduct_whenRunFindById_thenReturnRecord() {
        var id = "f03f8643-d547-435c-a980-d9c013c86de4";
        var iphone13Pro = new Product(id, "Iphone13 Pro", "Mobile Phone", 1000d);

        repository.save(iphone13Pro);
        Product found = repository.findById(id).get();

        assertThat(iphone13Pro, is(found));
        verify(repository, VerificationModeFactory.times(1)).save(iphone13Pro);
        verify(repository, VerificationModeFactory.times(1)).findById(id);
    }

    @Test
    public void whenRunFindByInvalidId_thenReturnNull() {
        var invalidId = "f03f8643-d547-435c-a980-d9c013c86de4";

        Product found = repository.findById(invalidId).get();

        assertThat(null, is(found));
        verify(repository, VerificationModeFactory.times(1)).findById(invalidId);
    }

    @Test
    public void whenSave_thenReturnRecord() {
        Product iphone13Pro = new Product("f03f8643-d547-435c-a980-d9c013c86de4", "Iphone13 Pro", "Mobile Phone", 1000d);
        Product created = repository.save(iphone13Pro);

        assertThat(iphone13Pro, is(created));
        verify(repository, VerificationModeFactory.times(1)).save(iphone13Pro);
    }

    @Test
    public void givenId_whenDelete_thenRemoveRecord() {
        var id = "f03f8643-d547-435c-a980-d9c013c86de4";
        Product iphone13Pro = new Product(id, "Iphone13 Pro", "Mobile Phone", 1000d);

        Product created = repository.save(iphone13Pro);
        repository.deleteById(id);
        repository.findById(id).ifPresent(product -> fail("Product should not exist id: " + id));

        assertThat(iphone13Pro, is(created));
        verify(repository, VerificationModeFactory.times(1)).save(iphone13Pro);
        verify(repository, VerificationModeFactory.times(1)).deleteById(id);
        verify(repository, VerificationModeFactory.times(1)).findById(id);
    }

    @Test
    public void givenInvalidId_whenDelete_thenDoNothing() {
        var id = "f03f8643-d547-435c-a980-d9c013c86de4";

        repository.findById(id).ifPresent(product -> fail("Product should not exist id: " + id));
        repository.deleteById(id);

        verify(repository, VerificationModeFactory.times(1)).findById(id);
        verify(repository, VerificationModeFactory.times(1)).deleteById(id);
    }
}