package com.sggc.services;

import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.User;
import com.sggc.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.sggc.services.SteamRequestService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceIT extends AbstractIntegrationTest {

    public static String STEAM_API_KEY_STRING = "DUMMY_STEAM_API_KEY";
    public static String CONTENT_TYPE_HEADER  = "Content-Type";

    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;

    @Test
    @DisplayName("If a user is not found in the DB, their details will be requested via the Steam API and persisted within the Database")
    void IfAUserIsNotFoundInTheDbItsDetailsWillBeRequestedViaTheSteamApiAndSavedToTheDb() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
        secretsManagerClient.createSecret(new CreateSecretRequest()
                .withName(STEAM_API_KEY_NAME)
                .withSecretString(STEAM_API_KEY_STRING)
        );

        wiremockClient.register(
                WireMock.get(urlPathEqualTo(GET_OWNED_GAMES_ENDPOINT))
                        .withQueryParam(STEAM_ID_QUERY_PARAM_KEY, equalTo("7656119804520628"))
                        .withQueryParam(STEAM_KEY_QUERY_PARAM_KEY, equalTo(STEAM_API_KEY_STRING))
                        .willReturn(ok()
                            .withHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON.toString())
                                .withBodyFile("steam-api/successful-get-owned-games-response-single-game-1.json")
                        ).build()
        );

        wiremockClient.register(
                WireMock.get(urlPathEqualTo(GET_OWNED_GAMES_ENDPOINT))
                        .withQueryParam(STEAM_ID_QUERY_PARAM_KEY, equalTo("7656119804520626"))
                        .withQueryParam(STEAM_KEY_QUERY_PARAM_KEY, equalTo(STEAM_API_KEY_STRING))
                        .willReturn(ok()
                                .withHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_JSON.toString())
                                .withBodyFile("steam-api/successful-get-owned-games-response-single-game-1.json")
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
}
