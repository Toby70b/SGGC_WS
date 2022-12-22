package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SggcResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import com.sggc.services.VanityUrlService;
import com.sggc.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
    @Autowired
    private SggcController controller;
    @MockBean
    private GameService gameService;
    @MockBean
    private UserService userService;
    @MockBean
    private VanityUrlService vanityUrlService;

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
        String mockInvalidVanityUrl = "someVanityUrl!%$%£%$R";
        String mockSteamId = "someSteamId";
        sggcRequest.setSteamIds(Set.of(mockInvalidVanityUrl,mockSteamId));

        ValidationResult expectedValidationError = new ValidationResult(true, mockInvalidVanityUrl, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
        when(vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(mockInvalidVanityUrl,mockSteamId))).thenReturn(List.of(expectedValidationError));


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

    //check json for validation error
    //check json for vanity url resolution error
    //check json for user has no games error
    //check a 500 error
}
