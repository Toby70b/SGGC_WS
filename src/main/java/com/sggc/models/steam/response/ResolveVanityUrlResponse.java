package com.sggc.models.steam.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
