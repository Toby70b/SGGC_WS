package com.sggc.controllers;

import com.sggc.errors.ApiError;
import com.sggc.models.sggc.SggcResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Controller advice to the SGGC controller used to provide the controller with a standardized method of returning errors back to the client
 */
@Log4j2
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SggcRestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Intercepts validation errors with the request model and returns an error wrapped them in an SGGCResponse object for easier consuming
     *
     * @param ex the caught exception
     * @param headers header of the request
     * @param status status of the response that would originally have been sent
     * @param request details of the request
     * @return a ResponseEntity object with a HTTP status of 400 containing a SGGCResponse indicating an error
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Validation error in request object", ex);
        List<String> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        final ApiError error = new ApiError(
                "Exception",
                "Request body violates validation rules, check error details for more information.",
                validationErrors
        );
        SggcResponse response = new SggcResponse(false, error);
        log.info("Internal server error occurred returning 400 response with body [{}]", response);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all method to catch all uncaught exceptions and wrap them in an SGGCResponse object for easier consuming
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SggcResponse> handleGenericException(Exception ex) {
        log.error("Internal server error occurred", ex);
        final ApiError error = new ApiError(
                "Exception",
                "Internal server error."
        );
        SggcResponse response = new SggcResponse(false, error);
        log.info("Internal server error occurred returning 500 response with body [{}]", response);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}