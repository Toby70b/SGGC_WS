package com.sggc.models.steam.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sggc.models.Game;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * Represents a response to the SGGC controller to retrieve common games
 */
@Data
@RequiredArgsConstructor
public class GetOwnedGamesResponseDetails {
    @JsonProperty("game_count")
    private int gameCount;
    private Set<Game> games;
}
