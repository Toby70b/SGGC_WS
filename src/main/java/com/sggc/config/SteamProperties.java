package com.sggc.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Represents the configuration of the bean used to configure the AWS Secret Manager client for the application
 */
@Component
@ConfigurationProperties("steam")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SteamProperties {
    private String apiAddress;
    private String storeAddress;
}
