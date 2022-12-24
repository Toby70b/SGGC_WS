package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.exceptions.*;
import com.sggc.models.Game;
import com.sggc.models.sggc.SggcResponse;
import com.sggc.services.VanityUrlService;
import com.sggc.validation.ValidationResult;
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
import org.springframework.beans.factory.annotation.Autowired;
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

import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SggcControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @Mock
    private VanityUrlService vanityUrlService;

    @InjectMocks
    private SggcController sggcController;

    @Test
    @DisplayName("If provided with a valid request it will return a successful response whose body includes a list of games")
    void IfProvidedWithAValidRequestItWillReturnASuccessfulResponseWhoseBodyIncludesAListOfGames() throws UserHasNoGamesException, SecretRetrievalException, TooFewSteamIdsException {
        when(userService.getIdsOfGamesOwnedByAllUsers(any())).thenReturn(new HashSet<>());
        Game exampleGame = new Game();
        when(gameService.findGamesById(any(), anyBoolean())).thenReturn(Set.of(exampleGame));

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(new GetCommonGamesRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getBody());
        assertEquals(Set.of(exampleGame), response.getBody().getBody());
        assertTrue(response.getBody().isSuccess());
    }


    @Test
    @DisplayName("If one of the users specified in the request has no games it will return a 404 error with an appropriate message")
    void IfOneOfTheUsersSpecifiedInTheRequestHasNoGamesItWillReturnA404ErrorWithAnAppropriateMessage() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        when(userService.getIdsOfGamesOwnedByAllUsers(Set.of("76561198045206222", "76561198045206223")))
                .thenThrow(new UserHasNoGamesException("76561198045206222"));

        when(vanityUrlService.resolveVanityUrls(Set.of("76561198045206222", "76561198045206223")))
                .thenReturn(Set.of("76561198045206222", "76561198045206223"));

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of("76561198045206222", "76561198045206223"));
        request.setMultiplayerOnly(true);

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(request);

        SggcResponse expectedResponse = new SggcResponse(false, new ApiError("UserHasNoGamesException",
                "User with Id: 76561198045206222 has no games associated with their account, or doesn't exist.", null));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    @DisplayName("If one of the Steam vanity URLs specified in the request is invalid it will return a 400 error with an appropriate message")
    void IfOneOfTheSteamIdsSpecifiedInTheRequestIsInvalidItWillReturnA400ErrorWithAnAppropriateMessage() throws Exception {
        List<ValidationResult> validationErrors = List.of(new ValidationResult(true,
                "765611980452$062222321321", VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE));

        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of("765611980452062222321321", "76561198045206223")))
                .thenReturn(validationErrors);

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of("765611980452062222321321", "76561198045206223"));
        request.setMultiplayerOnly(true);

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(request);

        SggcResponse expectedResponse = new SggcResponse(false, new ApiError("ValidationException",
                "Request body violates validation rules, check error details for more information.", validationErrors));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    @DisplayName("If an error occurs while trying to retrieve the steam key secret it throw an appropriate exception")
    void IfAnErrorOccursWhileTryingToRetrieveTheSteamKeySecretItWillReturnA500ErrorWithAnAppropriateMessage() throws Exception {
        when(vanityUrlService.resolveVanityUrls(Set.of("765611980452062222321321", "76561198045206223")))
                .thenThrow(new SecretRetrievalException(new Exception()));

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of("765611980452062222321321", "76561198045206223"));
        request.setMultiplayerOnly(true);

        SecretRetrievalException expectedException =
                assertThrows(SecretRetrievalException.class, () ->
                        sggcController.getGamesAllUsersOwn(request));

        assertEquals("Exception occurred when attempting to retrieve a secret from AWS secrets manager", expectedException.getMessage());
    }

    @Test
    @DisplayName("If an error occurs while trying to resolve a vanity URL into a steam user id then the controller will return a 404 error with an appropriate message")
    void IfAnErrorOccursWhileTryingToResolveAVanityUrlIntoASteamUserIdThenTheControllerWillReturnA404ErrorWithAnAppropriateMessage() throws Exception {
        when(vanityUrlService.resolveVanityUrls(Set.of("SomeVanityUrl", "76561198045206223")))
                .thenThrow(new VanityUrlResolutionException("SomeVanityUrl"));

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of("SomeVanityUrl", "76561198045206223"));
        request.setMultiplayerOnly(true);

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(request);

        SggcResponse expectedResponse = new SggcResponse(false, new ApiError("VanityUrlResolutionException",
                "Vanity Url: SomeVanityUrl could not be resolved to a steam id"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }
}


