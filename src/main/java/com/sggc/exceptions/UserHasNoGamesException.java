package com.sggc.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an exception to be thrown when a Steam user owns no games
 */
@Data
public class UserHasNoGamesException extends Exception {
    private String userId;
    public UserHasNoGamesException() {
        super();
    }
    public UserHasNoGamesException(String userId) {
        super(); this.userId = userId;
    }
}
