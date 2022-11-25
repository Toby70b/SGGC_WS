package com.sggc.config.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.RequiredArgsConstructor;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Represents the configuration for DynamoDB
 */
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.sggc.repositories")
@RequiredArgsConstructor
public class DynamoDbConfig {

    public static final String LOCAL_ENVIRONMENT_NAME = "local";

    private AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    @Value("${environment}")
    private String environment;

    private final DynamoDbProperties dynamoDbProperties;

    //TODO javadoc
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();
        if (LOCAL_ENVIRONMENT_NAME.equalsIgnoreCase(environment)) {
            clientBuilder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(dynamoDbProperties.getUrl(), dynamoDbProperties.getRegion()));
        } else {
            clientBuilder.withRegion(dynamoDbProperties.getRegion()).build();
        }
        return clientBuilder.withCredentials(amazonAWSCredentialsProvider()).build();
    }
}
