package util.clientfactories;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

import static util.constants.TestAwsConstants.*;

/**
 * Represents a factory for creating clients to interact with a local AWS Secrets Manager instance.
 */
public class AWSSecretsManagerClientFactory {

    /**
     * Initializes a new {@link AWSSecretsManager} object; A client for interacting with an AWS Secrets Manager instance.
     * By default, the service is expected to be running locally and configured to be running in the region of "eu-west-2".
     *
     * @param port the port which the AWS Secrets Manager instance is listening on
     * @return a new client for interacting with a local AWS Secrets Manager instance.
     */
    public AWSSecretsManager createClient(int port) {
        return  AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                String.format("http://localhost:%d", port), DEFAULT_REGION))
                .withCredentials(new AWSStaticCredentialsProvider
                        (new BasicAWSCredentials(MOCK_ACCESS_KEY,MOCK_SECRET_ACCESS_KEY)))
                .build();
    }
}
