package com.sggc.controllers;

import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SGGCResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
/**
 * Controller advice to the SGGC controller used to provide the controller with a standardized method of returning errors back to the client
 */
public class SGGCControllerAdvice extends ResponseEntityExceptionHandler {


    @ExceptionHandler(Exception.class)
    /**
     * Catch-all method to catch all uncaught exceptions and wrap them in an SGGCResponse object for easier consuming
     */
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