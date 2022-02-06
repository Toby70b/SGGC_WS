package com.sggc.services;

import com.sggc.models.Game;
import com.sggc.repositories.GameRepository;
import com.sggc.util.SteamRequestHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SteamRequestHandler steamRequestHandler;

    @InjectMocks
    private GameService gameService;

    @Nested
    @DisplayName("If provided with a game id it should return the details of the game matching that id")
    class IsGameMultiplayerTests {

        @Test
        @DisplayName("Return game details regardless of multiplayer status")
        void ifTheGameIsMultiplayerItWillReturnTrue() {
            Game exampleGame = new Game();
            exampleGame.setAppid("12");
            exampleGame.setMultiplayer(true);
            exampleGame.setName("Some Game name");
            exampleGame.setId(BigInteger.valueOf(10L));
            when(gameRepository.findGameByAppid("12")).thenReturn(exampleGame);
            assertEquals(Set.of(exampleGame),gameService.getCommonGames(Set.of("12"),false));
        }




    }

}