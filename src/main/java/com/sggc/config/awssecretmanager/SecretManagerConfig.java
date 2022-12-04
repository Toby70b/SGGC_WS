package com.sggc.config.awssecretmanager;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.sggc.config.dynamodb.DynamoDbProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SecretManagerConfig {

    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    public static final String LOCAL_ENVIRONMENT_NAME = "local";


    @Value("${environment}")
    private String environment;

    private final SecretManagerProperties secretManagerProperties;

    /**
     * Creates a new instance of the AWS Secrets Manager client to perform actions on AWS secrets.
     * <p/>
     * If the 'ENVIRONMENT' environment variable is equal to 'local' then the instance will be configured to connect to
     * a local AWS Secret Manager instance running on the host machine.
     *
     * @return a new instance of the AWS Secrets Manager client
     */
    @Bean
    public AWSSecretsManager secretManagerClient() {
        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        if (LOCAL_ENVIRONMENT_NAME.equalsIgnoreCase(environment)) {
            clientBuilder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(secretManagerProperties.getAddress(), secretManagerProperties.getRegion()));
        } else {
            clientBuilder.withRegion(secretManagerProperties.getRegion()).build();
        }
        return clientBuilder.withCredentials(amazonAWSCredentialsProvider()).build();
    }
}
