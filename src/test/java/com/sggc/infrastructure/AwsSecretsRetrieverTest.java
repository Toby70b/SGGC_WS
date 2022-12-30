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
    @DisplayName("Given a request is made to retrieve a secret when the secret is found then the secret's value should be returned")
    void GivenRequestToRetrieveSecretWhenTheSecretIsFoundThenTheSecretsValueShouldBeReturned() throws SecretRetrievalException {
        String mockSecretId = "secretId";
        String mockSecretVlaue = "secretName";

        GetSecretValueRequest valueRequest = new GetSecretValueRequest()
                .withSecretId(mockSecretId);
        when(client.getSecretValue(valueRequest)).thenReturn(new GetSecretValueResult().withSecretString(mockSecretVlaue));
        String secretValue = secretManagerService.getSecretValue(mockSecretId);
        assertEquals(mockSecretVlaue, secretValue);
    }


    @Test
    @DisplayName("Given a request is made to retrieve a secret when an error occurs attempting to retrieve a secret, then throw a appropriate exception")
    void GivenRequestToRetrieveSecretWhenAnErrorOccursRetrievingSecretThenThrowException() {
        GetSecretValueRequest valueRequest = new GetSecretValueRequest()
                .withSecretId("secretKey");

        when(client.getSecretValue(valueRequest)).thenThrow(AWSSecretsManagerException.class);
        SecretRetrievalException expectedException =
                assertThrows(SecretRetrievalException.class, ()->secretManagerService.getSecretValue("secretKey"));

        assertEquals("Exception occurred when attempting to retrieve secret [secretKey] from AWS secrets manager",
                expectedException.getMessage());
        assertTrue(expectedException.getCause() instanceof AWSSecretsManagerException);
    }

}
