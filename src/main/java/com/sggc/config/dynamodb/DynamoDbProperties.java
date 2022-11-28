package com.sggc.config.dynamodb;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//todo javadoc
@Component
@ConfigurationProperties("dynamodb")
@Data
public class DynamoDbProperties {
    private String address;
    private String region;
}
