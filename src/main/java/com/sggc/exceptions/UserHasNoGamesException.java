package com.sggc.exceptions;

import com.sggc.errors.ApiError;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Represents an exception to be thrown when a Steam user owns no games
 */
@Data
public class UserHasNoGamesException extends WebAppException {
    private String userId;

    public UserHasNoGamesException() {
        super();
    }

    public UserHasNoGamesException(String userId) {
        super();
        this.userId = userId;
    }

    @Override
    public ApiError toApiError() {
        return new ApiError(
                "UserHasNoGamesException",
                "User with Id: " + userId + " has no games associated with their account, or doesn't exist",
                null
        );
    }
}
