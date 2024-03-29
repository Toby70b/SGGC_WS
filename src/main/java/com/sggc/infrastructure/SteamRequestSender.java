package com.sggc.infrastructure;

import com.google.gson.*;
import com.sggc.config.SteamProperties;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.models.SteamGameCategory;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

/**
 * Represents an interface for communicating with the Steam API
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class SteamRequestSender {
    public static final String STEAM_API_KEY_MASK = "*************";

    public static final String STEAM_KEY_QUERY_PARAM_KEY = "key";
    public static final String STEAM_ID_QUERY_PARAM_KEY = "steamid";
    public static final String STEAM_APP_IDS_QUERY_PARAM_KEY = "appids";
    public static final String VANITY_URL_QUERY_PARAM_KEY = "vanityurl";

    public static final String STEAM_API_KEY_SECRET_ID = "SteamAPIKey";
    public static final String GET_OWNED_GAMES_ENDPOINT = "/IPlayerService/GetOwnedGames/v1/";
    public static final String RESOLVE_VANITY_URL_ENDPOINT = "/ISteamUser/ResolveVanityURL/v1/";
    public static final String GET_APP_DETAILS_ENDPOINT = "/api/appdetails/";

    private final RestTemplate restTemplate;
    private final AwsSecretRetriever secretManagerService;

    private final SteamProperties steamProperties;

    /**
     * Retrieves the games owned by a specified user via the Steam API
     *
     * @param userId the id of the user whose owned games will be retrieved
     * @return the response from the Steam API parsed into a {@link GetOwnedGamesResponse} object
     * @throws SecretRetrievalException if an error occurs retrieving the Steam API key secret from AWS secrets manager
     */
    public GetOwnedGamesResponse requestUsersOwnedGamesFromSteamApi(String userId) throws SecretRetrievalException {
        URI requestUri = steamApiRequest(GET_OWNED_GAMES_ENDPOINT)
                .queryParam(STEAM_ID_QUERY_PARAM_KEY, userId)
                .build()
                .toUri();

        log.debug("Contacting [{}] to get owned games of user [{}].", sanitizeRequestUri(requestUri), userId);
        return restTemplate.getForObject(requestUri, GetOwnedGamesResponse.class);
    }

    /**
     * Retrieves the details of a specific game's details via the Steam Store's API
     *
     * @param appId the appid of the game whose details are being requested
     * @return a GameData object parsed from the response from the Steam API containing the details of the specified app
     */
    public GameData requestAppDetailsFromSteamApi(String appId) throws IOException {
        //TODO abstract the URI creation into a method
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(steamProperties.getStoreAddress() + GET_APP_DETAILS_ENDPOINT);
        URI requestUri = uriComponentsBuilder
                .queryParam(STEAM_APP_IDS_QUERY_PARAM_KEY, appId)
                .build()
                .toUri();

        log.debug("Contacting [{}] to get details of game [{}].", requestUri, appId);
        String response = restTemplate.getForObject(requestUri, String.class);
        return parseGameDetailsList(response);
    }

    /**
     * Sends a request to the Steam API's ResolveVanityURL endpoint to retrieve a vanity URL's Steam user id equivalent
     *
     * @param vanityUrl the vanity URL to resolve
     * @return a response from the Steam API containing the resolved Steam user id
     */
    public ResolveVanityUrlResponse resolveVanityUrl(String vanityUrl) throws SecretRetrievalException {
        URI requestUri = steamApiRequest(RESOLVE_VANITY_URL_ENDPOINT)
                .queryParam(VANITY_URL_QUERY_PARAM_KEY, vanityUrl)
                .build()
                .toUri();

        log.debug("Contacting [{}] to resolve vanity URL [{}].", sanitizeRequestUri(requestUri), vanityUrl);
        return restTemplate.getForObject(requestUri, ResolveVanityUrlResponse.class);
    }

    /**
     * Parses the game details list from Steam GetAppDetails endpoint into a model object
     *
     * @param stringToParse the string to parse
     * @return a {@link GameData} object serialized from the response from the Steam API
     * @throws IOException if an error occurs while serializing the string into JSON
     */
    private GameData parseGameDetailsList(String stringToParse) throws IOException {
        JsonObject rootObject = getAppDetailsResponseRootObject(stringToParse);
        boolean responseSuccess = isAppDetailsResponseSuccessful(rootObject);
        /*
        Sometimes steam no longer has info on the Game Id e.g. 33910 ARMA II, this is probably because the devs of the games
        in question may have created a new steam product for the exact same game (demo perhaps?), so to avoid crashing if the game no longer
        has any details, we'll pass it through as a multiplayer game. Which is better than excluding games that could be multiplayer
        */
        if (!responseSuccess) {
            log.debug("Could not determine whether game was multiplayer. Will be treated as multiplayer.");
            return new GameData(Collections.singleton(new GameCategory(SteamGameCategory.MULTIPLAYER)));
        } else  {
            if (doesGameHaveCategories(rootObject)){
                log.debug("Could not determine whether game was multiplayer. Will be treated as multiplayer.");
                return new GameData(Collections.singleton(new GameCategory(SteamGameCategory.MULTIPLAYER)));
            }
        }
        String dataField = "data";
        rootObject = rootObject.getAsJsonObject(dataField);
        Gson gson = new Gson();
        return gson.fromJson(rootObject.toString(), GameData.class);
    }

    /**
     * Returns the 'root' object from the Steam store's Get App Details endpoint.
     * @param jsonString string representing the response from the endpoint.
     *
     * @return a Json object containing the details the Steam store contains on a game.
     * @throws IOException if an error occurs while serializing the string into JSON
     */
    public JsonObject getAppDetailsResponseRootObject(String jsonString) throws IOException {
        JsonElement jsonTree = parseResponseStringToJson(jsonString);
        JsonObject obj = jsonTree.getAsJsonObject();
        // The root of the response is an id of the game thus get the responses root value
        String gameId = obj.keySet().iterator().next();
        return obj.getAsJsonObject(gameId);
    }

    /**
     * Determines whether the response from the Steam store's Get App Details endpoint was successful.
     *
     * @param obj the response from the HTTP request deserialized into a {@link JsonObject} for easier parsing.
     * @return a Json representation of the categories field in the response.
     */
    private boolean isAppDetailsResponseSuccessful(JsonObject obj) {
        String successFieldKey = "success";
        return Boolean.parseBoolean(obj.get(successFieldKey).toString());
    }

    /**
     * Determines whether the Steam store's details for a game includes any categories.
     *
     * @param obj the response from the HTTP request deserialized into a {@link JsonObject} for easier parsing.
     * @return a Json representation of the categories field in the response.
     */
    private boolean doesGameHaveCategories(JsonObject obj) {
        String dataFieldKey = "data";
        String gameCategoriesFieldKey = "categories";
        JsonElement gameCategories = obj.get(dataFieldKey).getAsJsonObject().get(gameCategoriesFieldKey);
        return gameCategories == null || gameCategories.toString().trim().isEmpty();
    }

    /**
     * Parses a response string to JSON
     *
     * @param stringToParse the string to parse
     * @return JSON representation of the string
     * @throws IOException if an error occurs while serializing the string into JSON
     */
    private JsonElement parseResponseStringToJson(String stringToParse) throws IOException {
        try {
            return JsonParser.parseString(stringToParse);

        } catch (JsonSyntaxException e) {
            throw new IOException("Error when parsing response string into JSON object", e);
        }
    }

    /**
     * Retrieves a Steam API key from AWS secrets manager
     *
     * @return a Steam API key stored within AWS secrets manager
     */

    private String getSteamApiKey() throws SecretRetrievalException {
        return secretManagerService.getSecretValue(STEAM_API_KEY_SECRET_ID);
    }


    /**
     * Masks the Steam API key within the query params of a  request URI, used to prevent the key being logged.
     *
     * @param requestUri the request URI whose Steam API key should be masked
     * @return the request URI, now containing a masked Steam API key. If the steam id query param cannot be found
     * within the request URI, then the request URI is returned, unmodified
     */
    private String maskSteamApiKey(String requestUri) {
        String steamApiKey;
        int steamKeyIndex = requestUri.indexOf(STEAM_KEY_QUERY_PARAM_KEY);
        if (steamKeyIndex != -1) {
            final String steamApiKeyQueryParam = STEAM_KEY_QUERY_PARAM_KEY + "=";
            steamApiKey = requestUri.substring(steamKeyIndex).substring(steamApiKeyQueryParam.length(),
                    requestUri.substring(steamKeyIndex).indexOf("&"));
            return requestUri.replaceAll(steamApiKey, STEAM_API_KEY_MASK);
        } else {
            return requestUri;
        }
    }

    /**
     * Sanitizes the URI to the Steam API to prevent sensitive data from being logged
     *
     * @param requestUri the request URI to sanitize
     * @return a sanitized request URI that is safe to log
     */
    private String sanitizeRequestUri(URI requestUri) {
        String requestUriString = requestUri.toString();
        if (requestUriString.contains(STEAM_KEY_QUERY_PARAM_KEY)) {
            requestUriString = maskSteamApiKey(requestUriString);
        }
        return requestUriString;
    }

    /**
     * Entrypoint for constructing a request to the Steam API. Sets all properties for a successful request to the Steam API
     * @param endpoint the desired Steam API endpoint for the request to be built with
     * @return a builder object which can be chained to provide more properties that the request will be constructed with
     * @throws SecretRetrievalException if an error occurs retrieving the Steam API key secret from AWS secrets manager
     */
    private UriComponentsBuilder steamApiRequest(String endpoint) throws SecretRetrievalException {
        return UriComponentsBuilder.fromUriString(steamProperties.getApiAddress() + endpoint)
                .queryParam(STEAM_KEY_QUERY_PARAM_KEY, getSteamApiKey());
    }
}

