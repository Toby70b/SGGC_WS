package com.sggc.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.Set;

/**
 * Represents a response to the SGGC controller to retrieve common games
 */
@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOwnedGamesResponse {
    private GetOwnedGamesResponseDetails response;
}