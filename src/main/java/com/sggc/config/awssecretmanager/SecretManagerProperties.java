package com.sggc.config.awssecretmanager;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Represents the configuration of the bean used to configure the AWS Secret Manager client for the application
 */
@Component
@ConfigurationProperties("secrets-manager")
@Data
public class SecretManagerProperties {
    private String address;
    private String region;
}
