package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.GetCommonGamesRequest;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        when(gameService.getCommonGames(any(), anyBoolean())).thenReturn(Set.of(exampleGame));
        ResponseEntity<Set<Game>> response = sggcController.getGamesAllUsersOwn(new GetCommonGamesRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Set.of(exampleGame), response.getBody());
    }

    @Nested
    @DisplayName("Controller Advice Tests")
    class ControllerAdviceTests {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            this.mockMvc = MockMvcBuilders.standaloneSetup(new SGGCController(gameService, userService))
                    .setControllerAdvice(SGGCControllerAdvice.class)
                    .build();
        }

        @Test
        @DisplayName("If one of the users specified in the request has no games it will return a 404 error with an appropriate message")
        void IfOneOfTheUsersSpecifiedInTheRequestHasNoGamesItWillReturnA404ErrorWithAnAppropriateMessage() throws Exception {
            ObjectMapper objectMapper = new ObjectMapper();

            when(userService.getIdsOfGamesOwnedByAllUsers(Set.of("76561198045206222", "76561198045206223")))
                    .thenThrow(new UserHasNoGamesException("76561198045206222"));

            GetCommonGamesRequest request = new GetCommonGamesRequest();
            request.setSteamIds(Set.of("76561198045206222", "76561198045206223"));
            request.setMultiplayerOnly(true);

            MvcResult result = this.mockMvc
                    .perform(MockMvcRequestBuilders.post("/api/sggc/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            ApiError expectedResponse = new ApiError(null, "404", "UserHasNoGamesException", "User with Id: 76561198045206222 has no games associated with their account, or doesn't exist");
            ApiError actualResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ApiError.class);
            assertEquals(expectedResponse, actualResponse);
        }

    }

}
