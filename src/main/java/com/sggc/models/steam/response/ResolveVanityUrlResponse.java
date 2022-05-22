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
        private static final int SUCCESS_CODE = 1;

        private int success;
        @JsonProperty("steamid")
        private String steamId;

        /**
         * Determines whether the ResolveVanityUrl request was successful by checking the success code in the response
         * @return true if the success code equals 1 otherwise false
         */
        public boolean isSuccess(){
            return success == SUCCESS_CODE;
        }
    }
}
