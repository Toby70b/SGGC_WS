package com.sggc.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecretManagerConfig {

    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    /**
     * Creates a new instance of the AWS Secrets Manager client to perform actions on AWS secrets
     *
     * @return a new instance of the AWS Secrets Manager client
     */
    //TODO add spring beans for different profiles
    @Bean
    public AWSSecretsManager secretManagerClient() {
        return AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration
                        ("http://localhost:4566/", "eu-west-2"))
                .withCredentials(amazonAWSCredentialsProvider())
                .build();
    }
}
