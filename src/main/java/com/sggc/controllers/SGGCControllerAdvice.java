package com.sggc.controllers;

import com.sggc.errors.ApiError;
import com.sggc.exceptions.UserHasNoGamesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${sggc.api.version}")
    private String currentApiVersion;
    private Logger logger = LoggerFactory.getLogger(SGGCControllerAdvice.class);

    /**
     * Catches uncaught UserHasNoGamesException that have reached the controller and wraps them in a standardized error object with some additional details for the client
     * @param ex the UserHasNoGamesException
     * @return a response containing a standardized error object with some additional details for the client
     */
    @ExceptionHandler(UserHasNoGamesException.class)
    public ResponseEntity<ApiError> handleUserHasNoGames(UserHasNoGamesException ex) {

        final ApiError error = new ApiError(
                currentApiVersion,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                "UserHasNoGamesException",
                "User with Id: " + ex.getUserId() + " has no games associated with their account, or doesn't exist"
        );
        logger.error("ERROR:", ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    //TODO we need a catch-all case here, can replace the below IOException case
    /*
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiError> handleIOException(IOException ex) {
        final ApiError error = new ApiError(
                currentApiVersion,
                Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "IOException",
                "Internal server error, error within code, please check the logs..."
        );
        logger.error("ERROR:", ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    */
}