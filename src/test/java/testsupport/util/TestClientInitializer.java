package testsupport.util;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import testsupport.constants.TestAwsConstants;

/**
 * Represents a utility class for initializing clients for services used by the application and for application tests.
 */
public class TestClientInitializer {

    /**
     * Initializes a new {@link AWSSecretsManager} object; A client for interacting with an AWS Secrets Manager instance.
     * By default, the service is expected to be running locally and configured to be running in the region of "eu-west-2".
     *
     * @param port the port which the AWS Secrets Manager instance is listening on
     * @return a new client for interacting with a local AWS Secrets Manager instance.
     */
    public static AWSSecretsManager initializeAwsSecretsManagerClient(int port) {
        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                String.format("http://localhost:%d", port), TestAwsConstants.DEFAULT_REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    /**
     * Initializes a new {@link WireMock} object; A client for interacting with a Wiremock instance. By default,
     * the service is expected to be running locally.
     *
     * @param port the port which the WireMock instance is listening on
     * @return a new client for interacting with a local Wiremock instance.
     */
    public static WireMock initializeWiremockClient(int port) {
        return new WireMock("localhost", port);
    }
}
