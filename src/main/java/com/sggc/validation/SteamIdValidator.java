package com.sggc.validation;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a validator for deciding whether a Steam user id is valid
 */
public class SteamIdValidator extends StringValidator{

    public static final int STEAM_USER_ID_LENGTH = 17;

    @Override
    public boolean validate(String steamId) {
        if(steamId.length() != STEAM_USER_ID_LENGTH){
            return false;
        }
        if(!StringUtils.isNumeric(steamId)){
            return false;
        }
        return true;
    }
}
