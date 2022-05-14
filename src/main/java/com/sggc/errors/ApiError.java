package com.sggc.errors;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Represents a model of the standardized error that should be returned to the client from the API
 */
@Data
@NoArgsConstructor
public class ApiError {

    private String exception;
    private String errorMessage;
    private Object errorDetails;

    public ApiError(String exception, String errorMessage, Object errorDetails) {
        this.exception = exception;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
    }

    public ApiError(String exception, String errorMessage) {
        this.exception = exception;
        this.errorMessage = errorMessage;
    }
}