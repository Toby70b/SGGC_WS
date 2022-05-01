package com.sggc.validation;

import com.sggc.models.ValidationError;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a validator for deciding whether a Steam user id is valid
 */
public class SteamIdValidator extends StringValidator{

    public static final int STEAM_USER_ID_LENGTH = 17;
    public static final String USER_ID_INVALID_LENGTH = "Steam user id must be exactly 17 characters long";
    public static final String USER_ID_IS_NOT_NUMERIC_ERROR_MESSAGE = "Steam user id must be numeric";

    @Override
    public ValidationError validate(String steamId) {
        if(steamId.length() != STEAM_USER_ID_LENGTH){
            return new ValidationError(steamId, USER_ID_INVALID_LENGTH);
        }
        if(!StringUtils.isNumeric(steamId)){
            return new ValidationError(steamId, USER_ID_IS_NOT_NUMERIC_ERROR_MESSAGE);
        }
        return null;
    }
}
