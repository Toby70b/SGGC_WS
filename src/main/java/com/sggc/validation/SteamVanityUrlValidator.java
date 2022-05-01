package com.sggc.validation;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a validator for deciding whether a Steam vanity URL is valid
 */
public class SteamVanityUrlValidator extends StringValidator{
    @Override
    public boolean validate(String vanityUrl) {
        if(!withinRequiredLength(vanityUrl)){
            return false;
        }
        if (!StringUtils.isAlphanumeric(vanityUrl)){
            return false;
        }
        return true;
    }

    /**
     * Checks whether a String is within 3 and 32 characters long i.e. The limits of a Steam vanity URL
     * @param vanityUrl the Steam vanity URL String to validate
     * @return true if the length of the vanity URL is within 3 and 32 characters, otherwise false
     */
    private boolean withinRequiredLength(String vanityUrl) {
        return vanityUrl.length() > 2 && vanityUrl.length() < 33;
    }

}
