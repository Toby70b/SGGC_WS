package com.sggc.services;

import com.sggc.AbstractIntegrationTest;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class UserServiceIT extends AbstractIntegrationTest {

    @Autowired
    public UserService userService;

    @Test
    @DisplayName("If a user is not found in the DB, its details will be requested via the Steam API and saved to the DB")
    void IfAUserIsNotFoundInTheDbItsDetailsWillBeRequestedViaTheSteamApiAndSavedToTheDb() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
        userService.getIdsOfGamesOwnedByAllUsers(Set.of("DummySteamId1","DummySteamId2"));
        System.out.println();
    }

    @Test
    void dasda() throws TooFewSteamIdsException, SecretRetrievalException, UserHasNoGamesException {
        userService.getIdsOfGamesOwnedByAllUsers(Set.of("DummySteamId1","DummySteamId2"));
        System.out.println();
    }
}
