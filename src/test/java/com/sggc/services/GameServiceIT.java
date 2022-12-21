package com.sggc.services;

import com.sggc.AbstractIntegrationTest;
import com.sggc.repositories.GameRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GameServiceIT extends AbstractIntegrationTest {

    @Autowired
    public GameService gameService;

    @Autowired
    public GameRepository gameRepository;

    @Nested
    @DisplayName("If provided with a list of Game app ids then the service will return all Games with matching app ids persisted within the database")
    class FindGamesByAppIdTests() {

        @Test
        void ifProvidedWithAListOfGameAppIdsThenTheServiceWillReturnAllMatchingAppIdsPersistedWithinTheDatabase () {
            //populate the db with some games

            //call the service with some of the games populated

            //assert the response is equal to what was expected
        }

        @Test
        @DisplayName("If multiplayer-only games are requested the service will exclude any non-multiplayer games from the returned list")
        void ifMultiplayerOnlyGamesAreRequestedTheServiceWillExcludeAnyNonMultiplayerGamesFromTheReturnedList () {
            //populate the db with some games, some multiplayer others not

            //call the service with some of the games populated, include both multiplayer and not

            //assert the response is equal to what was expected
        }
    }

    @Nested
    @DisplayName("If a Game's multiplayer status is currently unknown it should be requested via the Steam Store API and persisted within the database")
    class FindGamesByAppIdTests() {


    }

    //multiplayer status should be saved down

    //if game no longer has details it should be treated as multiplayer and persisted
}
