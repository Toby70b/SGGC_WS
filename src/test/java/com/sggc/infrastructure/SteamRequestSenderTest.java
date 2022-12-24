package com.sggc.infrastructure;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sggc.config.SteamProperties;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.models.SteamGameCategory;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static com.sggc.util.TestUtils.createExampleGame;
import static com.sggc.infrastructure.SteamRequestSender.STEAM_API_KEY_SECRET_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SteamRequestSenderTest {

    public static final String MOCK_API_ADDRESS = "mockApiAddress";
    public static final String MOCK_STORE_ADDRESS = "mockStoreAddress";
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AwsSecretRetriever secretManagerService;

    private SteamRequestSender steamRequestSender;

    @BeforeEach
    public void setup() {
        SteamProperties mockSteamProperties = new SteamProperties(MOCK_API_ADDRESS, MOCK_STORE_ADDRESS);
        steamRequestSender = new SteamRequestSender(restTemplate, secretManagerService, mockSteamProperties);
    }

    @Test
    @DisplayName("Given a valid user id, when a successful request is made to retrieve a user's owned games then the " +
            "service should return a parsed response")
    void givenAValidUserIdWhenASuccessfulRequestIsMadeToRetrieveAUsersOwnedGamesThenTheServiceShouldReturnAParsedResponse() throws SecretRetrievalException {
        URI mockURI = URI.create(MOCK_API_ADDRESS + "/IPlayerService/GetOwnedGames/v1/?key=SomeKey&steamid=12345678910");

        GetOwnedGamesResponse mockResponse = new GetOwnedGamesResponse();
        GetOwnedGamesResponse.Response mockResponseDetails = new GetOwnedGamesResponse.Response();
        mockResponseDetails.setGameCount(1);
        mockResponseDetails.setGames(Set.of(createExampleGame("1", true, "someGame")));
        mockResponse.setResponse(mockResponseDetails);

        when(restTemplate.getForObject(mockURI, GetOwnedGamesResponse.class)).thenReturn(mockResponse);
        when(secretManagerService.getSecretValue(STEAM_API_KEY_SECRET_ID)).thenReturn("SomeKey");

        assertEquals(mockResponse, steamRequestSender.requestUsersOwnedGamesFromSteamApi("12345678910"));
    }

    @Nested
    @DisplayName("Retrieve application details tests")
    class RetrieveApplicationDetails {

        @Test
        @DisplayName("Given a application id which doesnt exist, when a successful request is made to retrieve an " +
                "application's details then the service should return a parsed response")
        void givenAValidApplicationIdWhenASuccessfulRequestIsMadeToRetrieveAnApplicationDetailsThenTheServiceShouldReturnAParsedResponse() throws IOException {
            URI mockURI = URI.create(MOCK_STORE_ADDRESS + "/api/appdetails/?appids=SomeAppId");
            String mockResponseJson = "{\n" +
                    "  \"SomeAppId\": {\n" +
                    "    \"success\": true,\n" +
                    "    \"data\": {\n" +
                    "      \"categories\": [\n" +
                    "        {\n" +
                    "          \"id\": 2,\n" +
                    "          \"description\": \"Single-player\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 1,\n" +
                    "          \"description\": \"Multi-player\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 9,\n" +
                    "          \"description\": \"Co-op\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"id\": 38,\n" +
                    "          \"description\": \"Online Co-op\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            GameCategory multiplayerCategory = new GameCategory(SteamGameCategory.MULTIPLAYER);
            GameCategory singlePlayerCategory = new GameCategory(SteamGameCategory.SINGLE_PLAYER);
            GameCategory unknownCategory = new GameCategory(null);
            GameData expectedGameDataResponse =
                    new GameData(Set.of(singlePlayerCategory, multiplayerCategory, unknownCategory));

            when(restTemplate.getForObject(mockURI, String.class)).thenReturn(mockResponseJson);
            assertEquals(expectedGameDataResponse, steamRequestSender.requestAppDetailsFromSteamApi("SomeAppId"));
        }

        @Test
        @DisplayName("Given a valid application id which doesnt exist, when a successful request is made to retrieve an " +
                "application's details then the service assume the game is multiplayer")
        void givenAValidApplicationIdWhichDoesntExistWhenASuccessfulRequestIsMadeToRetrieveAnApplicationDetailsThenTheServiceShouldAssumeTheGameIsMultiplayer() throws SecretRetrievalException, IOException {
            URI mockURI = URI.create(MOCK_STORE_ADDRESS + "/api/appdetails/?appids=SomeAppId");
            String mockResponseJson = "{\n" +
                    "    \"SomeAppId\": {\n" +
                    "        \"success\": false\n" +
                    "    }\n" +
                    "}";
            GameData expectedGameDataResponse =
                    new GameData(Collections.singleton(new GameCategory(SteamGameCategory.MULTIPLAYER)));

            when(restTemplate.getForObject(mockURI, String.class)).thenReturn(mockResponseJson);
            assertEquals(expectedGameDataResponse, steamRequestSender.requestAppDetailsFromSteamApi("SomeAppId"));
        }

        @Test
        @DisplayName("Given invalid response json, when the service attempts to parse the response into an object " +
                "then an exception will be thrown ")
        void givenInvalidRepsonseJsonWhenTheServiceAttemptsToParseTheResponseIntoAnObjectThenAnExceptionWillBeThrown() throws SecretRetrievalException, IOException {
            URI mockURI = URI.create(MOCK_STORE_ADDRESS + "/api/appdetails/?appids=SomeAppId");
            String mockResponseJson = "{\n" +
                    "    \"SomeAppId\": {\n" +
                    "        \"success\" false\n" +
                    "    }\n" +
                    "}";

            when(restTemplate.getForObject(mockURI, String.class)).thenReturn(mockResponseJson);
            assertThrows(IOException.class, () -> steamRequestSender.requestAppDetailsFromSteamApi("SomeAppId"));
        }
    }

    @Test
    @DisplayName("Given a valid vanity url, when a successful request is made to resolve the vanity url then the service " +
            "should return a parsed response")
    void givenAValidVanityUrlWhenASuccessfulRequestIsMadeToResolveTheVanityUrlThenTheServiceShouldReturnAParsedResponse() throws SecretRetrievalException {
        URI mockURI = URI.create(MOCK_API_ADDRESS + "/ISteamUser/ResolveVanityURL/v1/?key=SomeKey&vanityurl=SomeVanityUrl");

        ResolveVanityUrlResponse mockResponse = new ResolveVanityUrlResponse();
        ResolveVanityUrlResponse.Response mockResponseDetails = new ResolveVanityUrlResponse.Response();
        mockResponseDetails.setSteamId("12345678910");
        mockResponseDetails.setSuccess(1);
        when(restTemplate.getForObject(mockURI, ResolveVanityUrlResponse.class)).thenReturn(mockResponse);
        when(secretManagerService.getSecretValue(STEAM_API_KEY_SECRET_ID)).thenReturn("SomeKey");

        assertEquals(mockResponse, steamRequestSender.resolveVanityUrl("SomeVanityUrl"));
    }

    @Nested
    @DisplayName("Mask Steam API key tests")
    class MaskSteamApiKey {
        Logger steamRequestServiceLogger;
        ListAppender<ILoggingEvent> listAppender;

        @BeforeEach
        void setup() {
            steamRequestServiceLogger = (Logger) LoggerFactory.getLogger(SteamRequestSender.class);
            listAppender = new ListAppender<>();
            listAppender.start();
            steamRequestServiceLogger.addAppender(listAppender);
        }

        @Test
        @DisplayName("Given a request to the Steam API when the request URI is logged then the the request's Steam API key is masked")
        void givenARequestToTheSteamApiWhenTheRequestUriIsLoggedThenTheRequestsSteamApiKeyIsMasked() throws SecretRetrievalException {
            URI mockURI = URI.create(MOCK_API_ADDRESS + "/IPlayerService/GetOwnedGames/v1/?key=SomeKey&steamid=SomeUserId");

            String expectedDebugMessage =
                    "Contacting [mockApiAddress/IPlayerService/GetOwnedGames/v1/?key=*************&steamid=SomeUserId] " +
                            "to get owned games of user [SomeUserId]";

            GetOwnedGamesResponse mockResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponse.Response mockResponseDetails = new GetOwnedGamesResponse.Response();
            mockResponseDetails.setGameCount(1);
            mockResponseDetails.setGames(Set.of(createExampleGame("1", true, "someGame")));
            mockResponse.setResponse(mockResponseDetails);

            when(restTemplate.getForObject(mockURI, GetOwnedGamesResponse.class)).thenReturn(mockResponse);
            when(secretManagerService.getSecretValue(STEAM_API_KEY_SECRET_ID)).thenReturn("SomeKey");
            steamRequestSender.requestUsersOwnedGamesFromSteamApi("SomeUserId");

            assertFalse(listAppender.list.isEmpty());
            assertEquals(Level.DEBUG, listAppender.list.get(0).getLevel());
            assertEquals(expectedDebugMessage, listAppender.list.get(0).getMessage());
        }
    }
}