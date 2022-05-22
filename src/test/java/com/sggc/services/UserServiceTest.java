package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.User;
import com.sggc.models.ValidationResult;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import com.sggc.repositories.UserRepository;
import com.sggc.util.SteamRequestHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sggc.validation.SteamVanityUrlValidator.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;
import static com.sggc.validation.SteamVanityUrlValidator.VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SteamRequestHandler steamRequestHandler;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("If provided with a list of user ids it will return a list of game ids of games owned by all users")
    class GetIdsOfGamesOwnedByAllUsersTests {

        @Test
        @DisplayName("If one of the users provided does not own any games it will throw an exception with an appropriate message")
        void ifOneOfTheUsersProvidedDoesNotOwnAnyGamesItWillThrowAnExceptionWithAnAppropriateMessage() throws SecretRetrievalException {
            when(userRepository.findById("1")).thenReturn(Optional.empty());

            GetOwnedGamesResponse mockGetOwnedGamesResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponse.Response getOwnedGamesResponseDetails = new GetOwnedGamesResponse.Response();
            getOwnedGamesResponseDetails.setGameCount(0);
            mockGetOwnedGamesResponse.setResponse(getOwnedGamesResponseDetails);
            when(steamRequestHandler.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
            UserHasNoGamesException exception =
                    assertThrows(UserHasNoGamesException.class, () -> userService.getIdsOfGamesOwnedByAllUsers(Set.of("1")));
            assertEquals(exception.getUserId(), "1");

        }


        @Nested
        @DisplayName("All users own at least one game")
        class AllUsersOwnOneGameTests {
            @Test
            @DisplayName("If provided with a list of users who own one common game it will return a list of common games")
            void ifProvidedWithAListOfUsersWhoOwnOneCommonGameItWillReturnThatGame() throws UserHasNoGamesException, SecretRetrievalException {

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
            void ifProvidedWithAListOfUsersWhoOwnMultipleCommonGameItWillReturnThoseGames() throws UserHasNoGamesException, SecretRetrievalException {
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
            void ifProvidedWithAListOfUsersWhoDontOwnAnyCommonGamesItWillReturnAnEmptyList() throws UserHasNoGamesException, SecretRetrievalException {
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
        void ifTheUserProvidedAlreadyExistsWithinTheDatabaseItWillReturnAListOfTheUsersOwnedGameIds() throws UserHasNoGamesException, SecretRetrievalException {
            User user1 = new User();
            HashSet<String> set1 = new HashSet<>();
            set1.add("2");
            set1.add("1342");
            set1.add("10");
            user1.setOwnedGameIds(set1);

            when(userRepository.findById("1")).thenReturn(Optional.of(user1));
            assertEquals(Set.of("2", "1342", "10"), userService.findOwnedGamesByUserId("1"));
        }

        @Test
        @DisplayName("If the user provided does not exist within the database it will contact the Steam API and return a list of game ids of games owned by that user")
        void ifTheUserProvidedDoesNotExistWithinTheDatabaseItWillContactTheSteamApiAndReturnAListOfTheUsersOwnedGameIds() throws UserHasNoGamesException, SecretRetrievalException {
            when(clock.instant()).thenReturn(Clock.systemUTC().instant());
            when(userRepository.findById("1")).thenReturn(Optional.empty());
            GetOwnedGamesResponse mockGetOwnedGamesResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponse.Response getOwnedGamesResponseDetails = new GetOwnedGamesResponse.Response();
            getOwnedGamesResponseDetails.setGameCount(3);

            Game game1 = createExampleGame("2");
            Game game2 = createExampleGame("1342");
            Game game3 = createExampleGame("10");

            getOwnedGamesResponseDetails.setGames(Set.of(game1, game2, game3));
            mockGetOwnedGamesResponse.setResponse(getOwnedGamesResponseDetails);
            when(steamRequestHandler.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
            assertEquals(Set.of("2", "1342", "10"), userService.findOwnedGamesByUserId("1"));
        }

        @Test
        @DisplayName("If the user provided does not exist within the database if the user owns at least one game a new user will be saved to the database with details from the Steam API response")
        void ifTheUserProvidedDoesNotExistWithinTheDatabaseIfTheUserOwnsAtLeastOneGameANewUserWillBeSavedToTheDatabase() throws UserHasNoGamesException, SecretRetrievalException {
            when(userRepository.findById("12")).thenReturn(Optional.empty());
            GetOwnedGamesResponse mockGetOwnedGamesResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponse.Response getOwnedGamesResponseDetails = new GetOwnedGamesResponse.Response();
            getOwnedGamesResponseDetails.setGameCount(1);
            Game game1 = createExampleGame("2");
            getOwnedGamesResponseDetails.setGames(Set.of(game1));
            mockGetOwnedGamesResponse.setResponse(getOwnedGamesResponseDetails);
            when(steamRequestHandler.requestUsersOwnedGamesFromSteamApi("12")).thenReturn(mockGetOwnedGamesResponse);

            Clock fixedClock = Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC);
            when(clock.instant()).thenReturn(fixedClock.instant());

            userService.findOwnedGamesByUserId("12");

            User expectedUserObject = new User();
            expectedUserObject.setId("12");
            expectedUserObject.setOwnedGameIds(Set.of("2"));
            expectedUserObject.setRemovalDate(1535018400L);

            verify(userRepository, times(1)).save(expectedUserObject);


        }

        @Test
        @DisplayName("If the user found via the Steam API does not own any games it will throw an exception with an appropriate message")
        void ifTheUserFoundViaTheSteamApiDoesNotOwnAnyGamesItWillThrowAnExceptionWithAnAppropriateMessage() throws SecretRetrievalException {
            when(userRepository.findById("1")).thenReturn(Optional.empty());
            GetOwnedGamesResponse mockGetOwnedGamesResponse = new GetOwnedGamesResponse();
            GetOwnedGamesResponse.Response getOwnedGamesResponseDetails = new GetOwnedGamesResponse.Response();
            getOwnedGamesResponseDetails.setGameCount(0);
            mockGetOwnedGamesResponse.setResponse(getOwnedGamesResponseDetails);
            when(steamRequestHandler.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
            UserHasNoGamesException exception =
                    assertThrows(UserHasNoGamesException.class, () -> userService.findOwnedGamesByUserId("1"));
            assertEquals(exception.getUserId(), "1");
        }

    }

    @Nested
    class ValidationTests {

        @Nested
        @DisplayName("If provided with a invalid vanity URL it will return a list of validation errors")
        class InvalidUserIdValidationTests {
            @Test
            @DisplayName("If provided with a vanity URL that is too short it will return a validation error with an appropriate message")
            void ifProvidedWithAVanityUrlThatIsTooShortItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = RandomStringUtils.random(2, true, true);
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1,validationResultList.size());
                assertEquals(expectedValidationError,validationResultList.get(0));
            }

            @Test
            @DisplayName("If provided with a vanity URL that is too long it will return a validation error with an appropriate message")
            void ifProvidedWithAVanityUrlThatIsTooLongItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = RandomStringUtils.random(33, true, true);
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1,validationResultList.size());
                assertEquals(expectedValidationError,validationResultList.get(0));
            }

            @Test
            @DisplayName("If provided with a vanity URL that contains specical characters it will return a validation error with an appropriate message")
            void ifProvidedWithAVanityUrlThatContainsSpecialCharactersItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = "abc123%^&";
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1,validationResultList.size());
                assertEquals(expectedValidationError,validationResultList.get(0));
            }

            @Test
            @DisplayName("If provided with multiple invalid Vanity URLs it will return multiple validation errors with appropriate messages")
            void IfProvidedWithMultipleInvalidVanityUrlsItWillReturnMultipleValidationErrorsWithAppropriateMessages() {
                String generatedString1 = "abc123%^&";
                String generatedString2 = RandomStringUtils.random(33, true, true);

                ValidationResult expectedValidationError1 = new ValidationResult(true, generatedString1, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
                ValidationResult expectedValidationError2 = new ValidationResult(true, generatedString2, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);

                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString1,generatedString2));

                assertEquals(2,validationResultList.size());
                assertTrue(validationResultList.contains(expectedValidationError1));
                assertTrue(validationResultList.contains(expectedValidationError2));
            }
        }

        @Nested
        @DisplayName("If provided with a valid user id it will return an empty list")
        class ValidUserIdValidationTests {

            @Test
            @DisplayName("If provided with a valid Steam id it will return an empty list")
            void ifProvidedWithAValidSteamIdItWillReturnAnEmptyList() throws SecretRetrievalException {
                String randomSteamId = RandomStringUtils.random(17, false, true);

                ResolveVanityUrlResponse.Response response = new ResolveVanityUrlResponse.Response();
                response.setSuccess(true);
                response.setSteamId(randomSteamId);
                ResolveVanityUrlResponse vanityUrlResponse = new ResolveVanityUrlResponse();
                vanityUrlResponse.setResponse(response);

                when(steamRequestHandler.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse);
                assertEquals(userService.resolveVanityUrls(Set.of("VanityUrl1")),Set.of(randomSteamId));
            }

            @Test
            @DisplayName("If provided with a valid vanity URL it will return an empty list")
            void ifProvidedWithAValidVanityUrlItWillReturnAnEmptyList() throws SecretRetrievalException {
                String randomSteamId1 = RandomStringUtils.random(17, false, true);
                String randomSteamId2 = RandomStringUtils.random(17, false, true);

                ResolveVanityUrlResponse.Response response1 = new ResolveVanityUrlResponse.Response();
                response1.setSuccess(true);
                response1.setSteamId(randomSteamId1);
                ResolveVanityUrlResponse vanityUrlResponse1 = new ResolveVanityUrlResponse();
                vanityUrlResponse1.setResponse(response1);

                ResolveVanityUrlResponse.Response response2 = new ResolveVanityUrlResponse.Response();
                response2.setSuccess(true);
                response2.setSteamId(randomSteamId2);
                ResolveVanityUrlResponse vanityUrlResponse2 = new ResolveVanityUrlResponse();
                vanityUrlResponse2.setResponse(response2);

                when(steamRequestHandler.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse1);
                when(steamRequestHandler.resolveVanityUrl("VanityUrl2")).thenReturn(vanityUrlResponse2);

                assertEquals(userService.resolveVanityUrls(Set.of("VanityUrl1","VanityUrl2")),Set.of(randomSteamId1,randomSteamId2));
            }

            @Test
            @DisplayName("If provided with a mixture of valid vanity URL's and Steam ids it will return an empty list")
            void ifProvidedWithAMixtureOfValidVanityUrlsAndSteamIdsItWillReturnAnEmptyList() throws SecretRetrievalException {
                String randomSteamId1 = RandomStringUtils.random(17, false, true);
                String randomSteamId2 = RandomStringUtils.random(17, false, true);
                String randomSteamId3 = "7" + RandomStringUtils.random(16, false, true);

                ResolveVanityUrlResponse.Response response1 = new ResolveVanityUrlResponse.Response();
                response1.setSuccess(true);
                response1.setSteamId(randomSteamId1);
                ResolveVanityUrlResponse vanityUrlResponse1 = new ResolveVanityUrlResponse();
                vanityUrlResponse1.setResponse(response1);

                ResolveVanityUrlResponse.Response response2 = new ResolveVanityUrlResponse.Response();
                response2.setSuccess(true);
                response2.setSteamId(randomSteamId2);
                ResolveVanityUrlResponse vanityUrlResponse2 = new ResolveVanityUrlResponse();
                vanityUrlResponse2.setResponse(response2);

                when(steamRequestHandler.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse1);
                when(steamRequestHandler.resolveVanityUrl("VanityUrl2")).thenReturn(vanityUrlResponse2);

                assertEquals(userService.resolveVanityUrls(Set.of("VanityUrl1",randomSteamId3,"VanityUrl2")),Set.of(randomSteamId1,randomSteamId3,randomSteamId2));
            }
        }

    }

    @Nested
    @DisplayName("Given a vanity URL when the service resolves the vanity URL then it will return a Steam user id")
    class resolveVanityUrlTests {
        @Test
        @DisplayName("Given a list containing a single vanity URL when the service resolves the vanity URL then it will return a list containing the Steam user id")
        void GivenAListContainingASingleVanityUrlWhenTheServiceResolvesTheVanityUrlThenItWillReturnAListContainingTheSteamUserId() {
            assertTrue(userService.validateSteamIdsAndVanityUrls(Set.of("SomeVanityUrl", "77561198045206297", "KalmanRobert", "76561197979721079")).isEmpty());
        }

        @Test
        @DisplayName("Given a list containing a multiple vanity URLs when the service resolves the vanity URLs then it will return a list of Steam user ids")
        void GivenAListContainingAMultipleVanityUrlsWhenTheServiceResolvesTheVanityUrlsThenItWillReturnAListOfSteamUserIds() {
            assertTrue(userService.validateSteamIdsAndVanityUrls(Set.of("SomeVanityUrl", "77561198045206297", "KalmanRobert", "76561197979721079")).isEmpty());
        }

        @Test
        @DisplayName("Given a list containing a mix of both vanity URLs and Steam User Ids when the service resolves the vanity URLs then it will return a list of Steam user ids")
        void GivenAListContainingAMixOfBothVanityUrlsAndSteamUserIdsWhenTheServiceResolvesTheVanityUrlsThenItWillReturnAListOfSteamUserIds() {
            assertTrue(userService.validateSteamIdsAndVanityUrls(Set.of("SomeVanityUrl", "77561198045206297", "KalmanRobert", "76561197979721079")).isEmpty());
        }
    }


    private Game createExampleGame(String gameId) {
        Game game = new Game();
        game.setAppid(gameId);
        return game;
    }
}