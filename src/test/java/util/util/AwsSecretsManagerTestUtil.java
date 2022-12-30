package util.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import util.constants.SecretsTestConstants;

import static util.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;

/**
 * Utility class to support test classes with common functionality relating to the applications relationship with AWS
 * Secrets Manager.
 */
public class AwsSecretsManagerTestUtil {
    public static void createMockSteamApiKey(AWSSecretsManager secretsManagerClient) {
        secretsManagerClient.createSecret(new CreateSecretRequest()
                .withName(SecretsTestConstants.MOCK_STEAM_API_KEY_ID)
                .withSecretString(MOCK_STEAM_API_KEY_VALUE)
        );
    }
}
