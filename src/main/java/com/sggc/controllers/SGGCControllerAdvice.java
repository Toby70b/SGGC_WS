package com.sggc.controllers;

import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SGGCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
/**
 * Controller advice to the SGGC controller used to provide the controller with a standardized method of returning errors back to the client
 */
public class SGGCControllerAdvice extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(SGGCControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    /**
     * Catch-all method to catch all uncaught exceptions and wrap them in an SGGCResponse object for easier consuming
     */
    public ResponseEntity<SGGCResponse> handleGenericException(Exception ex) {
        final ApiError error = new ApiError(
                "Exception",
                "Internal server error."
        );
        logger.error("ERROR:", ex);
        return new ResponseEntity<>(new SGGCResponse(false,error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}