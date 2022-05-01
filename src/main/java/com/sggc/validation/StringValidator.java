package com.sggc.validation;

import com.sggc.models.ValidationError;

/**
 * Abstract class representing a hierarchy of classes which validate a string based on some conditions
 */
public abstract class StringValidator {
    /**
     * Validate a string against one or more conditions
     *
     * @param stringToValidate the string to validate against
     * @return if the input failed the validation a ValidationError object containing details on the failure, otherwise null
     */
    public abstract ValidationError validate(String stringToValidate);
}
