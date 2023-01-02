package com.sggc.infrastructure;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.AWSSecretsManagerException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.sggc.exceptions.SecretRetrievalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsSecretsRetrieverTest {

    @Mock
    private AWSSecretsManager client;

    @InjectMocks
    private AwsSecretRetriever secretManagerService;

    @Test
    @DisplayName("Given a request to retrieve a secret, when the secret is found ,then the secret's value should be returned.")
    void GivenRequestToRetrieveSecretWhenTheSecretIsFoundThenTheSecretsValueShouldBeReturned() throws SecretRetrievalException {
        String mockSecretId = "secretId";
        String mockSecretValue = "secretName";

        GetSecretValueRequest valueRequest = new GetSecretValueRequest()
                .withSecretId(mockSecretId);
        when(client.getSecretValue(valueRequest)).thenReturn(new GetSecretValueResult().withSecretString(mockSecretValue));
        String secretValue = secretManagerService.getSecretValue(mockSecretId);
        assertEquals(mockSecretValue, secretValue);
    }


    @Test
    @DisplayName("Given a request to retrieve a secret, when an error occurs attempting to retrieve a secret, then an appropriate exception will be thrown.")
    void GivenRequestToRetrieveSecretWhenAnErrorOccursRetrievingSecretThenThrowException() {
        String mockSecretId = "secretId";
        GetSecretValueRequest valueRequest = new GetSecretValueRequest()
                .withSecretId(mockSecretId);

        when(client.getSecretValue(valueRequest)).thenThrow(AWSSecretsManagerException.class);
        SecretRetrievalException expectedException =
                assertThrows(SecretRetrievalException.class, ()->secretManagerService.getSecretValue(mockSecretId));

        assertEquals("Exception occurred when attempting to retrieve secret [secretId] from AWS secrets manager.",
                expectedException.getMessage());
        assertTrue(expectedException.getCause() instanceof AWSSecretsManagerException);
    }

}
