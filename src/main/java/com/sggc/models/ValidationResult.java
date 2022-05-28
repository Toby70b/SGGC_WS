package com.sggc.models;

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
