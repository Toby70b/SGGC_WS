package com.sggc.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.constants.SteamWebTestConstants;
import com.sggc.models.Game;
import com.sggc.repositories.GameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceIT extends AbstractIntegrationTest {

    @Autowired
    public GameService gameService;

    @Autowired
    public GameRepository gameRepository;

    @Nested
    @DisplayName("If provided with a list of Game app ids then the service will return all Games with matching app ids persisted within the database")
    class FindGamesByAppIdTests {

        @Test
        void ifProvidedWithAListOfGameAppIdsThenTheServiceWillReturnAllMatchingAppIdsPersistedWithinTheDatabase() {
            Game game1 = new Game();
            Game game2 = new Game();
            Game game3 = new Game();

            game1.setId("1");
            game2.setId("2");
            game2.setId("3");

            game1.setAppid("1080");
            game2.setAppid("2080");
            game3.setAppid("3080");

            game1.setName("Game 1");
            game2.setName("Game 2");
            game3.setName("Game 3");

            game1.setMultiplayer(true);
            game2.setMultiplayer(false);
            game3.setMultiplayer(false);

            gameRepository.save(game1);
            gameRepository.save(game2);
            gameRepository.save(game3);

            Set<Game> result = gameService.findGamesById(Set.of("1080", "2080", "3080"), false);

            assertEquals(Set.of(game1, game2, game3), result);
        }

        @Test
        @DisplayName("If multiplayer-only games are requested the service will exclude any non-multiplayer games from the returned list")
        void ifMultiplayerOnlyGamesAreRequestedTheServiceWillExcludeAnyNonMultiplayerGamesFromTheReturnedList() {
            Game game1 = new Game();
            Game game2 = new Game();
            Game game3 = new Game();

            game1.setId("1");
            game2.setId("2");
            game2.setId("3");

            game1.setAppid("1080");
            game2.setAppid("2080");
            game3.setAppid("3080");

            game1.setName("Game 1");
            game2.setName("Game 2");
            game3.setName("Game 3");

            game1.setMultiplayer(true);
            game2.setMultiplayer(false);
            game3.setMultiplayer(false);

            gameRepository.save(game1);
            gameRepository.save(game2);
            gameRepository.save(game3);

            Set<Game> result = gameService.findGamesById(Set.of("1080", "2080", "3080"), true);

            assertEquals(Set.of(game1), result);
        }
    }

    @Nested
    @DisplayName("If a Game's multiplayer status is currently unknown it should be requested via the Steam Store API and persisted within the database")
    class PersistGamesMultiplayerStatusTests {

        @Test
        void IfAGamesMultiplayerStatusIsUnknownItShouldBeRequestedViaTheSteamStoreAPIAndPersistedWithinTheDatabase() {
            Game game1 = new Game();
            Game game2 = new Game();

            game1.setId("1");
            game2.setId("2");

            game1.setAppid("1080");
            game2.setAppid("2080");

            game1.setName("Game 1");
            game2.setName("Game 2");

            game1.setMultiplayer(null);
            game2.setMultiplayer(null);

            gameRepository.save(game1);
            gameRepository.save(game2);

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_APP_DETAILS_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_APP_IDS_QUERY_PARAM_KEY, equalTo("1080"))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-store/get-app-details/successful-response-multiplayer-game-1.json")
                            ).build()
            );

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_APP_DETAILS_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_APP_IDS_QUERY_PARAM_KEY, equalTo("2080"))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-store/get-app-details/successful-response-single-player-game-1.json")
                            ).build()
            );

            gameService.findGamesById(Set.of("1080", "2080"), true);

            List<Game> persistedGames = new ArrayList<>();
            gameRepository.findAll().forEach(persistedGames::add);
            assertEquals(2, persistedGames.size());

            Game persistedGame1 = gameRepository.findGameByAppid("1080");
            Game persistedGame2 = gameRepository.findGameByAppid("2080");

            assertNotNull(persistedGame1);
            assertNotNull(persistedGame2);

            assertTrue(persistedGame1.getMultiplayer());
            assertFalse(persistedGame2.getMultiplayer());
        }

        @Test
        @DisplayName("If a Game's multiplayer status is not available via the Steam Store API it should be treated as multiplayer")
        void IfAGamesMultiplayerStatusIsNotAvailableViaTheSteamStoreApiItShouldBeTreatedAsMultiplayer() {
            Game game1 = new Game();
            game1.setId("1");
            game1.setAppid("1080");
            game1.setName("Game 1");
            game1.setMultiplayer(null);
            gameRepository.save(game1);

            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.GET_APP_DETAILS_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_APP_IDS_QUERY_PARAM_KEY, equalTo("1080"))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-store/get-app-details/unsuccessful-response-1.json")
                            ).build()
            );

            gameService.findGamesById(Set.of("1080"), true);

            List<Game> persistedGames = new ArrayList<>();
            gameRepository.findAll().forEach(persistedGames::add);
            assertEquals(1, persistedGames.size());

            Game persistedGame1 = gameRepository.findGameByAppid("1080");
            assertNotNull(persistedGame1);
            assertTrue(persistedGame1.getMultiplayer());
        }

    }

}
