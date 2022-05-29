package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

import static com.sggc.TestUtils.createExampleGame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SteamRequestServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SteamRequestService steamRequestService;

    @Test
    @DisplayName("Given a valid user id, when a successful request is made to retrieve a user's owned games then the " +
            "service should return a parsed response")
    void givenAValidUserIdWhenASuccessfulRequestIsMadeToRetrieveAUsersOwnedGamesThenTheServiceShouldReturnAParsedResponse() throws SecretRetrievalException {
        String mockURI = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=SomeKey&steamid=12345678910";

        GetOwnedGamesResponse mockResponse = new GetOwnedGamesResponse();
        GetOwnedGamesResponse.Response mockResponseDetails = new GetOwnedGamesResponse.Response();
        mockResponseDetails.setGameCount(1);
        mockResponseDetails.setGames(Set.of(createExampleGame("1",true,"someGame")));
        mockResponse.setResponse(mockResponseDetails);

        when(restTemplate.getForObject(mockURI, GetOwnedGamesResponse.class)).thenReturn(mockResponse);

        assertEquals(mockResponse,steamRequestService.requestUsersOwnedGamesFromSteamApi("12345678910"));

    }




}