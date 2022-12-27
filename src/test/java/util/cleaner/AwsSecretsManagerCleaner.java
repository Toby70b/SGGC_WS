package util.cleaner;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;

/**
 * Represents a class that can reset a given AWS Secrets Manager service to a state resembling its first initialization.
 * Designed to be used by integration tests to avoid pollution.
 */
public class AwsSecretsManagerCleaner implements TestResourceCleaner {

    private final AWSSecretsManager secretsManagerClient;

    /**
     * @param secretsManagerClient a preconfigured AWS Secrets Manager client
     */
    public AwsSecretsManagerCleaner(AWSSecretsManager secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    @Override
    public void performCleanup() {
        ListSecretsResult result = secretsManagerClient.listSecrets(new ListSecretsRequest());
        for (SecretListEntry secret: result.getSecretList()) {
            DeleteSecretRequest request =  new DeleteSecretRequest()
                    .withSecretId(secret.getName())
                    .withForceDeleteWithoutRecovery(true);
            secretsManagerClient.deleteSecret(request);
        }
    }
}
