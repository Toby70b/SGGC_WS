package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.mockito.Mockito.*;


@SpringBootTest
class AwsSecretManagerServiceSpringBootTest {

    @MockBean
    private SecretsManagerClient client;

    @Autowired
    private AwsSecretManagerService secretManagerService;

    @Test
    @DisplayName("Given a previously cached secret, when subsequently retrieving the secret, then use the secret in the cache")
    void givenCachedKeyWhenRetrievingAKeyThenUseCache() throws SecretRetrievalException {
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId("someSecretKey")
                .build();
        GetSecretValueResponse response = GetSecretValueResponse.builder().secretString("SomeSecretValue").build();
        when(client.getSecretValue(valueRequest)).thenReturn(response);
        secretManagerService.getSecretValue("someSecretKey");
        verify(client).getSecretValue(valueRequest);

        secretManagerService.getSecretValue("someSecretKey");
        secretManagerService.getSecretValue("someSecretKey");
        verify(client, times(1)).getSecretValue(valueRequest);
    }

}