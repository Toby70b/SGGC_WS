package com.sggc;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Represents a class that can reset a given Dynamo DB service to a state resembling its first initialization. Designed
 * to be used by integration tests to avoid pollution.
 */
public class AmazonDynamoDbCleaner implements TestResourceCleaner {

    public static final String LOCAL_DYNAMO_DB_DDL_PATH = "Local-Developer-Setup/DynamoDB/tables/";
    private final AmazonDynamoDB dynamoDbClient;

    /**
     * @param dynamoDbClient a preconfigured Amazon Dynamo DB client
     */
    public AmazonDynamoDbCleaner(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void performCleanup() {
        ListTablesResult tables = dynamoDbClient.listTables();
        List<String> tableNames = tables.getTableNames();
        for (String tableName : tableNames) {
            Reader reader = null;
            try {
                reader = Files.newBufferedReader(Paths.get(String.format("%s%s.json", LOCAL_DYNAMO_DB_DDL_PATH, tableName)));
            } catch (IOException e) {
                //TODO dont do this, or at least wrap in a custom expcetion with an appropriate message
                throw new RuntimeException(e);
            }
            dynamoDbClient.deleteTable(tableName);
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .create();
            CreateTableRequest createTableRequest = gson.fromJson(reader, CreateTableRequest.class);
            dynamoDbClient.createTable(createTableRequest);
        }
    }
}
