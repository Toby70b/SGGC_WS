package com.sggc;

/**
 * Represents a class used to
 */
public class TestSteamWebConstants {

    private TestSteamWebConstants() {
    }

    public final static String STEAM_ID_QUERY_PARAM_KEY = "steamid";
    public final static String STEAM_KEY_QUERY_PARAM_KEY = "key";

    public static class Endpoints {
        private Endpoints() {
        }

        public final static String GET_OWNED_GAMES_ENDPOINT = "/IPlayerService/GetOwnedGames/v1/";
    }
}
