package util.extentions;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import util.cleaner.DynamoDbCleaner;
import util.clientfactories.LocalDynamoDbClientFactory;

/**
 * Represents a custom JUnit Extension, used to remove any data from a SGGC Local DynamoDB instance while leaving
 * the table structure intact.
 */
public class SggcLocalDynamoDbCleanerExtension implements BeforeEachCallback {

    private final DynamoDbCleaner dynamoDbCleaner;

    /**
     * Initializes a new SggcLocalDynamoDbCleanerExtension object.
     * @param port the port number of the local DynamoDB instance.
     */
    public SggcLocalDynamoDbCleanerExtension(int port) {
        dynamoDbCleaner = new DynamoDbCleaner(new LocalDynamoDbClientFactory().createClient(port));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        dynamoDbCleaner.performCleanup();
    }

}
