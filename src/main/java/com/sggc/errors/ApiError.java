package com.sggc.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiError {

    private String apiVersion;
    private String code;
    private String exception;
    private String errorMessage;

    public ApiError(String apiVersion, String code, String exception, String errorMessage) {
        this.apiVersion = apiVersion;
        this.code = code;
        this.exception = exception;
        this.errorMessage = errorMessage;
    }

    public static ApiError fromDefaultAttributeMap(String apiVersion,
                                                   Map<String, Object> defaultErrorAttributes) {
        return new ApiError(
                apiVersion,
                ((Integer) defaultErrorAttributes.get("status")).toString(),
                (String) defaultErrorAttributes.getOrDefault("exception", "no message available"),
                (String) defaultErrorAttributes.get("message")
        );
    }

    public Map<String, Object> toAttributeMap() {
        Map<String, Object> apiVersion = new HashMap<>();
        apiVersion.put("apiVersion", this.apiVersion);
        apiVersion.put("code", "500");
        apiVersion.put("exception", "Exception");
        apiVersion.put("errorMessage", "Internal Server Error");
        return apiVersion;
    }
}