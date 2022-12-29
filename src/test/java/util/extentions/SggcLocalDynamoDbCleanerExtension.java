package util.extentions;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import util.cleaner.AmazonDynamoDbCleaner;
import util.clientfactories.LocalDynamoDbClientFactory;

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
        dynamoDbCleaner = new AmazonDynamoDbCleaner(new LocalDynamoDbClientFactory().createClient(port));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        dynamoDbCleaner.performCleanup();
    }

}
