package com.sggc.config.awssecretmanager;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Represents the configuration for AWS Secrets Manager
 */
@Configuration
@RequiredArgsConstructor
public class SecretManagerConfig {

    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    private final SecretManagerProperties secretManagerProperties;

    /**
     * Creates a new instance of the AWS Secrets Manager client to perform actions on AWS secrets.
     * <p/>
     * If the 'SECRETS_MANAGER_ADDRESS' environment variable is set then the instance will be configured to connect to
     * a local AWS Secret Manager instance running on the host machine.
     *
     * @return a new instance of the AWS Secrets Manager client
     */
    @Bean
    public AWSSecretsManager secretManagerClient() {
        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        if (!secretManagerProperties.getAddress().isEmpty()) {
            clientBuilder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(secretManagerProperties.getAddress(), secretManagerProperties.getRegion()));
        } else {
            clientBuilder.withRegion(secretManagerProperties.getRegion()).build();
        }
        return clientBuilder.withCredentials(amazonAWSCredentialsProvider()).build();
    }
}
