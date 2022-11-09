package com.sggc.config;

import com.amazonaws.auth.*;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Represents the configuration for DynamoDB
 */
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.sggc.repositories")
public class DynamoDbConfig {

    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration
                        ("http://localhost:8000/", "local"))
                .withCredentials(amazonAWSCredentialsProvider())
                .build();
    }
}
