package br.com.project.aws_project02.config.local;

import br.com.project.aws_project02.repository.ProductEventLogRepository;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableDynamoDBRepositories(basePackageClasses = ProductEventLogRepository.class)
@Profile("local")
public class DynamoDBConfigLocal {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoDBConfigLocal.class);

    private final AmazonDynamoDB amazonDynamoDB;

    public DynamoDBConfigLocal() {
        this.amazonDynamoDB = AmazonDynamoDBClient.builder()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                "http://localhost:4566",
                                Regions.US_EAST_1.getName()))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

        try {
            Table table = dynamoDB.getTable("product-events");
            table.describe(); // This will throw an exception if the table does not exist
            LOG.info("Table 'product-events' already exists.");
        } catch (ResourceNotFoundException e) {
            // Table does not exist, create it
            LOG.info("Table 'product-events' does not exist. Creating table.");

            List<AttributeDefinition> attributeDefinitions = Arrays.asList(
                    new AttributeDefinition().withAttributeName("pk").withAttributeType(ScalarAttributeType.S),
                    new AttributeDefinition().withAttributeName("sk").withAttributeType(ScalarAttributeType.S)
            );

            List<KeySchemaElement> keySchema = Arrays.asList(
                    new KeySchemaElement().withAttributeName("pk").withKeyType(KeyType.HASH),
                    new KeySchemaElement().withAttributeName("sk").withKeyType(KeyType.RANGE)
            );

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName("product-events")
                    .withKeySchema(keySchema)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withBillingMode(BillingMode.PAY_PER_REQUEST);

            Table newTable = dynamoDB.createTable(request);

            try {
                newTable.waitForActive();
                LOG.info("Table 'product-events' created successfully.");
            } catch (InterruptedException ex) {
                LOG.error("Failed to wait for table 'product-events' to become active", ex);
            }
        } catch (Exception e) {
            LOG.error("Error checking or creating table 'product-events'", e);
        }
    }

    @Bean
    @Primary
    public DynamoDBMapperConfig dynamoDBMapperConfig(){
        return DynamoDBMapperConfig.DEFAULT;
    }

    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB,
                                         DynamoDBMapperConfig config){
        return new DynamoDBMapper(amazonDynamoDB, config);
    }

    @Bean
    @Primary
    public AmazonDynamoDB amazonDynamoDB(){
        return this.amazonDynamoDB;
    }
}
