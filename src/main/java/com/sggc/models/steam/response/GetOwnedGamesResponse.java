package com.sggc.models.steam.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sggc.models.Game;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Set;
//TODO move details in here as inner class

/**
 * Represents a response to the SGGC controller to retrieve common games
 */
@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetOwnedGamesResponse {
    private Response response;

    @Data
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("game_count")
        private int gameCount;
        private Set<Game> games;
    }
}
