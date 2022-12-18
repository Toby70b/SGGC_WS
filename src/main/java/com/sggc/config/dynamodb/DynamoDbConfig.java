package com.sggc.config.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.RequiredArgsConstructor;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Represents the configuration for DynamoDB
 */
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.sggc.repositories")
@RequiredArgsConstructor
public class DynamoDbConfig {

    private AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    private final DynamoDbProperties dynamoDbProperties;

    /**
     * Creates a new instance of the AWS DynamoDB client to perform actions on DynamoDB databases
     * <p/>
     * If the 'ENVIRONMENT' environment variable is equal to 'local' then the instance will be configured to connect to
     * a local AWS Secret Manager instance running on the host machine.
     *
     * @return a new instance of the AWS DynamoDB client
     */
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();
        if (!dynamoDbProperties.getAddress().isEmpty()) {
            clientBuilder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(dynamoDbProperties.getAddress(), dynamoDbProperties.getRegion()));
        } else {
            clientBuilder.withRegion(dynamoDbProperties.getRegion()).build();
        }
        return clientBuilder.withCredentials(amazonAWSCredentialsProvider()).build();
    }
}
