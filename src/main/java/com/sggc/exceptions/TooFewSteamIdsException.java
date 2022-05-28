package com.sggc.exceptions;

import com.sggc.errors.ApiError;

/**
 * Represents an exception to be thrown when less than two steam user's libraries are being compared
 */
public class TooFewSteamIdsException extends WebAppException {
    @Override
    public ApiError toApiError() {
        return  new ApiError(
                "TooFewSteamIdsException",
                "A minimum of two users must be provided in order to compare game libraries. If a mix of Vanity URL and Steam Id have been provided, please confirm that they dont relate to the same user.",
                null
        );
    }
}
