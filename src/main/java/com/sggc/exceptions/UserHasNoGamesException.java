package com.sggc.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserHasNoGamesException extends Exception {
    private String userId;
    public UserHasNoGamesException() {
        super();
    }
}