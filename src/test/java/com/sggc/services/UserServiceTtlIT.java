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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.sggc.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class UserServiceTtlIT extends AbstractIntegrationTest {

    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;

    @MockBean
    private Clock clock;

    @Test
    @DisplayName("When a user is persisted in the database its TTL field will be populated with a date exactly 24 hours in the future")
    void WhenAUserIsPersistedInTheDatabaseItsTtlFieldWillBePopulatedWithADateExactly24HoursInTheFuture() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
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

        Clock fixedClock = Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixedClock.instant());
        Date oneDayInTheFuture = Date.from(clock.instant().plus(1, ChronoUnit.DAYS));

        userService.getIdsOfGamesOwnedByAllUsers(Set.of("7656119804520628", "7656119804520626"));

        List<User> persistedUsers = new ArrayList<>();
        userRepository.findAll().forEach(persistedUsers::add);
        assertEquals(2, persistedUsers.size());

        Optional<User> user1 = userRepository.findById("7656119804520628");
        Optional<User> user2 = userRepository.findById("7656119804520626");

        assertTrue(user1.isPresent());
        assertTrue(user2.isPresent());

        assertEquals(oneDayInTheFuture, Date.from(Instant.ofEpochSecond(user1.get().getRemovalDate())));
        assertEquals(oneDayInTheFuture, Date.from(Instant.ofEpochSecond(user2.get().getRemovalDate())));
    }
}
