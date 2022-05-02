package com.sggc.models.steam.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
//TODO move details in here as inner class
/**
 * Represents a response to the SGGC controller to retrieve common games
 */
@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOwnedGamesResponse {
    private GetOwnedGamesResponseDetails response;
}