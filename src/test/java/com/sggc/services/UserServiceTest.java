package com.sggc.services;

import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.GetOwnedGamesResponse;
import com.sggc.models.GetOwnedGamesResponseDetails;
import com.sggc.models.User;
import com.sggc.repositories.UserRepository;
import com.sggc.util.SteamRequestHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SteamRequestHandler steamRequestHandler;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("If provided with a list of user ids it will return a list of game ids of games owned by all users")
    class GetIdsOfGamesOwnedByAllUsersTests {

        @Test
        @DisplayName("If one of the users provided does not own any games it will throw an exception with an appropriate message")
        void ifOneOfTheUsersProvidedDoesNotOwnAnyGamesItWillThrowAnExceptionWithAnAppropriateMessage() {
            when(userRepository.findById("1")).thenReturn(Optional.empty());

            GetOwnedGamesResponse mockGetOwnedGamesResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponseDetails getOwnedGamesResponseDetails = new GetOwnedGamesResponseDetails();
            getOwnedGamesResponseDetails.setGameCount(0);
            mockGetOwnedGamesResponse.setResponse(getOwnedGamesResponseDetails);
            when(steamRequestHandler.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
            UserHasNoGamesException exception =
                    assertThrows(UserHasNoGamesException.class, () -> userService.getIdsOfGamesOwnedByAllUsers(Set.of("1", "2")));
            assertEquals(exception.getUserId(), "1");

        }

        @Nested
        @DisplayName("All users own at least one game")
        class AllUsersOwnOneGameTests {
            @Test
            @DisplayName("If provided with a list of users who own one common game it will return a list of common games")
            void ifProvidedWithAListOfUsersWhoOwnOneCommonGameItWillReturnThatGame() throws UserHasNoGamesException {

                User user1 = new User();
                HashSet<String> set1 = new HashSet<>();
                set1.add("1");
                set1.add("2");
                set1.add("3");
                user1.setOwnedGameIds(set1);

                User user2 = new User();
                HashSet<String> set2 = new HashSet<>();
                set2.add("4");
                set2.add("2");
                set2.add("6");
                user2.setOwnedGameIds(set2);

                when(userRepository.findById("1")).thenReturn(Optional.of(user1));
                when(userRepository.findById("2")).thenReturn(Optional.of(user2));

                assertEquals(Set.of("2"), userService.getIdsOfGamesOwnedByAllUsers(Set.of("1", "2")));

            }

            @Test
            @DisplayName("If provided with a list of users who own multiple common games it will return a list of common games")
            void ifProvidedWithAListOfUsersWhoOwnMultipleCommonGameItWillReturnThoseGames() throws UserHasNoGamesException {
                User user1 = new User();
                HashSet<String> set1 = new HashSet<>();
                set1.add("1");
                set1.add("2");
                set1.add("3");
                user1.setOwnedGameIds(set1);

                User user2 = new User();
                HashSet<String> set2 = new HashSet<>();
                set2.add("1");
                set2.add("2");
                set2.add("6");
                user2.setOwnedGameIds(set2);

                when(userRepository.findById("1")).thenReturn(Optional.of(user1));
                when(userRepository.findById("2")).thenReturn(Optional.of(user2));

                assertEquals(Set.of("2", "1"), userService.getIdsOfGamesOwnedByAllUsers(Set.of("1", "2")));
            }

            @Test
            @DisplayName("If provided with a list of users who no multiple common games it will return an empty list")
            void ifProvidedWithAListOfUsersWhoDontOwnAnyCommonGamesItWillReturnAnEmptyList() throws UserHasNoGamesException {
                User user1 = new User();
                HashSet<String> set1 = new HashSet<>();
                set1.add("1");
                set1.add("2");
                set1.add("3");
                user1.setOwnedGameIds(set1);

                User user2 = new User();
                HashSet<String> set2 = new HashSet<>();
                set2.add("4");
                set2.add("5");
                set2.add("6");
                user2.setOwnedGameIds(set2);

                when(userRepository.findById("1")).thenReturn(Optional.of(user1));
                when(userRepository.findById("2")).thenReturn(Optional.of(user2));

                assertTrue(userService.getIdsOfGamesOwnedByAllUsers(Set.of("1", "2")).isEmpty());
            }
        }

    }

    @Nested
    @DisplayName("if provided with a user id it will return a list of game ids of games owned by that user")
    class GetIdsOfGamesOwnedByUserTests {
        @Test
        @DisplayName("If the user provided already exists within the database it will return a list of game ids of games owned by that user")
        void ifTheUserProvidedAlreadyExistsWithinTheDatabaseItWillReturnAListOfTheUsersOwnedGameIds() {
        }

        @Test
        @DisplayName("If the user provided does not exist within the database it will contact the Steam API and return a list of game ids of games owned by that user")
        void ifTheUserProvidedDoesNotExistWithinTheDatabaseItWillContactTheSteamApiAndReturnAListOfTheUsersOwnedGameIds() {
        }

        @Test
        @DisplayName("If the user provided does not exist within the database if the user owns at least one game a new user will be saved to the database with details from the Steam API response")
        void ifTheUserProvidedDoesNotExistWithinTheDatabaseIfTheUserOwnsAtLeastOneGameANewUserWillBeSavedToTheDatabase() {
        }


        @Test
        @DisplayName("If the user found via the Steam API does not own any games it will throw an exception with an appropriate message")
        void ifTheUserFoundViaTheSteamApiDoesNotOwnAnyGamesItWillThrowAnExceptionWithAnAppropriateMessage() {
            when(userRepository.findById("1")).thenReturn(Optional.empty());
            GetOwnedGamesResponse mockGetOwnedGamesResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponseDetails getOwnedGamesResponseDetails = new GetOwnedGamesResponseDetails();
            getOwnedGamesResponseDetails.setGameCount(0);
            mockGetOwnedGamesResponse.setResponse(getOwnedGamesResponseDetails);
            when(steamRequestHandler.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
            UserHasNoGamesException exception =
                    assertThrows(UserHasNoGamesException.class, () -> userService.findOwnedGamesByUserId("1"));
            assertEquals(exception.getUserId(), "1");
        }

    }
}