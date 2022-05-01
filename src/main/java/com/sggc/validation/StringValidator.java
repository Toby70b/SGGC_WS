package com.sggc.validation;

/**
 * Abstract class representing a hierarchy of classes which validate a string based on some conditions
 */
public abstract class StringValidator {
    /**
     * Validate a string against one or more conditions
     * @param stringToValidate the string to validate against
     * @return true if the string has not violated any of the specified conditions, otherwise false
     */
    public abstract boolean validate(String stringToValidate);
}
