package com.sggc.exceptions;

import com.sggc.errors.ApiError;

public abstract class WebAppException extends Exception {

    public abstract ApiError toApiError();
}
