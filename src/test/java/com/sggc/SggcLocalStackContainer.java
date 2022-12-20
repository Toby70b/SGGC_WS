package com.sggc;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;

public class SggcLocalStackContainer extends LocalStackContainer {

    private static final String DEFAULT_DOCKER_IMAGE = "localstack/localstack:latest";
    private static final int DEFAULT_EXPOSED_PORT = 4566;
    private static final LocalStackContainer.Service[] ENABLED_SERVICES = {SECRETSMANAGER};

    public SggcLocalStackContainer() {
        super(DockerImageName.parse(DEFAULT_DOCKER_IMAGE));
        this.withExposedPorts(DEFAULT_EXPOSED_PORT)
                .withServices(ENABLED_SERVICES);
    }

    /**
     * Resets the state of LocalStack instance
     */
    public void reset() {
        for (Service enabledService:ENABLED_SERVICES) {
            switch (enabledService){
                case SECRETSMANAGER:
                    AWSSecretsManager secretsManagerClient = AWSSecretsManagerClientBuilder.standard()
                            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:"+this.getFirstMappedPort().toString(), "eu-west-2"))
                            .withCredentials(new DefaultAWSCredentialsProviderChain())
                            .build();
                    AwsSecretsManagerCleanerTest awsSecretsManagerCleaner = new AwsSecretsManagerCleanerTest(secretsManagerClient);
                    awsSecretsManagerCleaner.performCleanup();
                    break;
            }
        }
    }

}
