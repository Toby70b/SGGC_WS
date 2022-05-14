package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.exceptions.ValidationException;
import com.sggc.models.Game;
import com.sggc.models.ValidationResult;
import com.sggc.models.sggc.SGGCResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sggc.validation.SteamIdValidator.USER_ID_INVALID_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
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
    void IfProvidedWithAValidRequestItWillReturnASuccessfulResponseWhoseBodyIncludesAListOfGames() throws UserHasNoGamesException, SecretRetrievalException, ValidationException {
        when(userService.getIdsOfGamesOwnedByAllUsers(any())).thenReturn(new HashSet<>());
        Game exampleGame = new Game();
        when(gameService.findGamesById(any(), anyBoolean())).thenReturn(Set.of(exampleGame));
        ResponseEntity<SGGCResponse> response = sggcController.getGamesAllUsersOwn(new GetCommonGamesRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getBody());
        assertEquals(Set.of(exampleGame), response.getBody().getBody());
        assertTrue(response.getBody().isSuccess());
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

            when(userService.resolveVanityUrls(Set.of("76561198045206222", "76561198045206223")))
                    .thenReturn(Set.of("76561198045206222", "76561198045206223"));


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

            SGGCResponse expectedResponse = new SGGCResponse(false,new ApiError("UserHasNoGamesException",
                    "User with Id: 76561198045206222 has no games associated with their account, or doesn't exist.",null));
            //Convert the expected response to JSON and then convert it back to an object to match actual response
            String expectedResponseJson  = objectMapper.writeValueAsString(expectedResponse);
            SGGCResponse expectedResponseObj = objectMapper.readValue(expectedResponseJson, SGGCResponse.class);

            SGGCResponse actualResponse = objectMapper.readValue(result.getResponse().getContentAsString(), SGGCResponse.class);
            assertEquals(expectedResponseObj, actualResponse);
        }

        @Test
        @DisplayName("If one of the Steam Ids specified in the request has no games it will return a 400 error with an appropriate message")
        void IfOneOfTheSteamIdsSpecifiedInTheRequestIsInvalidItWillReturnA400ErrorWithAnAppropriateMessage() throws Exception {
            ObjectMapper objectMapper = new ObjectMapper();

            List<ValidationResult> validationErrors = List.of(new ValidationResult(true,
                    "765611980452062222321321", USER_ID_INVALID_LENGTH));

            when(userService.validateSteamIdsAndVanityUrls(Set.of("765611980452062222321321", "76561198045206223")))
                    .thenReturn(validationErrors);

            GetCommonGamesRequest request = new GetCommonGamesRequest();
            request.setSteamIds(Set.of("765611980452062222321321", "76561198045206223"));
            request.setMultiplayerOnly(true);

            MvcResult result = this.mockMvc
                    .perform(MockMvcRequestBuilders.post("/api/sggc/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();

            SGGCResponse expectedResponse = new SGGCResponse(false,new ApiError("ValidationException",
                    "Request body violates validation rules, check error details for more information.", validationErrors));
            //Convert the expected response to JSON and then convert it back to an object to match actual response
            String expectedResponseJson  = objectMapper.writeValueAsString(expectedResponse);
            SGGCResponse expectedResponseObj = objectMapper.readValue(expectedResponseJson, SGGCResponse.class);

            SGGCResponse actualResponse = objectMapper.readValue(result.getResponse().getContentAsString(), SGGCResponse.class);
            assertEquals(expectedResponseObj, actualResponse);
        }

    }

}
