package com.sggc.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.constants.SteamWebTestConstants;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.User;
import com.sggc.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.sggc.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class UserServiceIT extends AbstractIntegrationTest {


    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;

    @Test
    @DisplayName("If a user is not found in the DB, their details will be requested via the Steam API and persisted within the database")
    void IfAUserIsNotFoundInTheDbItsDetailsWillBeRequestedViaTheSteamApiAndPersistedWithinTheDatabase() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
        secretsManagerTestSupporter.createMockSteamApiKey();
        wiremockClient.register(
                WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                        .withQueryParam(SteamWebTestConstants.STEAM_ID_QUERY_PARAM_KEY, equalTo("7656119804520628"))
                        .withQueryParam(SteamWebTestConstants.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                        .willReturn(ok()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                .withBodyFile("steam-api/get-owned-games/successful-get-owned-games-response-single-game-1.json")
                        ).build()
        );

        wiremockClient.register(
                WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_OWNED_GAMES_ENDPOINT))
                        .withQueryParam(SteamWebTestConstants.STEAM_ID_QUERY_PARAM_KEY, equalTo("7656119804520626"))
                        .withQueryParam(SteamWebTestConstants.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                        .willReturn(ok()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                .withBodyFile("steam-api/get-owned-games/successful-get-owned-games-response-single-game-1.json")
                        ).build()
        );

        String getOwnedGamesResponseMockGameId = "3830";
        userService.getIdsOfGamesOwnedByAllUsers(Set.of("7656119804520628", "7656119804520626"));

        List<User> persistedUsers = new ArrayList<>();
        userRepository.findAll().forEach(persistedUsers::add);
        assertEquals(2, persistedUsers.size());

        Optional<User> user1 = userRepository.findById("7656119804520628");
        Optional<User> user2 = userRepository.findById("7656119804520626");

        assertTrue(user1.isPresent());
        assertTrue(user2.isPresent());

        assertEquals(1, user1.get().getOwnedGameIds().size());
        assertEquals(1, user1.get().getOwnedGameIds().size());

        assertTrue(user1.get().getOwnedGameIds().contains(getOwnedGamesResponseMockGameId));
        assertTrue(user2.get().getOwnedGameIds().contains(getOwnedGamesResponseMockGameId));
    }

    @Test
    @DisplayName("If a request to retrieve all games owned by all users is received then a list of common games should be returned")
    void IfARequestToRetrieveAllGamesOwnedByAllUsersIsReceivedThenAListOfCommonGamesShouldBeReturned() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        user1.setId("7656119804520628");
        user1.setOwnedGameIds(Set.of("1189", "6147", "8888"));

        user2.setId("7656119804520626");
        user2.setOwnedGameIds(Set.of("1182", "6147", "8888", "3789"));

        user3.setId("7656119804520618");
        user3.setOwnedGameIds(Set.of("6147", "8888"));

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Set<String> commonGames = userService.getIdsOfGamesOwnedByAllUsers(Set.of("7656119804520628", "7656119804520626", "7656119804520618"));
        assertEquals(Set.of("6147", "8888"), commonGames);
    }
}
