package util.cleaner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Represents a class that can reset a given Dynamo DB service to a state resembling its first initialization with SGGC
 * table structure.
 */
public class DynamoDbCleaner implements TestResourceCleaner {

    public static final String LOCAL_DYNAMO_DB_DDL_PATH = "Local-Developer-Setup/DynamoDB/tables/";
    private final AmazonDynamoDB dynamoDbClient;
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * @param dynamoDbClient a preconfigured Amazon Dynamo DB client.
     */
    public DynamoDbCleaner(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void performCleanup() {
        ListTablesResult tables = dynamoDbClient.listTables();
        List<String> tableNames = tables.getTableNames();
        for (String tableName : tableNames) {
            Reader reader;
            try {
                reader = Files.newBufferedReader(Paths.get(String.format("%s%s.json", LOCAL_DYNAMO_DB_DDL_PATH, tableName)));
            } catch (IOException e) {
                throw new RuntimeException("Exception occurred when trying to re-create deleted DynamoDB tables", e);
            }
            dynamoDbClient.deleteTable(tableName);
            CreateTableRequest createTableRequest = gson.fromJson(reader, CreateTableRequest.class);
            dynamoDbClient.createTable(createTableRequest);
        }
    }
}
