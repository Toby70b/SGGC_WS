package com.sggc.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.User;
import com.sggc.repositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import util.clientfactories.AWSSecretsManagerClientFactory;
import util.constants.SteamWebTestConstants;
import util.extentions.SggcLocalDynamoDbCleanerExtension;
import util.extentions.SggcLocalStackCleanerExtension;
import util.extentions.WiremockCleanerExtension;
import util.util.AwsSecretsManagerTestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;
import static util.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;

public class UserServiceIT extends AbstractIntegrationTest {

    @RegisterExtension
    SggcLocalDynamoDbCleanerExtension dynamoDbCleanerExtension = new SggcLocalDynamoDbCleanerExtension(sggcDynamoDbContainer.getFirstMappedPort());

    @RegisterExtension
    WiremockCleanerExtension wiremockCleanerExtension = new WiremockCleanerExtension(wiremockContainer.getFirstMappedPort());

    @RegisterExtension
    SggcLocalStackCleanerExtension localStackCleanerExtension
            = new SggcLocalStackCleanerExtension(localStackContainer.getFirstMappedPort(), List.of(SECRETSMANAGER));

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static WireMock wiremockClient;
    private static AWSSecretsManager secretsManagerClient;

    @BeforeAll
    static void beforeAll() {
        wiremockClient = new WireMock("localhost", wiremockContainer.getFirstMappedPort());
        secretsManagerClient = new AWSSecretsManagerClientFactory().createClient(localStackContainer.getFirstMappedPort());
    }

    @Nested
    @DisplayName("Given a request to retrieve users common games, when a user is not found in the DB, then the  service will" +
            " to retrieve their details via the Steam API and persist them within the database.")
    class PersistUsersOwnedGamesTests {

        @Test
        @DisplayName("User's details successfully retrieved from the Steam Store API.")
        void IfAUserIsNotFoundInTheDbItsDetailsWillBeRequestedViaTheSteamApiAndPersistedWithinTheDatabase() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
            AwsSecretsManagerTestUtil.createMockSteamApiKey(secretsManagerClient);

            String mockUserId1 = "7656119804520628";
            String mockUserId2 = "7656119804520626";

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_ID_QUERY_PARAM_KEY, equalTo(mockUserId1))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/get-owned-games/successful-response-single-game-1.json")
                            ).build()
            );

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_ID_QUERY_PARAM_KEY, equalTo(mockUserId2))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/get-owned-games/successful-response-single-game-1.json")
                            ).build()
            );

            String getOwnedGamesResponseMockGameId = "3830";
            userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockUserId1, mockUserId2));

            List<User> persistedUsers = new ArrayList<>();
            userRepository.findAll().forEach(persistedUsers::add);
            assertEquals(2, persistedUsers.size());

            Optional<User> user1 = userRepository.findById(mockUserId1);
            Optional<User> user2 = userRepository.findById(mockUserId2);

            assertTrue(user1.isPresent());
            assertTrue(user2.isPresent());

            assertEquals(1, user1.get().getOwnedGameIds().size());
            assertEquals(1, user1.get().getOwnedGameIds().size());

            assertTrue(user1.get().getOwnedGameIds().contains(getOwnedGamesResponseMockGameId));
            assertTrue(user2.get().getOwnedGameIds().contains(getOwnedGamesResponseMockGameId));
        }

        @Test
        @DisplayName("Retrieved user does ot own any games, an appropriate exception will be thrown.")
        void IfARetrievedUserDoesNotOwnAnyGamesThenAnAppropriateExceptionWillBeThrown() {
            AwsSecretsManagerTestUtil.createMockSteamApiKey(secretsManagerClient);

            String mockUserId1 = "7656119804520628";
            String mockUserId2 = "7656119804520626";

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_ID_QUERY_PARAM_KEY, equalTo(mockUserId1))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/get-owned-games/successful-response-no-games-1.json")
                            ).build()
            );

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_ID_QUERY_PARAM_KEY, equalTo(mockUserId2))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/get-owned-games/successful-response-single-game-1.json")
                            ).build()
            );

            UserHasNoGamesException expectedException =
                    assertThrows(UserHasNoGamesException.class, () ->
                            userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockUserId1, mockUserId2)));

            assertEquals(mockUserId1, expectedException.getUserId());
        }

        @Test
        @DisplayName("User's details unsuccessfully retrieved from the Steam Store API. An appropriate exception will be thrown.")
        void IfAnAttemptToRetrieveAUserIsUnsuccessfulDueToTheUserNotExistingThenAnAppropriateExceptionWillBeThrown() {
            AwsSecretsManagerTestUtil.createMockSteamApiKey(secretsManagerClient);

            String mockUserId1 = "7656119804520628";
            String mockUserId2 = "7656119804520626";

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_ID_QUERY_PARAM_KEY, equalTo(mockUserId1))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/get-owned-games/unsuccessful-response-1.json")
                            ).build()
            );

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_ID_QUERY_PARAM_KEY, equalTo(mockUserId2))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/get-owned-games/successful-response-single-game-1.json")
                            ).build()
            );

            UserHasNoGamesException expectedException =
                    assertThrows(UserHasNoGamesException.class, () ->
                            userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockUserId1, mockUserId2)));

            assertEquals(expectedException.getUserId(), mockUserId1);
        }
    }

    @Test
    @DisplayName("Given a request to retrieve users' common games, when the users' owned games have been retrieved, then a list of common games should be returned")
    void IfARequestToRetrieveAllGamesOwnedByAllUsersIsReceivedThenAListOfCommonGamesShouldBeReturned() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
        String mockUserId1 = "7656119804520628";
        String mockUserId2 = "7656119804520626";
        String mockUserId3 = "7656119804520618";

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        user1.setId(mockUserId1);
        user1.setOwnedGameIds(Set.of("1189", "6147", "8888"));

        user2.setId(mockUserId2);
        user2.setOwnedGameIds(Set.of("1182", "6147", "8888", "3789"));

        user3.setId(mockUserId3);
        user3.setOwnedGameIds(Set.of("6147", "8888"));

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Set<String> commonGames = userService.getIdsOfGamesOwnedByAllUsers(Set.of("7656119804520628", "7656119804520626", "7656119804520618"));
        assertEquals(Set.of("6147", "8888"), commonGames);
    }
}
