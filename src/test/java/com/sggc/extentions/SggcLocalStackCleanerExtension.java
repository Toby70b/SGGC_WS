package com.sggc.extentions;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.sggc.cleaner.AwsSecretsManagerCleaner;
import com.sggc.cleaner.TestResourceCleaner;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sggc.constants.TestAwsConstants.DEFAULT_REGION;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;

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
                            new AwsSecretsManagerCleaner(createAwsSecretsManagerClient()));
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

    /**
     * Initializes a new AWSSecretsManager client configured to be used with the local LocalStack instance.
     * @return a new AWSSecretsManager client.
     */
    private AWSSecretsManager createAwsSecretsManagerClient() {
        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(String.format("http://localhost:%d",port), DEFAULT_REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

}
