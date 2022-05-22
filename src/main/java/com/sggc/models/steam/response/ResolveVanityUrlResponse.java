package com.sggc.models.steam.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a response to the SGGC controller to resolve a vanity URL
 */
@Data
@NoArgsConstructor
public class ResolveVanityUrlResponse {
    private Response response;

    @Data
    @NoArgsConstructor
    public static class Response {
        private boolean success;
        @JsonProperty("steamid")
        private String steamId;
    }
}
