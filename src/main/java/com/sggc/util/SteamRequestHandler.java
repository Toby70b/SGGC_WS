package com.sggc.util;

import com.google.gson.*;
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

import java.io.IOException;
import java.util.Collections;

/**
 * Class representing an interface for communicating with the Steam API
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class SteamRequestHandler {

    private final RestTemplate restTemplate;

    public static final String GET_OWNED_GAMES_ENDPOINT = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/";
    public static final String GET_APP_DETAILS_ENDPOINT = "https://store.steampowered.com/api/appdetails/";
    public static final String RESOLVE_VANITY_URL_ENDPOINT = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";

    /**
     * Sends a request to the Steam API's GetOwnedGames endpoint to retrieve the details of a specific game
     *
     * @param userId the user id whose being queries
     * @return the response from the Steam API parsed into a {@link GetOwnedGamesResponse} object
     * @throws SecretRetrievalException if an error occurs retrieving the Steam API key secret from AWS secrets manager
     */
    public GetOwnedGamesResponse requestUsersOwnedGamesFromSteamApi(String userId) throws SecretRetrievalException {
        String requestUri = GET_OWNED_GAMES_ENDPOINT + "?key=" + getSteamApiKey() + "&steamid=" + userId;
        log.debug("Contacting [{}] to get owned games of user [{}]",requestUri,userId);
        return restTemplate.getForObject(requestUri, GetOwnedGamesResponse.class);
    }

    /**
     * Sends a request to the Steam API's GetAppDetails endpoint to retrieve the details of a specific game
     *
     * @param appId the appid of the game whose details are being requested
     * @return a GameData object parsed from the response from the Steam API containing the details of the specified app
     */
    public GameData requestAppDetailsFromSteamApi(String appId) throws IOException {
        String requestUri = GET_APP_DETAILS_ENDPOINT + "?appids=" + appId;
        log.debug("Contacting [{}] to get details of game [{}]",requestUri,appId);
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
        String requestUri = RESOLVE_VANITY_URL_ENDPOINT + "?key=" + getSteamApiKey() + "&vanityurl=" + vanityUrl;
        log.debug("Contacting [{}] to resolve vanity URL [{}]" ,requestUri,vanityUrl);
        return restTemplate.getForObject(requestUri, ResolveVanityUrlResponse.class);
    }

    /**
     * Parses the game details list from Steam GetAppDetails endpoint into a model object
     *
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
        String successField = "success";
        boolean responseSuccess = Boolean.parseBoolean(obj.get(successField).toString());
        /*
        Sometimes steam no longer has info on the Game Id e.g. 33910 ARMA II, this is probably because the devs of the games
        in question may have created a new steam product for the exact same game (demo perhaps?), so to avoid crashing if the game no longer
        has any details, we'll pass it through as a multiplayer game, better than excluding games that could be multiplayer
        */
        if (!responseSuccess) {
            log.debug("Could not determine whether game was multiplayer. Will be treated as multiplayer.");
            return new GameData(Collections.singleton(new GameCategory(SteamGameCategory.MULTIPLAYER)));
        }
        String dataField = "data";
        obj = obj.getAsJsonObject(dataField);
        return gson.fromJson(obj.toString(), GameData.class);
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
            throw new IOException("Error when parsing response string into JSON object, this is likely due an invalid user id", e);
        }
    }

    /**
     * Retrieves the Steam API key from AWS secrets manager
     *
     * @return the Steam API key stored within AWS secrets manager
     * @throws SecretRetrievalException if an exception occurs trying to retrieve the Steam API key from AWS secrets manager
     */
    private String getSteamApiKey() throws SecretRetrievalException {
        return SteamKeyRetriever.getInstance().getSteamKey();
    }


}

