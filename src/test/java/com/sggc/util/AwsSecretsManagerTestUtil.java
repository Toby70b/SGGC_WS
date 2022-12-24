package com.sggc.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.sggc.constants.SecretsTestConstants;

import static com.sggc.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;

/**
 * Utility class to support test classes with common functionality relating to the applications relationship with AWS
 * Secrets Manager
 */
public class AwsSecretsManagerTestUtil {
    public static void createMockSteamApiKey(AWSSecretsManager secretsManagerClient) {
        secretsManagerClient.createSecret(new CreateSecretRequest()
                .withName(SecretsTestConstants.STEAM_API_KEY_ID)
                .withSecretString(MOCK_STEAM_API_KEY_VALUE)
        );
    }
}
