package util.util;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import static util.constants.TestAwsConstants.DEFAULT_REGION;

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
        return configureAwsClientWithCommonConfiguration(AWSSecretsManagerClientBuilder.standard(), port).build();
    }

    /**
     * Initializes a new {@link AmazonDynamoDB} object; A client for interacting with an Amazon DynamoDB instance. By
     * default, the service is expected to be running locally and configured to be running in the region of "eu-west-2".
     *
     * @param port the port which the Amazon DynamoDB instance is listening on
     * @return a new client for interacting with a local Amazon DynamoDB instance.
     */
    public static AmazonDynamoDB initializeDynamoDBClient(int port) {
        return configureAwsClientWithCommonConfiguration(AmazonDynamoDBClientBuilder.standard(), port).build();
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

    /**
     * Configures clients for interacting with AWS services. By default, the clients are configured to communicate
     * with services that are running locally and configured to be running in the region of "eu-west-2". The client
     * is also configured with default credentials
     *
     * @param clientBuilder a client builder object that has not yet been configured
     * @param port          the port which the AWS service that client is desired to communicate with listening on
     * @return the same client builder object passed as a parameter, now configured with Endpoint and Credentials
     * Configuration
     */
    private static <T extends AwsClientBuilder<T, S>, S> T configureAwsClientWithCommonConfiguration(T clientBuilder, int port) {
        return clientBuilder
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                String.format("http://localhost:%d", port), DEFAULT_REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain());

    }
}
