package com.sggc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SGGCResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SGGCControllerSpringBootTest {

    private static final ObjectMapper om = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    @DisplayName("Given a request with less than two steam id's when the service validates the request body then a 400 error will be returned with an appropriate message")
    public void givenRequestWithLessThanTwoSteamIdsWhenServicesValidatesA400ErrorWillBeReturnedWithAnAppropriateMessage() throws Exception {
        GetCommonGamesRequest sggcRequest = new GetCommonGamesRequest();
        sggcRequest.setMultiplayerOnly(true);
        sggcRequest.setSteamIds(Collections.singleton("someSteamId"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(om.writeValueAsString(sggcRequest),headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:"+port+"/api/sggc/", HttpMethod.POST, entity, String.class);

        final ApiError error = new ApiError(
                "Exception",
                "Request body violates validation rules, check error details for more information.",
                Collections.singleton("More than one Steam id must be included")
        );
        SGGCResponse expectedResponse = new SGGCResponse(false,error);
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertEquals(om.writeValueAsString(expectedResponse),response.getBody());



    }
}
