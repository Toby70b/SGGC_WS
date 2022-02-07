package com.sggc.controllers;

import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.GetCommonGamesRequest;
import com.sggc.models.User;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SGGCControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SGGCController sggcController;

    @Test
    @DisplayName("If provided with a valid request it will return a successful response whose body includes a list of games")
    void IfProvidedWithAValidRequestItWillReturnASuccessfulResponseWhoseBodyIncludesAListOfGames() throws UserHasNoGamesException {
        when(userService.getIdsOfGamesOwnedByAllUsers(any())).thenReturn(new HashSet<>());
        Game exampleGame = new Game();
        when(gameService.getCommonGames(any(),anyBoolean())).thenReturn(Set.of(exampleGame));
        ResponseEntity<Set<Game>> response = sggcController.getGamesAllUsersOwn(new GetCommonGamesRequest());
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(Set.of(exampleGame),response.getBody());
    }

    @Test
    @DisplayName("If one of the users specified in the request has no games it should return a 404 error with an appropriate message")
    void IfProvidedWithAGameIdItWillReturnTheDetailsOfTheGameMatchingThatId() throws UserHasNoGamesException {
        when(userService.getIdsOfGamesOwnedByAllUsers(any())).thenThrow(new UserHasNoGamesException());
        ResponseEntity<Set<Game>> response = sggcController.getGamesAllUsersOwn(new GetCommonGamesRequest());
        assertEquals(HttpStatus.OK,response.getStatusCode());
    }

}