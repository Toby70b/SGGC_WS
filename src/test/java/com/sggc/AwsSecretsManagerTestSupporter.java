package com.sggc;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static com.sggc.TestSecretsConstants.MOCK_STEAM_API_KEY_VALUE;

/**
 * Helper class to support test classes with common functionality relating to the applications relationship with AWS
 * Secrets Manager
 */
@Data
@RequiredArgsConstructor
public class AwsSecretsManagerTestSupporter {

    private final AWSSecretsManager secretsManagerClient;

    public void createMockSteamApiKey() {
        secretsManagerClient.createSecret(new CreateSecretRequest()
                .withName(TestSecretsConstants.STEAM_API_KEY_ID)
                .withSecretString(MOCK_STEAM_API_KEY_VALUE)
        );
    }
}
