package com.sggc.exceptions;

import com.sggc.errors.ApiError;

/**
 * Class representing exceptions whose details should be returned from the webapp to the consumer
 */
public abstract class WebAppException extends Exception {

    /**
     * Creates an ApiError object containing useful information about the exception. Designed to be returned to the API consumer
     *
     * @return an ApiError object containing useful information about the exception
     */
    public abstract ApiError toApiError();
}
