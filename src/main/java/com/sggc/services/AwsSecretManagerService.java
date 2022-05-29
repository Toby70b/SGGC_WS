package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Class representing a service used to communicate with AWS Secret Manager
 */
@Service
@Log4j2
public class AwsSecretManagerService {

    private final SecretsManagerClient client = createSecretManagerClient();

    /**
     * Retrieves the given key from AWS secrets manager
     *
     * @return the specified key stored within AWS secrets manager
     * @throws SecretRetrievalException if an exception occurs trying to retrieve the key from AWS secrets manager
     */
    public String getSecretValue(String secretId) throws SecretRetrievalException {
        try {
            log.debug("Attempting to retrieve secret [{}] from AWS Secrets Manager",secretId);
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretId)
                    .build();
            GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);
            return valueResponse.secretString();
        } catch (Exception e) {
            throw new SecretRetrievalException("Exception occurred when attempting to retrieve Steam API Key from AWS secrets manager", e);
        }
    }

    /**
     * Creates a new instance of the AWS Secrets Manager client to perform actions on AWS secrets
     *
     * @return a new instance of the AWS Secrets Manager client
     */
    private SecretsManagerClient createSecretManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.EU_WEST_2)
                .build();
    }

}
