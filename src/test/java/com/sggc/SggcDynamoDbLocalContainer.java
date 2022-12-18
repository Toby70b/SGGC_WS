package com.sggc;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SggcDynamoDbLocalContainer extends GenericContainer<SggcDynamoDbLocalContainer> {

    public static final String SUCCESS_LOG_MESSAGE_REGEX = ".*########## DB boostrap completed! ##########.*\\n";
    public static final String DEFAULT_DOCKER_FILE_LOCATION = "Local-Developer-Setup/DynamoDB/Dockerfile";
    public static final int DEFAULT_EXPOSED_PORT = 8000;

    public SggcDynamoDbLocalContainer() {
        super(new ImageFromDockerfile().withDockerfile(Path.of(DEFAULT_DOCKER_FILE_LOCATION)));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT)
            .waitingFor(Wait.forLogMessage(SUCCESS_LOG_MESSAGE_REGEX, 1));
    }

    /**
     * Resets the state of the Local DynamoDB instance to when it was first initialized.
     */
    public void reset(AmazonDynamoDB dynamoDbClient) throws IOException {
        ListTablesResult tables = dynamoDbClient.listTables();
        List<String> tableNames = tables.getTableNames();
        for (String tableName: tableNames) {
            Reader reader = Files.newBufferedReader(Paths.get("Local-Developer-Setup/DynamoDB/tables/"+tableName+".json"));
            DeleteTableResult deleteTableResult = dynamoDbClient.deleteTable(tableName);
            //TODO panic here if failed to delete?
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .create();
            CreateTableRequest createTableRequest = gson.fromJson(reader, CreateTableRequest.class);
            dynamoDbClient.createTable(createTableRequest);
        }

    }


}
