package com.sggc.services;

import com.sggc.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Represents a service for interacting with Steam Vanity URLs
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class VanityUrlService {

    public static final String VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE = "Vanity URL must be between 3 and 32 characters long";
    public static final String VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE = "Vanity URL must not contain special characters";

    /**
     * Validate a Steam Vanity URL
     *
     * @param vanityUrl the Steam Vanity URL to validate against
     * @return a ValidationResult object, indicating if the validation failed, and if so, why.
     */
    public ValidationResult validate(String vanityUrl) {
        if (!withinRequiredLength(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
        }
        if (!StringUtils.isAlphanumeric(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
        }
        return new ValidationResult(false);
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
