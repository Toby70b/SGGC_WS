package com.sggc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class SecretManagerConfig {

    /**
     * Creates a new instance of the AWS Secrets Manager client to perform actions on AWS secrets
     *
     * @return a new instance of the AWS Secrets Manager client
     */
    @Bean
    public SecretsManagerClient secretManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.EU_WEST_2)
                .build();
    }
}
