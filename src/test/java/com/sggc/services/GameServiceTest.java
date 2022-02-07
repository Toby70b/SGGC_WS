package com.sggc.services;

import com.sggc.models.Game;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.repositories.GameRepository;
import com.sggc.util.SteamRequestHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SteamRequestHandler steamRequestHandler;

    @InjectMocks
    private GameService gameService;

    @Test
    @DisplayName("If provided with a game id it will return the details of the game matching that id")
    void IfProvidedWithAGameIdItWillReturnTheDetailsOfTheGameMatchingThatId() {
        Game exampleGame = createExampleGame("12", true, "Some game name");
        when(gameRepository.findGameByAppid("12")).thenReturn(exampleGame);
        assertEquals(Set.of(exampleGame), gameService.getCommonGames(Set.of("12"), false));
    }

    @Test
    @DisplayName("If both multiplayer games and non-multiplayer are desired, then no games will be filtered out of the returned list of games")
    void IfBothMultiplayerAndNonMultiplayerGamesAreDesiredThenNoGamesWillBeFilteredOutOfTheReturnedListOfGames() {

        Game exampleGame1 = createExampleGame("12", false, "Some game name");
        Game exampleGame2 = createExampleGame("78", true, "Some other game name");
        Game exampleGame3 = createExampleGame("112", true, "yet another game name");

        when(gameRepository.findGameByAppid("12")).thenReturn(exampleGame1);
        when(gameRepository.findGameByAppid("78")).thenReturn(exampleGame2);
        when(gameRepository.findGameByAppid("112")).thenReturn(exampleGame3);
        assertEquals(Set.of(exampleGame1, exampleGame2, exampleGame3), gameService.getCommonGames(Set.of("12", "78", "112"), false));
    }

    @Nested
    @DisplayName("Multiplayer filter tests")
    class MultiplayerGameFilterTests {
        @Nested
        @DisplayName("If the games multiplayer status has already been persisted via the database")
        class KnownMultiplayerStatusTests {
            @Test
            @DisplayName("If only multiplayer games are desired, then non-multiplayer games will be filtered out of the returned list of games")
            void IfOnlyMultiplayerGamesAreDesiredThenNonMultiplayerGamesWillBeFilteredOutOfTheReturnedListOfGames() {

                Game exampleGame1 = createExampleGame("12", false, "Some game name");
                Game exampleGame2 = createExampleGame("78", true, "Some other game name");
                Game exampleGame3 = createExampleGame("112", true, "yet another game name");

                when(gameRepository.findGameByAppid("12")).thenReturn(exampleGame1);
                when(gameRepository.findGameByAppid("78")).thenReturn(exampleGame2);
                when(gameRepository.findGameByAppid("112")).thenReturn(exampleGame3);
                assertEquals(Set.of(exampleGame2, exampleGame3), gameService.getCommonGames(Set.of("12", "78", "112"), true));
            }
        }

        @Nested
        @DisplayName("If the games multiplayer status is unknown")
        class UnknownMultiplayerStatusTests {
            @Test
            @DisplayName("If only multiplayer games are desired, then non-multiplayer games will be filtered out of the returned list of games")
            void IfOnlyMultiplayerGamesAreDesiredThenNonMultiplayerGamesWillBeFilteredOutOfTheReturnedListOfGames() throws IOException {

                String multiplayerAppDetailsResponseExampleJson1 = "{\n" +
                        "  \"573100\": {\n" +
                        "    \"success\": true,\n" +
                        "    \"categories\": [\n" +
                        "      {\n" +
                        "        \"id\": 2,\n" +
                        "        \"description\": \"Single-player\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": 1,\n" +
                        "        \"description\": \"Multi-player\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}";
                String multiplayerAppDetailsResponseExampleJson2 = "{\n" +
                        "  \"573101\": {\n" +
                        "    \"success\": true,\n" +
                        "    \"categories\": [\n" +
                        "      {\n" +
                        "        \"id\": 2,\n" +
                        "        \"description\": \"Single-player\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": 1,\n" +
                        "        \"description\": \"Multi-player\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}";

                String singlePlayerAppDetailsResponseExampleJson = "{\n" +
                        "  \"573100\": {\n" +
                        "    \"success\": true,\n" +
                        "    \"categories\": [\n" +
                        "      {\n" +
                        "        \"id\": 2,\n" +
                        "        \"description\": \"Single-player\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}";

                Game exampleGame1 = createExampleGame("573100", null, "Some game name");
                Game exampleGame2 = createExampleGame("573101", null, "Some game name");
                Game exampleGame3 = createExampleGame("573102", null, "Some game name");
                when(gameRepository.findGameByAppid("573100")).thenReturn(exampleGame1);
                when(gameRepository.findGameByAppid("573101")).thenReturn(exampleGame2);
                when(gameRepository.findGameByAppid("573102")).thenReturn(exampleGame3);

                when(steamRequestHandler.requestAppDetailsFromSteamApi("573100")).thenReturn(multiplayerAppDetailsResponseExampleJson1);
                when(steamRequestHandler.requestAppDetailsFromSteamApi("573101")).thenReturn(singlePlayerAppDetailsResponseExampleJson);
                when(steamRequestHandler.requestAppDetailsFromSteamApi("573102")).thenReturn(multiplayerAppDetailsResponseExampleJson2);

                GameData multiplayerGameData1 =  new GameData(Set.of(new GameCategory(1)));
                GameData multiplayerGameData2 =  new GameData(Set.of(new GameCategory(1)));
                GameData singlePlayerGameData1 =  new GameData(Set.of(new GameCategory(2)));

                when(steamRequestHandler.parseGameDetailsList(multiplayerAppDetailsResponseExampleJson1)).thenReturn(multiplayerGameData1);
                when(steamRequestHandler.parseGameDetailsList(singlePlayerAppDetailsResponseExampleJson)).thenReturn(singlePlayerGameData1);
                when(steamRequestHandler.parseGameDetailsList(multiplayerAppDetailsResponseExampleJson2)).thenReturn(multiplayerGameData2);
                assertEquals(Set.of(exampleGame1, exampleGame3), gameService.getCommonGames(Set.of("573100","573101" ,"573102"), true));
            }

            @Test
            @DisplayName("If an exception is thrown while trying to determine whether a game is multiplayer it will throw an exception with an appropriate message")
            void IfAnExceptionIsThrownWhileTryingToDetermineWhetherAGameIsMultiplayerItWillThrowAnExceptionWithAnAppropriateMessage() throws IOException {
                when(steamRequestHandler.parseGameDetailsList(any())).thenThrow(new IOException());
                Game exampleGame1 = createExampleGame("12", null, "Some game name");
                when(gameRepository.findGameByAppid("12")).thenReturn(exampleGame1);
                assertThrows(UncheckedIOException.class, () -> gameService.getCommonGames(Set.of("12"), true));
            }
        }


    }

    private Game createExampleGame(String appid, Boolean multiplayer, String name) {
        Game exampleGame = new Game();
        exampleGame.setAppid(appid);
        exampleGame.setMultiplayer(multiplayer);
        exampleGame.setName(name);
        exampleGame.setId(BigInteger.valueOf(10L));
        return exampleGame;
    }

}