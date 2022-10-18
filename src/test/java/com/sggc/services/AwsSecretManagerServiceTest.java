package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsSecretManagerServiceTest {

    @Mock
    private SecretsManagerClient client;

    @InjectMocks
    private AwsSecretManagerService secretManagerService;

    @Test
    @DisplayName("Given a request is made to retrieve a secret when an error occurs attempting to retrieve a secret, then throw a appropriate exception")
    void GivenRequestToRetrieveSecretWhenAnErrorOccursRetrievingSecretThenThrowException() {
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId("secretKey")
                .build();
        GetSecretValueResponse response = GetSecretValueResponse.builder().secretString("SomeSecretValue").build();

        GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);

        when(client.getSecretValue(valueRequest)).thenThrow(SecretsManagerException.class);
        SecretRetrievalException expectedException =
                assertThrows(SecretRetrievalException.class, ()->secretManagerService.getSecretValue("secretKey"));

        assertEquals("Exception occurred when attempting to retrieve secret from AWS secrets manager",
                expectedException.getMessage());
        assertTrue(expectedException.getCause() instanceof SecretsManagerException);
    }
}
