package util.constants;

/**
 * Represents a class for constants related to the Steam Store and Web APIs. To be used for application tests.
 */
public class SteamWebTestConstants {

    private SteamWebTestConstants() {
    }

    public static class QueryParams {

        private QueryParams() {
        }

        public final static String STEAM_ID_QUERY_PARAM_KEY = "steamid";
        public final static String STEAM_KEY_QUERY_PARAM_KEY = "key";
        public final static String STEAM_APP_IDS_QUERY_PARAM_KEY = "appids";
        public static final String VANITY_URL_QUERY_PARAM_KEY = "vanityurl";
    }

    public static class Endpoints {
        private Endpoints() {
        }

        public final static String GET_OWNED_GAMES_ENDPOINT = "/IPlayerService/GetOwnedGames/v1/";
        public final static String GET_APP_DETAILS_ENDPOINT = "/api/appdetails/";
        public static final String RESOLVE_VANITY_URL_ENDPOINT = "/ISteamUser/ResolveVanityURL/v1/";
    }
}
