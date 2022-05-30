package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.models.SteamGameCategory;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static com.sggc.TestUtils.createExampleGame;
import static com.sggc.services.SteamRequestService.STEAM_API_KEY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SteamRequestServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AwsSecretManagerService secretManagerService;

    @InjectMocks
    private SteamRequestService steamRequestService;

    @Test
    @DisplayName("Given a valid user id, when a successful request is made to retrieve a user's owned games then the " +
            "service should return a parsed response")
    void givenAValidUserIdWhenASuccessfulRequestIsMadeToRetrieveAUsersOwnedGamesThenTheServiceShouldReturnAParsedResponse() throws SecretRetrievalException {
        URI mockURI = URI.create("https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=SomeKey&steamid=12345678910");

        GetOwnedGamesResponse mockResponse = new GetOwnedGamesResponse();
        GetOwnedGamesResponse.Response mockResponseDetails = new GetOwnedGamesResponse.Response();
        mockResponseDetails.setGameCount(1);
        mockResponseDetails.setGames(Set.of(createExampleGame("1", true, "someGame")));
        mockResponse.setResponse(mockResponseDetails);

        when(restTemplate.getForObject(mockURI, GetOwnedGamesResponse.class)).thenReturn(mockResponse);
        when(secretManagerService.getSecretValue(STEAM_API_KEY_NAME)).thenReturn("SomeKey");

        assertEquals(mockResponse, steamRequestService.requestUsersOwnedGamesFromSteamApi("12345678910"));
    }

    @Nested
    @DisplayName("Retrieve application details tests")
    class RetrieveApplicationDetails {

        @Test
        @DisplayName("Given a application id which doesnt exist, when a successful request is made to retrieve an " +
                "application's details then the service should return a parsed response")
        void givenAValidApplicationIdWhenASuccessfulRequestIsMadeToRetrieveAnApplicationDetailsThenTheServiceShouldReturnAParsedResponse() throws IOException {
            URI mockURI = URI.create("https://store.steampowered.com/api/appdetails/?appids=SomeAppId");
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
            assertEquals(expectedGameDataResponse, steamRequestService.requestAppDetailsFromSteamApi("SomeAppId"));
        }

        @Test
        @DisplayName("Given a valid application id which doesnt exist, when a successful request is made to retrieve an " +
                "application's details then the service assume the game is multiplayer")
        void givenAValidApplicationIdWhichDoesntExistWhenASuccessfulRequestIsMadeToRetrieveAnApplicationDetailsThenTheServiceShouldAssumeTheGameIsMultiplayer() throws SecretRetrievalException, IOException {
            URI mockURI = URI.create("https://store.steampowered.com/api/appdetails/?appids=SomeAppId");
            String mockResponseJson = "{\n" +
                    "    \"SomeAppId\": {\n" +
                    "        \"success\": false\n" +
                    "    }\n" +
                    "}";
            GameData expectedGameDataResponse =
                    new GameData(Collections.singleton(new GameCategory(SteamGameCategory.MULTIPLAYER)));

            when(restTemplate.getForObject(mockURI, String.class)).thenReturn(mockResponseJson);
            assertEquals(expectedGameDataResponse, steamRequestService.requestAppDetailsFromSteamApi("SomeAppId"));
        }

        @Test
        @DisplayName("Given invalid response json, when the service attempts to parse the response into an object " +
                "then an exception will be thrown ")
        void givenInvalidRepsonseJsonWhenTheServiceAttemptsToParseTheResponseIntoAnObjectThenAnExceptionWillBeThrown() throws SecretRetrievalException, IOException {
            URI mockURI = URI.create("https://store.steampowered.com/api/appdetails/?appids=SomeAppId");
            String mockResponseJson = "{\n" +
                    "    \"SomeAppId\": {\n" +
                    "        \"success\" false\n" +
                    "    }\n" +
                    "}";

            when(restTemplate.getForObject(mockURI, String.class)).thenReturn(mockResponseJson);
            assertThrows(IOException.class, ()->steamRequestService.requestAppDetailsFromSteamApi("SomeAppId"));
        }
    }

    @Test
    @DisplayName("Given a valid vanity url, when a successful request is made to resolve the vanity url then the service " +
            "should return a parsed response")
    void givenAValidVanityUrlWhenASuccessfulRequestIsMadeToResolveTheVanityUrlThenTheServiceShouldReturnAParsedResponse() throws SecretRetrievalException {
        URI mockURI = URI.create("https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=SomeKey&vanityurl=SomeVanityUrl");

        ResolveVanityUrlResponse mockResponse = new ResolveVanityUrlResponse();
        ResolveVanityUrlResponse.Response mockResponseDetails = new ResolveVanityUrlResponse.Response();
        mockResponseDetails.setSteamId("12345678910");
        mockResponseDetails.setSuccess(1);
        when(restTemplate.getForObject(mockURI, ResolveVanityUrlResponse.class)).thenReturn(mockResponse);
        when(secretManagerService.getSecretValue(STEAM_API_KEY_NAME)).thenReturn("SomeKey");

        assertEquals(mockResponse, steamRequestService.resolveVanityUrl("SomeVanityUrl"));
    }
}