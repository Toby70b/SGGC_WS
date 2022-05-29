package com.sggc.util;

import com.sggc.exceptions.SecretRetrievalException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public class SteamKeyRetriever {
    private static final String STEAM_API_KEY_NAME = "SteamAPIKey";
    private final String steamKey;
    private static SteamKeyRetriever instance;

    private SteamKeyRetriever() {
        SecretsManagerClient secretsManagerClient = SecretManagerUtil.createSecretManagerClient();
        this.steamKey =  SecretManagerUtil.getSecretValue(secretsManagerClient,STEAM_API_KEY_NAME);
        secretsManagerClient.close();
    }

    public static SteamKeyRetriever getInstance() throws SecretRetrievalException {
        if (instance == null){
            try{
                instance = new SteamKeyRetriever();
            }
            catch (Exception e){
                throw new SecretRetrievalException("Exception occurred when attempting to retrieve Steam API Key from AWS secrets manager",e);
            }
        }
        return instance;
    }

    public String getSteamKey() {
        return steamKey;
    }
}
