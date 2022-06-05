package com.sggc.controllers;

import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SGGCResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.BindException;

/**
 * Controller advice to the SGGC controller used to provide the controller with a standardized method of returning errors back to the client
 */
@Log4j2
@RestControllerAdvice
public class SGGCRestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<SGGCResponse> handleBindException(BindException ex) {
        log.error("BindException occurred, this will be caused by a validation error with the request body", ex);
        final ApiError error = new ApiError(
                "ValidationException",
                ""

        );
        SGGCResponse response = new SGGCResponse(false,error);
        log.info("Error occurred when validation request object returning 400 error response with body [{}]", response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /**
     * Catch-all method to catch all uncaught exceptions and wrap them in an SGGCResponse object for easier consuming
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SGGCResponse> handleGenericException(Exception ex) {
        log.error("Internal server error occurred", ex);
        final ApiError error = new ApiError(
                "Exception",
                "Internal server error."
        );
        SGGCResponse response = new SGGCResponse(false,error);
        log.info("Internal server error occurred returning 500 response with body [{}]", response);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}