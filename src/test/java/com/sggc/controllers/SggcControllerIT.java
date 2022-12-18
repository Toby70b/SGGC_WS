package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SggcResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

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
    private SggcController controller;

    @Test
    @DisplayName("Given a request with less than two steam id's when the service validates the request body then a 400 error will be returned with an appropriate message")
    public void givenRequestWithLessThanTwoSteamIdsWhenServicesValidatesA400ErrorWillBeReturnedWithAnAppropriateMessage() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        sggcRequest.setSteamIds(Collections.singleton("someSteamId"));


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
}
