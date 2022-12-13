package com.sggc.validation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResult {
    private boolean error;
    private String objectId;
    private String validationMessage;

    public ValidationResult(boolean error) {
        this.error = error;
    }
}
