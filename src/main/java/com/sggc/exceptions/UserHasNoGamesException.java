package com.sggc.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//TODO add message here
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
