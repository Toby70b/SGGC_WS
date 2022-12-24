package com.sggc.extentions;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.sggc.cleaner.AmazonDynamoDbCleaner;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.sggc.constants.TestAwsConstants.DEFAULT_REGION;

/**
 * Represents a custom JUnit Extension, used to remove any data from a SGGC Local DynamoDb instance while leaving
 * the table structure intact
 */
public class SggcLocalDynamoDbCleanerExtension implements BeforeEachCallback {

    private final AmazonDynamoDbCleaner dynamoDbCleaner;

    /**
     * Initializes a new SggcLocalDynamoDbCleanerExtension object
     * @param port the port number of the local DynamoDb instance
     */
    public SggcLocalDynamoDbCleanerExtension(int port) {
        AmazonDynamoDB dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(String.format("http://localhost:%d",port), DEFAULT_REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
        dynamoDbCleaner = new AmazonDynamoDbCleaner(dynamoDbClient);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        dynamoDbCleaner.performCleanup();
    }

}
