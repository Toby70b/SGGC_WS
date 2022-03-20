package com.sggc.errors;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 *  Represents a model of the standardized error that should be returned to the client from the API
 */
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

    /**
     * Creates a standardized error that using default values
     * @param apiVersion the version of the api the client is calling for support purposes
     * @param defaultErrorAttributes a map containing default values to create the error object
     * @return a standardized error that containing default values
     */
    public static ApiError fromDefaultAttributeMap(String apiVersion,
                                                   Map<String, Object> defaultErrorAttributes) {
        return new ApiError(
                apiVersion,
                ((Integer) defaultErrorAttributes.get("status")).toString(),
                (String) defaultErrorAttributes.getOrDefault("exception", "no message available"),
                (String) defaultErrorAttributes.get("message")
        );
    }

    /**
     * Creates a map of default values for a standardized error object
     * @return a map of default values for a standardized error object
     */
    public Map<String, Object> toAttributeMap() {
        Map<String, Object> apiVersion = new HashMap<>();
        apiVersion.put("apiVersion", this.apiVersion);
        apiVersion.put("code", "500");
        apiVersion.put("exception", "Exception");
        apiVersion.put("errorMessage", "Internal Server Error");
        return apiVersion;
    }
}