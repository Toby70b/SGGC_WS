package util.extentions;

import util.cleaner.AmazonDynamoDbCleaner;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static util.util.TestClientInitializer.initializeDynamoDBClient;

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
        dynamoDbCleaner = new AmazonDynamoDbCleaner(initializeDynamoDBClient(port));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        dynamoDbCleaner.performCleanup();
    }

}
