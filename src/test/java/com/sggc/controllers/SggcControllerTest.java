package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.exceptions.VanityUrlResolutionException;
import com.sggc.models.Game;
import com.sggc.models.sggc.SggcResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import com.sggc.services.VanityUrlService;
import com.sggc.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static util.constants.SggcVanityUrlValidationErrorMessageConstants.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;

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
    @DisplayName("Given a valid request, when after processing the request, then it will return a successful response whose body includes a list of games.")
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
    @DisplayName("Given a request, when If one of the users specified in the request has no games, then it will return a 404 error with an appropriate message.")
    void IfOneOfTheUsersSpecifiedInTheRequestHasNoGamesItWillReturnA404ErrorWithAnAppropriateMessage() throws Exception {

        String mockSteamId1 = "76561198045206222";
        String mockSteamId2 = "76561198045206223";

        when(userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockSteamId1, mockSteamId2)))
                .thenThrow(new UserHasNoGamesException(mockSteamId1));

        when(vanityUrlService.resolveVanityUrls(Set.of(mockSteamId1, mockSteamId2)))
                .thenReturn(Set.of(mockSteamId1, mockSteamId2));

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of(mockSteamId1, mockSteamId2));
        request.setMultiplayerOnly(true);

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(request);

        SggcResponse expectedResponse = new SggcResponse(false, new ApiError("UserHasNoGamesException",
                "User with Id: 76561198045206222 has no games associated with their account, or doesn't exist.", null));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    @DisplayName("Given a request, when one of the Steam vanity URLs specified in the request is invalid, then it will return a 400 error with an appropriate message.")
    void IfOneOfTheSteamIdsSpecifiedInTheRequestIsInvalidItWillReturnA400ErrorWithAnAppropriateMessage() throws Exception {
        String mockInvalidVanityUrl = "765611980452$062222321321";
        String mockSteamId = "765611980452062222321321";

        List<ValidationResult> validationErrors = List.of(new ValidationResult(true,
                mockInvalidVanityUrl, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE));

        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockSteamId, mockInvalidVanityUrl)))
                .thenReturn(validationErrors);

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of(mockSteamId, mockInvalidVanityUrl));
        request.setMultiplayerOnly(true);

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(request);

        SggcResponse expectedResponse = new SggcResponse(false, new ApiError("ValidationException",
                "Request body violates validation rules. Please review the response object for more information.", validationErrors));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    @DisplayName("Given a request, when an error occurs while trying to retrieve the steam key secret, then an appropriate exception will be thrown.")
    void IfAnErrorOccursWhileTryingToRetrieveTheSteamKeySecretItWillReturnA500ErrorWithAnAppropriateMessage() throws Exception {
        String mockSteamId1 = "765611980452062222321321";
        String mockSteamId2 = "76561198045206223";

        when(vanityUrlService.resolveVanityUrls(Set.of(mockSteamId1, mockSteamId2)))
                .thenThrow(new SecretRetrievalException("someSecretId", new Exception()));

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of(mockSteamId1, mockSteamId2));
        request.setMultiplayerOnly(true);

        SecretRetrievalException expectedException =
                assertThrows(SecretRetrievalException.class, () ->
                        sggcController.getGamesAllUsersOwn(request));

        assertEquals("Exception occurred when attempting to retrieve secret [someSecretId] from AWS secrets manager."
                , expectedException.getMessage());
    }

    @Test
    @DisplayName("Given a request, when an error occurs while trying to resolve a vanity URL into a Steam user ID, then the controller will return a 404 error with an appropriate message.")
    void IfAnErrorOccursWhileTryingToResolveAVanityUrlIntoASteamUserIdThenTheControllerWillReturnA404ErrorWithAnAppropriateMessage() throws Exception {
        String mockVanityUrl = "SomeVanityUrl";
        String mockSteamId = "76561198045206223";

        when(vanityUrlService.resolveVanityUrls(Set.of(mockVanityUrl, mockSteamId)))
                .thenThrow(new VanityUrlResolutionException(mockVanityUrl));

        GetCommonGamesRequest request = new GetCommonGamesRequest();
        request.setSteamIds(Set.of(mockVanityUrl, mockSteamId));
        request.setMultiplayerOnly(true);

        ResponseEntity<SggcResponse> response = sggcController.getGamesAllUsersOwn(request);

        SggcResponse expectedResponse = new SggcResponse(false, new ApiError("VanityUrlResolutionException",
                "Vanity Url: SomeVanityUrl could not be resolved to a steam id"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }
}


