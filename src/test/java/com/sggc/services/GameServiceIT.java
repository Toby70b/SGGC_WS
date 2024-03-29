package com.sggc.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.models.Game;
import com.sggc.repositories.GameRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import util.constants.SteamWebTestConstants;
import util.extentions.SggcLocalDynamoDbCleanerExtension;
import util.extentions.WiremockCleanerExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceIT extends AbstractIntegrationTest {

    @RegisterExtension
    SggcLocalDynamoDbCleanerExtension dynamoDbCleanerExtension = new SggcLocalDynamoDbCleanerExtension(sggcDynamoDbContainer.getFirstMappedPort());

    @RegisterExtension
    WiremockCleanerExtension wiremockCleanerExtension = new WiremockCleanerExtension(wiremockContainer.getFirstMappedPort());

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;


    private static WireMock wiremockClient;

    @BeforeAll
    static void beforeAll() {
        wiremockClient = new WireMock("localhost", wiremockContainer.getFirstMappedPort());
    }


    @Nested
    @DisplayName("Given a collection of game IDs, when the service retrieves the corresponding games then it will return the games persisted within the database.")
    class FindGamesByAppIdTests {

        @Test
        @DisplayName("If multiplayer status does not matter then return all games with matching IDs persisted within the database.")
        void ifProvidedWithAListOfGameAppIdsThenTheServiceWillReturnAllMatchingAppIdsPersistedWithinTheDatabase() {
            String mockGameId1 = "1080";
            String mockGameId2 = "2080";
            String mockGameId3 = "3080";

            Game game1 = new Game();
            Game game2 = new Game();
            Game game3 = new Game();

            game1.setId("1");
            game2.setId("2");
            game2.setId("3");

            game1.setAppid(mockGameId1);
            game2.setAppid(mockGameId2);
            game3.setAppid(mockGameId3);

            game1.setName("Game 1");
            game2.setName("Game 2");
            game3.setName("Game 3");

            game1.setMultiplayer(true);
            game2.setMultiplayer(false);
            game3.setMultiplayer(false);

            gameRepository.save(game1);
            gameRepository.save(game2);
            gameRepository.save(game3);

            Set<Game> result = gameService.findGamesById(Set.of(mockGameId1, mockGameId2, mockGameId3), false);

            assertEquals(Set.of(game1, game2, game3), result);
        }

        @Test
        @DisplayName("If multiplayer-only games are requested then exclude any non-multiplayer games from the returned list.")
        void ifMultiplayerOnlyGamesAreRequestedTheServiceWillExcludeAnyNonMultiplayerGamesFromTheReturnedList() {
            String mockGameId1 = "1080";
            String mockGameId2 = "2080";
            String mockGameId3 = "3080";

            Game game1 = new Game();
            Game game2 = new Game();
            Game game3 = new Game();

            game1.setId("1");
            game2.setId("2");
            game2.setId("3");

            game1.setAppid(mockGameId1);
            game2.setAppid(mockGameId2);
            game3.setAppid(mockGameId3);

            game1.setName("Game 1");
            game2.setName("Game 2");
            game3.setName("Game 3");

            game1.setMultiplayer(true);
            game2.setMultiplayer(false);
            game3.setMultiplayer(false);

            gameRepository.save(game1);
            gameRepository.save(game2);
            gameRepository.save(game3);

            Set<Game> result = gameService.findGamesById(Set.of(mockGameId1, mockGameId2, mockGameId3), true);

            assertEquals(Set.of(game1), result);
        }
    }

    @Nested
    @DisplayName("Given a request for multiplayer-only games, when a game's multiplayer status is currently unknown, then " +
            "the service will retrieve it via the Steam Store API and persist within the database.")
    class PersistGamesMultiplayerStatusTests {

        @Test
        @DisplayName("The game's multiplayer status is available via the Steam Store API.")
        void IfAGamesMultiplayerStatusIsUnknownItShouldBeRequestedViaTheSteamStoreAPIAndPersistedWithinTheDatabase() {
            String mockGameId1 = "1080";
            String mockGameId2 = "2080";

            Game game1 = new Game();
            Game game2 = new Game();

            game1.setId("1");
            game2.setId("2");

            game1.setAppid(mockGameId1);
            game2.setAppid(mockGameId2);

            game1.setName("Game 1");
            game2.setName("Game 2");

            game1.setMultiplayer(null);
            game2.setMultiplayer(null);

            gameRepository.save(game1);
            gameRepository.save(game2);

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_APP_DETAILS_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_APP_IDS_QUERY_PARAM_KEY, equalTo(mockGameId1))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-store/get-app-details/successful-response-multiplayer-game-1.json")
                            ).build()
            );

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_APP_DETAILS_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_APP_IDS_QUERY_PARAM_KEY, equalTo(mockGameId2))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-store/get-app-details/successful-response-single-player-game-1.json")
                            ).build()
            );

            gameService.findGamesById(Set.of(mockGameId1, mockGameId2), true);

            List<Game> persistedGames = new ArrayList<>();
            gameRepository.findAll().forEach(persistedGames::add);
            assertEquals(2, persistedGames.size());

            Game persistedGame1 = gameRepository.findGameByAppid(mockGameId1);
            Game persistedGame2 = gameRepository.findGameByAppid(mockGameId2);

            assertNotNull(persistedGame1);
            assertNotNull(persistedGame2);

            assertTrue(persistedGame1.getMultiplayer());
            assertFalse(persistedGame2.getMultiplayer());
        }

        @Test
        @DisplayName("The game's multiplayer status is not available via the Steam Store API. It should be treated as multiplayer.")
        void IfAGamesMultiplayerStatusIsNotAvailableViaTheSteamStoreApiItShouldBeTreatedAsMultiplayer() {
            String mockGameId1 = "1080";

            Game game1 = new Game();
            game1.setId("1");
            game1.setAppid(mockGameId1);
            game1.setName("Game 1");
            game1.setMultiplayer(null);
            gameRepository.save(game1);

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_APP_DETAILS_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_APP_IDS_QUERY_PARAM_KEY, equalTo(mockGameId1))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-store/get-app-details/unsuccessful-response-1.json")
                            ).build()
            );

            gameService.findGamesById(Set.of(mockGameId1), true);

            List<Game> persistedGames = new ArrayList<>();
            gameRepository.findAll().forEach(persistedGames::add);
            assertEquals(1, persistedGames.size());

            Game persistedGame1 = gameRepository.findGameByAppid(mockGameId1);
            assertNotNull(persistedGame1);
            assertTrue(persistedGame1.getMultiplayer());
        }
    }
}
