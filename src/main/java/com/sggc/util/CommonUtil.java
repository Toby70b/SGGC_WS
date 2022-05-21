package com.sggc.util;

/**
 * Utility class for common methods and constants
 */
//TODO: remove this class in a cleanup ticket
public class CommonUtil {
    public static final int MULTIPLAYER_ID = 1;
    public static final int SINGLEPLAYER_ID = 2;
    public static final String GET_OWNED_GAMES_ENDPOINT = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/";
    public static final String GET_APP_DETAILS_ENDPOINT = "https://store.steampowered.com/api/appdetails/";
    public static final String RESOLVE_VANITY_URL_ENDPOINT = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/";
}

