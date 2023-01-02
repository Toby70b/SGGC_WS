package util.clientfactories;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import static util.constants.TestAwsConstants.*;

/**
 * Represents a factory for creating clients to interact with a local Amazon DynamoDB instance.
 */
public class LocalDynamoDbClientFactory {

    /**
     * Initializes a new {@link AmazonDynamoDB} object; A client for interacting with an Amazon DynamoDB instance. By
     * default, the service is expected to be running locally and configured to be running in the region of "eu-west-2".
     *
     * @param port the port which the Amazon DynamoDB instance is listening on.
     * @return a new client for interacting with a local Amazon DynamoDB instance.
     */
    public AmazonDynamoDB createClient(int port) {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                String.format("http://localhost:%d", port), DEFAULT_REGION))
                .withCredentials(new AWSStaticCredentialsProvider
                        (new BasicAWSCredentials(MOCK_ACCESS_KEY, MOCK_SECRET_ACCESS_KEY)))
                .build();
    }
}
