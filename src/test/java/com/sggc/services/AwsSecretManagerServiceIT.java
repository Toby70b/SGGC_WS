package com.sggc.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.sggc.config.CachingConfig;
import com.sggc.exceptions.SecretRetrievalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.*;
@SpringBootTest(classes = AwsSecretManagerService.class)
@Import(CachingConfig.class)
class AwsSecretManagerServiceIT {

    @MockBean
    private AWSSecretsManager client;

    @Autowired
    private AwsSecretManagerService secretManagerService;

    @Test
    @DisplayName("Given a previously cached secret, when subsequently retrieving the secret, then use the secret in the cache")
    void givenCachedKeyWhenRetrievingAKeyThenUseCache() throws SecretRetrievalException {

        GetSecretValueRequest valueRequest = new GetSecretValueRequest()
                .withSecretId("someSecretKey");
        GetSecretValueResult valueResponse = new GetSecretValueResult()
                .withSecretString("SomeSecretValue");

        when(client.getSecretValue(valueRequest)).thenReturn(valueResponse);
        secretManagerService.getSecretValue("someSecretKey");
        verify(client).getSecretValue(valueRequest);

        secretManagerService.getSecretValue("someSecretKey");
        secretManagerService.getSecretValue("someSecretKey");
        verify(client, times(1)).getSecretValue(valueRequest);
    }

}