package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.exceptions.VanityUrlResolutionException;
import com.sggc.models.Game;
import com.sggc.models.User;
import com.sggc.validation.ValidationResult;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import com.sggc.repositories.UserRepository;
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

import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;
import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SteamRequestService steamRequestService;

    @Mock
    private Clock clock;

    @Mock
    private VanityUrlService vanityUrlService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("If provided with a list of user ids it will return a list of game ids of games owned by all users")
    class GetIdsOfGamesOwnedByAllUsersTests {

        @Test
        @DisplayName("If provided with an empty list it will throw an exception")
        void ifProvidedWithAnEmptyListItWillThrowAnExceptionWithAnAppropriateMessage() {
            assertThrows(TooFewSteamIdsException.class, () -> userService.getIdsOfGamesOwnedByAllUsers(new HashSet<>()));
        }

        @Test
        @DisplayName("If less than two users are provided it will throw an exception")
        void IfLessThanTwoUsersAreProvidedItWillThrowAnExceptionWithAnAppropriateMessage() {
            assertThrows(TooFewSteamIdsException.class, () -> userService.getIdsOfGamesOwnedByAllUsers(Set.of("1")));
        }

        @Test
        @DisplayName("If one of the users provided does not own any games it will throw an exception with an appropriate message")
        void ifOneOfTheUsersProvidedDoesNotOwnAnyGamesItWillThrowAnExceptionWithAnAppropriateMessage() throws SecretRetrievalException {

            User user2 = new User();
            HashSet<String> set2 = new HashSet<>();
            set2.add("4");
            set2.add("2");
            set2.add("6");
            user2.setOwnedGameIds(set2);

            //Depending on which id is used first Mockito could throw a UnnecessaryStubbingException, since we don't care
            //about order make the below mocks lenient
            lenient().when(userRepository.findById("1")).thenReturn(Optional.empty());
            lenient().when(userRepository.findById("2")).thenReturn(Optional.of(user2));

            GetOwnedGamesResponse mockGetOwnedGamesResponse1 = new GetOwnedGamesResponse();
            GetOwnedGamesResponse.Response getOwnedGamesResponseDetails = new GetOwnedGamesResponse.Response();
            getOwnedGamesResponseDetails.setGameCount(0);
            mockGetOwnedGamesResponse1.setResponse(getOwnedGamesResponseDetails);

            when(steamRequestService.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse1);
            UserHasNoGamesException exception =
                    assertThrows(UserHasNoGamesException.class, () -> userService.getIdsOfGamesOwnedByAllUsers(Set.of("1", "2")));
            assertEquals(exception.getUserId(), "1");

        }


        @Nested
        @DisplayName("All users own at least one game")
        class AllUsersOwnOneGameTests {
            @Test
            @DisplayName("If provided with a list of users who own one common game it will return a list of common games")
            void ifProvidedWithAListOfUsersWhoOwnOneCommonGameItWillReturnThatGame() throws UserHasNoGamesException, SecretRetrievalException, TooFewSteamIdsException {

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
            void ifProvidedWithAListOfUsersWhoOwnMultipleCommonGameItWillReturnThoseGames() throws UserHasNoGamesException, SecretRetrievalException, TooFewSteamIdsException {
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
            void ifProvidedWithAListOfUsersWhoDontOwnAnyCommonGamesItWillReturnAnEmptyList() throws UserHasNoGamesException, SecretRetrievalException, TooFewSteamIdsException {
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
            when(steamRequestService.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
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
            when(steamRequestService.requestUsersOwnedGamesFromSteamApi("12")).thenReturn(mockGetOwnedGamesResponse);

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
            when(steamRequestService.requestUsersOwnedGamesFromSteamApi("1")).thenReturn(mockGetOwnedGamesResponse);
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
                when(vanityUrlService.validate(generatedString)).thenReturn(expectedValidationError);
                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1, validationResultList.size());
                assertEquals(expectedValidationError, validationResultList.get(0));
            }

            @Test
            @DisplayName("If provided with a vanity URL that is too long it will return a validation error with an appropriate message")
            void ifProvidedWithAVanityUrlThatIsTooLongItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = RandomStringUtils.random(33, true, true);
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
                when(vanityUrlService.validate(generatedString)).thenReturn(expectedValidationError);
                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1, validationResultList.size());
                assertEquals(expectedValidationError, validationResultList.get(0));
            }

            @Test
            @DisplayName("If provided with a vanity URL that contains special characters it will return a validation error with an appropriate message")
            void ifProvidedWithAVanityUrlThatContainsSpecialCharactersItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = "abc123%^&";
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
                when(vanityUrlService.validate(generatedString)).thenReturn(expectedValidationError);
                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1, validationResultList.size());
                assertEquals(expectedValidationError, validationResultList.get(0));
            }

            @Test
            @DisplayName("If provided with multiple invalid Vanity URLs it will return multiple validation errors with appropriate messages")
            void IfProvidedWithMultipleInvalidVanityUrlsItWillReturnMultipleValidationErrorsWithAppropriateMessages() {
                String generatedString1 = "abc123%^&";
                String generatedString2 = RandomStringUtils.random(33, true, true);

                ValidationResult expectedValidationError1 = new ValidationResult(true, generatedString1, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
                ValidationResult expectedValidationError2 = new ValidationResult(true, generatedString2, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
                when(vanityUrlService.validate(generatedString1)).thenReturn(expectedValidationError1);
                when(vanityUrlService.validate(generatedString2)).thenReturn(expectedValidationError2);

                List<ValidationResult> validationResultList = userService.validateSteamIdsAndVanityUrls(Set.of(generatedString1, generatedString2));

                assertEquals(2, validationResultList.size());
                assertTrue(validationResultList.contains(expectedValidationError1));
                assertTrue(validationResultList.contains(expectedValidationError2));
            }
        }
    }

    @Nested
    class VanityUrlResolutionTests {
        @Nested
        @DisplayName("If provided with valid Vanity URL(s) then it will resolve any Vanity URl(s) into Steam user ids")
        class ValidUserIdValidationTests {

            @Test
            @DisplayName("Single Vanity URL")
            void ifProvidedWithAValidSteamIdItWillReturnAnEmptyList() throws SecretRetrievalException, VanityUrlResolutionException {
                String randomSteamId = RandomStringUtils.random(17, true, true);

                ResolveVanityUrlResponse.Response response = new ResolveVanityUrlResponse.Response();
                response.setSuccess(1);
                response.setSteamId(randomSteamId);
                ResolveVanityUrlResponse vanityUrlResponse = new ResolveVanityUrlResponse();
                vanityUrlResponse.setResponse(response);

                when(steamRequestService.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse);
                assertEquals(userService.resolveVanityUrls(Set.of("VanityUrl1")), Set.of(randomSteamId));
            }

            @Test
            @DisplayName("Multiple Vanity URLs")
            void ifProvidedWithAValidVanityUrlItWillReturnAnEmptyList() throws SecretRetrievalException, VanityUrlResolutionException {
                String randomSteamId1 = RandomStringUtils.random(22, true, true);
                String randomSteamId2 = RandomStringUtils.random(17, true, true);

                ResolveVanityUrlResponse.Response response1 = new ResolveVanityUrlResponse.Response();
                response1.setSuccess(1);
                response1.setSteamId(randomSteamId1);
                ResolveVanityUrlResponse vanityUrlResponse1 = new ResolveVanityUrlResponse();
                vanityUrlResponse1.setResponse(response1);

                ResolveVanityUrlResponse.Response response2 = new ResolveVanityUrlResponse.Response();
                response2.setSuccess(1);
                response2.setSteamId(randomSteamId2);
                ResolveVanityUrlResponse vanityUrlResponse2 = new ResolveVanityUrlResponse();
                vanityUrlResponse2.setResponse(response2);

                when(steamRequestService.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse1);
                when(steamRequestService.resolveVanityUrl("VanityUrl2")).thenReturn(vanityUrlResponse2);

                assertEquals(userService.resolveVanityUrls(Set.of("VanityUrl1", "VanityUrl2")), Set.of(randomSteamId1, randomSteamId2));
            }

            @Test
            @DisplayName("Mix of Vanity URLs and Steam user ids")
            void ifProvidedWithAMixtureOfValidVanityUrlsAndSteamIdsItWillReturnAnEmptyList() throws SecretRetrievalException, VanityUrlResolutionException {
                String randomSteamId1 = RandomStringUtils.random(17, true, true);
                String randomSteamId2 = RandomStringUtils.random(18, true, true);
                String randomSteamId3 = "7" + RandomStringUtils.random(16, false, true);

                ResolveVanityUrlResponse.Response response1 = new ResolveVanityUrlResponse.Response();
                response1.setSuccess(1);
                response1.setSteamId(randomSteamId1);
                ResolveVanityUrlResponse vanityUrlResponse1 = new ResolveVanityUrlResponse();
                vanityUrlResponse1.setResponse(response1);

                ResolveVanityUrlResponse.Response response2 = new ResolveVanityUrlResponse.Response();
                response2.setSuccess(1);
                response2.setSteamId(randomSteamId2);
                ResolveVanityUrlResponse vanityUrlResponse2 = new ResolveVanityUrlResponse();
                vanityUrlResponse2.setResponse(response2);

                when(steamRequestService.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse1);
                when(steamRequestService.resolveVanityUrl("VanityUrl2")).thenReturn(vanityUrlResponse2);

                assertEquals(userService.resolveVanityUrls(Set.of("VanityUrl1", randomSteamId3, "VanityUrl2")), Set.of(randomSteamId1, randomSteamId3, randomSteamId2));
            }
        }

        @Test
        @DisplayName("Given a valid Vanity URL that doesn't resolve into a steam id when the service resolves the vanity URL then it will throw a VanityUrlResolutionException including the vanity url")
        void GivenAValidVanityUrlThatDoesntResolveIntoASteamIdWhenTheServiceResolvesTheVanityUrlThenItWillThrowAVanityUrlResolutionExceptionIncludingTheVanityUrl() throws SecretRetrievalException {
            String randomVanityUrl = RandomStringUtils.random(17, true, true);
            ResolveVanityUrlResponse.Response response = new ResolveVanityUrlResponse.Response();
            response.setSuccess(42);
            ResolveVanityUrlResponse vanityUrlResponse = new ResolveVanityUrlResponse();
            vanityUrlResponse.setResponse(response);
            when(steamRequestService.resolveVanityUrl(randomVanityUrl)).thenReturn(vanityUrlResponse);
            VanityUrlResolutionException ex = assertThrows(VanityUrlResolutionException.class, () -> userService.resolveVanityUrls(Set.of(randomVanityUrl)));
            assertEquals(randomVanityUrl, ex.getVanityUrl());
        }

    }

    private Game createExampleGame(String gameId) {
        Game game = new Game();
        game.setAppid(gameId);
        return game;
    }
}