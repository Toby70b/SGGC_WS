package com.sggc.util;

import com.google.gson.*;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.models.GetOwnedGamesResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.io.IOException;
import java.util.Collections;

import static com.sggc.util.CommonUtil.*;

/**
 * Class representing an interface for communicating with the Steam API
 */
//TODO: Look for any improvements that could be made here
@Component
@RequiredArgsConstructor
public class SteamRequestHandler {
    private final Logger logger = LoggerFactory.getLogger(SteamRequestHandler.class);

    private final RestTemplate restTemplate;
    private static final String STEAM_API_KEY_NAME = "SteamAPIKey";

    /**
     * Send a request to the Steam API's GetOwnedGames endpoint to retrieve the details of a specific game
     * @param userId the user id whose being queries
     * @return the response from the Steam API parsed into a {@link GetOwnedGamesResponse} object
     * @throws SecretRetrievalException if an error occurs retrieving the Steam API key secret from AWS secrets manager
     */
    public GetOwnedGamesResponse requestUsersOwnedGamesFromSteamApi(String userId) throws SecretRetrievalException {
        String requestUri = GET_OWNED_GAMES_ENDPOINT+"?key=" + getSteamApiKey() + "&steamid=" + userId;
        logger.debug("Contacting " + requestUri + " to get owned games of user " + userId);
        return restTemplate.getForObject(requestUri,GetOwnedGamesResponse.class);
    }

    //TODO: return parsed response
    /**
     * Send a request to the Steam API's GetAppDetails endpoint to retrieve the details of a specific game
     * @param appId the appid of the game whose details are being requested
     * @return a String response from the Steam API
     */
    public String requestAppDetailsFromSteamApi(String appId) {
        String URI = GET_APP_DETAILS_ENDPOINT + "?appids=" + appId;
        logger.debug("Contacting " + URI + " to get details of game " + appId);
        return restTemplate.getForObject(URI,String.class);
    }

    /**
     * Parses the game details list from Steam GetAppDetails endpoint into a model object
     * @param stringToParse the string to parse
     * @return a {@link GameData} object serialized from the response from the Steam API
     * @throws IOException if an error occurs while serializing the string into JSON
     */
    public GameData parseGameDetailsList(String stringToParse) throws IOException {
        Gson gson = new Gson();
        JsonElement jsonTree = parseResponseStringToJson(stringToParse);
        JsonObject obj = jsonTree.getAsJsonObject();
        // The root of the response is a id of the game thus get the responses root value
        String gameId = obj.keySet().iterator().next();
        obj = obj.getAsJsonObject(gameId);
        boolean responseSuccess = Boolean.parseBoolean(obj.get("success").toString());
        /*
        Sometimes steam no longer has info on the Game Id e.g. 33910 ARMA II, this is probably because the devs of the games
        in question may have created a new steam product for the exact same game (demo perhaps?), so to avoid crashing if the game no longer
        has any details, we'll pass it through as a multiplayer game, better than excluding games that could be multiplayer
        */
        if (!responseSuccess) {
            return new GameData(Collections.singleton(new GameCategory(MULTIPLAYER_ID)));
        }
        obj = obj.getAsJsonObject("data");
        return gson.fromJson(obj.toString(), GameData.class);
    }

    /**
     * Parses a response string to JSON
     * @param stringToParse the string to parse
     * @return JSON representation of the string
     * @throws IOException if an error occurs while serializing the string into JSON
     */
    public JsonElement parseResponseStringToJson(String stringToParse) throws IOException {
        try {
            return JsonParser.parseString(stringToParse);

        } catch (JsonSyntaxException e) {
            throw new IOException("Error when parsing response string into JSON object, this is likely due an invalid user id", e);
        }
    }

    /**
     * Retrieves the Steam API key from AWS secrets manager
     * @return the Steam API key stored within AWS secrets manager
     * @throws SecretRetrievalException if an exception occurs trying to retrieve the Steam API key from AWS secrets manager
     */
    private String getSteamApiKey() throws SecretRetrievalException {
        try(SecretsManagerClient secretsManagerClient = SecretManagerUtil.createSecretManagerClient()){
            return SecretManagerUtil.getSecretValue(secretsManagerClient,STEAM_API_KEY_NAME);
        }
        catch (Exception e){
            throw new SecretRetrievalException("Exception occurred when attempting to retrieve Steam API Key from AWS secrets manager",e);
        }
    }

}

