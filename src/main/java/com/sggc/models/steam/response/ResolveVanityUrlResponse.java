package com.sggc.models.steam.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResolveVanityUrlResponse {
    private Response response;

    @Data
    @AllArgsConstructor
    public static class Response {
        private boolean success;
        private String steamId;
    }
}
