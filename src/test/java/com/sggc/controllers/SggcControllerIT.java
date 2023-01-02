package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.exceptions.VanityUrlResolutionException;
import com.sggc.models.sggc.SggcResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import com.sggc.services.VanityUrlService;
import com.sggc.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SggcController.class)
public class SggcControllerIT {

    public static final String SGGC_CONTROLLER_URI = "/api/sggc/";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @MockBean
    private VanityUrlService vanityUrlService;
    @MockBean
    private GameService gameService;

    @Test
    @DisplayName("Given a request with less than two Steam id's when validating the request body then a 400 error will be returned with an appropriate message")
    public void givenRequestWithLessThanTwoSteamIdsWhenServicesValidatesA400ErrorWillBeReturnedWithAnAppropriateMessage() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        String mockSteamId = "someSteamId";
        sggcRequest.setSteamIds(Collections.singleton(mockSteamId));


        final ApiError error = new ApiError(
                "Exception",
                "Request body violates validation rules, check error details for more information.",
                Collections.singleton("More than one Steam id must be included")
        );
        SggcResponse expectedResponse = new SggcResponse(false, error);

        mockMvc.perform(post(SGGC_CONTROLLER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sggcRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedResponse), false));
    }

    @Test
    @DisplayName("Given a request is received with an invalid Vanity URL when validating the Vanity URL then a 400 error will be return with an appropriate response object")
    public void givenARRequestIsReceivedWithAnInvalidVanityUrlWhenValidatingThenA400ErrorWillBeReturnedWithAnAppropriateResponseObject() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        String mockVanityUrl = "someVanityUrl!%$%£%$R";
        String mockSteamId = "someSteamId";
        sggcRequest.setSteamIds(Set.of(mockVanityUrl,mockSteamId));

        ValidationResult expectedValidationError = new ValidationResult(true, mockVanityUrl, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockVanityUrl,mockSteamId))).thenReturn(List.of(expectedValidationError));


        final ApiError error = new ApiError(
                "ValidationException",
                "Request body violates validation rules, check error details for more information.",
                Collections.singleton(expectedValidationError)
        );
        SggcResponse expectedResponse = new SggcResponse(false, error);

        mockMvc.perform(post(SGGC_CONTROLLER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sggcRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedResponse), false));
    }

    @Test
    @DisplayName("Given a request with a valid Vanity URL which does not have a corresponding Steam user id when resolving " +
            "it via the Steam API then a 400 error will be return with an appropriate response object")
    public void givenARRequestWithAVValidVanityUrlWhichDoesNotHaveACorrespondingSteamUserIdWhenResolvingThenItWillReturnA400Error() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        String mockInvalidVanityUrl = "someVanityUrl";
        String mockSteamId = "someSteamId";
        sggcRequest.setSteamIds(Set.of(mockInvalidVanityUrl,mockSteamId));

        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockInvalidVanityUrl,mockSteamId))).thenReturn(new ArrayList<>());

        when(vanityUrlService.resolveVanityUrls(Set.of(mockInvalidVanityUrl,mockSteamId)))
                .thenThrow(new VanityUrlResolutionException(mockInvalidVanityUrl));

        final ApiError error = new ApiError(
                "VanityUrlResolutionException",
                "Vanity Url: someVanityUrl could not be resolved to a steam id",
                null
        );
        SggcResponse expectedResponse = new SggcResponse(false, error);

        mockMvc.perform(post(SGGC_CONTROLLER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sggcRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedResponse), false));
    }

    @Test
    @DisplayName("Given a request with a valid Steam user id, when a user is found to own no games return a 404 error with an appropriate response object")
    public void givenARequestWithAValidSteamUserIdWhenAUserIdFoundToOwnNoGamesReturnA404ErrorWithAnAppropriateResponseObject() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        String mockSteamId1 = "someSteamId1";
        String mockSteamId2 = "someSteamId2";
        sggcRequest.setSteamIds(Set.of(mockSteamId1,mockSteamId2));

        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockSteamId1,mockSteamId2))).thenReturn(new ArrayList<>());

        when(vanityUrlService.resolveVanityUrls(Set.of(mockSteamId1,mockSteamId2))).thenReturn(Set.of(mockSteamId1,mockSteamId2));

        when(userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockSteamId1,mockSteamId2)))
                .thenThrow(new UserHasNoGamesException(mockSteamId1));

        final ApiError error = new ApiError(
                "UserHasNoGamesException",
                "User with Id: someSteamId1 has no games associated with their account, or doesn't exist.",
                null
        );
        SggcResponse expectedResponse = new SggcResponse(false, error);

        mockMvc.perform(post(SGGC_CONTROLLER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sggcRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedResponse), false));
    }

    @Test
    @DisplayName("Given a request when after Vanity URL resolution there are fewer than two Steam ids to find common games for" +
            " then a 400 error will be return with an appropriate response object")
    public void givenARequestWhenAfterResolutionThereAreFewerThanTwoSteamIdsToFindCommonGamesForThenReturnA400ErrorWithAnAppropriateResponseObject() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        String mockSteamId1 = "someSteamId1";
        String mockSteamId2 = "someSteamId2";
        sggcRequest.setSteamIds(Set.of(mockSteamId1,mockSteamId2));

        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockSteamId1,mockSteamId2))).thenReturn(new ArrayList<>());

        when(vanityUrlService.resolveVanityUrls(Set.of(mockSteamId1,mockSteamId2))).thenReturn(Set.of(mockSteamId1,mockSteamId2));

        when(userService.getIdsOfGamesOwnedByAllUsers(Set.of(mockSteamId1,mockSteamId2)))
                .thenThrow(new TooFewSteamIdsException());

        final ApiError error = new ApiError(
                "TooFewSteamIdsException",
                "A minimum of two users must be provided in order to compare game libraries. If a mix of " +
                        "Vanity URL and Steam Id have been provided, please confirm that they dont relate to the same user.",
                null
        );
        SggcResponse expectedResponse = new SggcResponse(false, error);

        mockMvc.perform(post(SGGC_CONTROLLER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sggcRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedResponse), false));
    }

    @Test
    @DisplayName("Given a request when an uncaught exception is thrown then a 500 error will be return with an appropriate response object")
    public void givenARequestWhenAnUncaughtExceptionIsThrownThenA500ErrorWillBeReturnedWithAnAppropriateResponseObject() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        String mockSteamId1 = "someSteamId1";
        String mockSteamId2 = "someSteamId2";
        sggcRequest.setSteamIds(Set.of(mockSteamId1,mockSteamId2));

        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockSteamId1,mockSteamId2))).thenThrow(new RuntimeException());

        final ApiError error = new ApiError(
                "Exception",
                "Internal server error."
        );
        SggcResponse expectedResponse = new SggcResponse(false, error);

        mockMvc.perform(post(SGGC_CONTROLLER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sggcRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(expectedResponse), false));
    }
}
