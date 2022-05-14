package com.sggc.exceptions;

import com.sggc.errors.ApiError;
import com.sggc.models.ValidationResult;
import lombok.Getter;

import java.util.List;

/**
 * Represents an exception to be thrown when user input has violated the validation rules
 */
@Getter
public class ValidationException extends WebAppException {
    private List<ValidationResult> validationErrors;

    public ValidationException() {
        super();
    }

    public ValidationException(List<ValidationResult> validationErrors) {
        super();
        this.validationErrors = validationErrors;
    }

    @Override
    public ApiError toApiError() {
        return  new ApiError(
                "ValidationException",
                "Request body violates validation rules, check error details for more information.",
                validationErrors
        );
    }
}
