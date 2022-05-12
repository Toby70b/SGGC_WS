package com.sggc.validation;

import com.sggc.models.ValidationResult;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a validator for deciding whether a Steam vanity URL is valid
 */
public class SteamVanityUrlValidator extends StringValidator {

    public static final String VANITY_URL_BEGINS_WITH_AN_INVALID_NUMBER_VALIDATION_ERROR_MESSAGE = "Vanity URL begins with an invalid number";
    public static final String VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE = "Vanity URL must be between 3 and 32 characters long";
    public static final String VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE = "Vanity URL must not contain special characters";

    @Override
    public ValidationResult validate(String vanityUrl) {
        if (beginsWithInvalidNumber(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_BEGINS_WITH_AN_INVALID_NUMBER_VALIDATION_ERROR_MESSAGE);
        }
        if (!withinRequiredLength(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
        }
        if (!StringUtils.isAlphanumeric(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
        }
        return new ValidationResult(false);
    }

    /**
     * Checks whether a String begins with 7, 8 or 9 these being invalid for Steam vanity URL
     *
     * @param vanityUrl the Steam vanity URL String to validate
     * @return true if the String begins with 7, 8 or 9, otherwise false
     */
    private boolean beginsWithInvalidNumber(String vanityUrl) {
        return vanityUrl.startsWith("7") || vanityUrl.startsWith("8") || vanityUrl.startsWith("9");
    }

    /**
     * Checks whether a String is within 3 and 32 characters long i.e. The limits of a Steam vanity URL
     *
     * @param vanityUrl the Steam vanity URL String to validate
     * @return true if the length of the vanity URL is within 3 and 32 characters, otherwise false
     */
    private boolean withinRequiredLength(String vanityUrl) {
        return vanityUrl.length() > 2 && vanityUrl.length() < 33;
    }

}
