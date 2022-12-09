package com.sggc.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.sggc.exceptions.SecretRetrievalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class representing a service used to communicate with AWS Secret Manager
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class AwsSecretManagerService {

    private final AWSSecretsManager client;

    //TODO can the below return null?
    /**
     * Retrieves the given key from AWS secrets manager
     *
     * @return the specified key stored within AWS secrets manager
     * @throws SecretRetrievalException if an exception occurs trying to retrieve the key from AWS secrets manager
     */
    @Cacheable("secrets")
    public String getSecretValue(String secretId) throws SecretRetrievalException {
        try {
            log.debug("Attempting to retrieve secret [{}] from AWS Secrets Manager",secretId);
            GetSecretValueRequest valueRequest = new GetSecretValueRequest()
                    .withSecretId(secretId);
            GetSecretValueResult valueResponse = client.getSecretValue(valueRequest);
            return valueResponse.getSecretString();
        } catch (Exception e) {
            throw new SecretRetrievalException("Exception occurred when attempting to retrieve secret from AWS secrets manager", e);
        }
    }

}
