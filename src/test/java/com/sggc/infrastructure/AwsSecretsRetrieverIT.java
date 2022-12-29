package com.sggc.infrastructure;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.sggc.AbstractIntegrationTest;
import com.sggc.exceptions.SecretRetrievalException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import util.clientfactories.AWSSecretsManagerClientFactory;
import util.extentions.SggcLocalStackCleanerExtension;
import util.util.AwsSecretsManagerTestUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;
import static util.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_ID;
import static util.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;

class AwsSecretsRetrieverIT extends AbstractIntegrationTest {


    @RegisterExtension
    SggcLocalStackCleanerExtension localStackCleanerExtension
            = new SggcLocalStackCleanerExtension(localStackContainer.getFirstMappedPort(), List.of(SECRETSMANAGER));

    private static AWSSecretsManager secretsManagerClient;

    @BeforeAll
    static void beforeAll() {
        secretsManagerClient = new AWSSecretsManagerClientFactory().createClient(localStackContainer.getFirstMappedPort());
    }

    @Autowired
    private AwsSecretRetriever awsSecretRetriever;

    @Test
    @DisplayName("Given a request to retrieve a secret when the secret is found then the secret's value will be returned")
    void givenARequestToRetrieveASecretWhenTheSecretIsFoundThenTheSecretsValueWillBeReturned() throws SecretRetrievalException {
        AwsSecretsManagerTestUtil.createMockSteamApiKey(secretsManagerClient);
        String secretValue = awsSecretRetriever.getSecretValue(MOCK_STEAM_API_KEY_ID);
        assertEquals(secretValue, MOCK_STEAM_API_KEY_VALUE);
    }

    @Test
    @DisplayName("Given a request to retrieve a secret when the secret cannot not be found then an appropriate exception will be thrown")
    void givenARequestToRetrieveASecretWhenTheSecretCannotBeFoundThenAnAppropriateExceptionWillBeThrown() {
        SecretRetrievalException expectedException = assertThrows(SecretRetrievalException.class,
                () -> awsSecretRetriever.getSecretValue("someSecretId"));

        assertEquals("Exception occurred when attempting to retrieve secret [someSecretId] from AWS secrets manager",
                expectedException.getMessage());
    }

}