package com.sggc.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.constants.SteamWebTestConstants;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.extentions.SggcLocalDynamoDbCleanerExtension;
import com.sggc.extentions.SggcLocalStackCleanerExtension;
import com.sggc.extentions.WiremockCleanerExtension;
import com.sggc.models.User;
import com.sggc.repositories.UserRepository;
import com.sggc.util.AwsSecretsManagerTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import static com.sggc.containers.SggcLocalStackContainer.ENABLED_SERVICES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class UserServiceTtlIT extends AbstractIntegrationTest {

    @RegisterExtension
    SggcLocalDynamoDbCleanerExtension dynamoDbCleanerExtension
            = new SggcLocalDynamoDbCleanerExtension(sggcDynamoDbContainer.getFirstMappedPort());

    @RegisterExtension
    WiremockCleanerExtension wiremockCleanerExtension
            = new WiremockCleanerExtension(wiremockContainer.getFirstMappedPort());

    @RegisterExtension
    SggcLocalStackCleanerExtension localStackCleanerExtension
            = new SggcLocalStackCleanerExtension(localStackContainer.getFirstMappedPort(), List.of(ENABLED_SERVICES));

    WireMock wiremockClient = initializeWiremockClient();
    AWSSecretsManager secretsManagerClient = initializeAwsSecretsManagerClient();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private Clock clock;

    @Test
    @DisplayName("When a user is persisted in the database its TTL field will be populated with a date exactly 24 hours in the future")
    void WhenAUserIsPersistedInTheDatabaseItsTtlFieldWillBePopulatedWithADateExactly24HoursInTheFuture() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
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

        String mockDate = "2018-08-22T10:00:00Z";
        Clock fixedClock = Clock.fixed(Instant.parse(mockDate), ZoneOffset.UTC);
        when(clock.instant()).thenReturn(fixedClock.instant());
        Date oneDayInTheFuture = Date.from(clock.instant().plus(1, ChronoUnit.DAYS));

        userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockUserId1, mockUserId2));

        List<User> persistedUsers = new ArrayList<>();
        userRepository.findAll().forEach(persistedUsers::add);
        assertEquals(2, persistedUsers.size());

        Optional<User> user1 = userRepository.findById(mockUserId1);
        Optional<User> user2 = userRepository.findById(mockUserId2);

        assertTrue(user1.isPresent());
        assertTrue(user2.isPresent());

        assertEquals(oneDayInTheFuture, Date.from(Instant.ofEpochSecond(user1.get().getRemovalDate())));
        assertEquals(oneDayInTheFuture, Date.from(Instant.ofEpochSecond(user2.get().getRemovalDate())));
    }
}
