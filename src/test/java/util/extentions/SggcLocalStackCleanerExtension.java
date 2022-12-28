package util.extentions;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import util.cleaner.AwsSecretsManagerCleaner;
import util.cleaner.TestResourceCleaner;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.constants.TestAwsConstants.DEFAULT_REGION;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;
import static util.util.TestClientInitializer.initializeAwsSecretsManagerClient;

/**
 * Represents a custom JUnit Extension, used to remove any data from a LocalStack instance.
 */

public class SggcLocalStackCleanerExtension implements BeforeEachCallback {

    private final List<LocalStackContainer.Service> enabledServices;
    private final int port;
    private final Map<String, TestResourceCleaner> resourceCleanerMap;

    /**
     * Initializes a new SggcLocalStackCleanerExtension object.
     * @param enabledServices the services enabled on the local LocalStack instance.
     * @param port the port number of the local LocalStack instance.
     */
    public SggcLocalStackCleanerExtension(int port, List<LocalStackContainer.Service> enabledServices) {
        this.port = port;
        this.enabledServices = enabledServices;

        resourceCleanerMap = populateCleanerMap();
    }

    /**
     * Populates the resource cleaner map to prevent multiple initializations on each beforeEach() call.
     * @return a new AWSSecretsManager client.
     */
    private Map<String, TestResourceCleaner> populateCleanerMap() {
        Map<String, TestResourceCleaner> cleanerMap = new HashMap<>();
        for (LocalStackContainer.Service enabledService: enabledServices) {
            switch (enabledService){
                case SECRETSMANAGER:
                    cleanerMap.put(SECRETSMANAGER.getLocalStackName(),
                            new AwsSecretsManagerCleaner(initializeAwsSecretsManagerClient(port)));
                    break;
            }
        }
        return cleanerMap;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        for (LocalStackContainer.Service enabledService: enabledServices) {
            resourceCleanerMap.get(enabledService.getLocalStackName()).performCleanup();
        }
    }
}
