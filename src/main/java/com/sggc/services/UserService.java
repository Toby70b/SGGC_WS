package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.User;
import com.sggc.models.ValidationError;
import com.sggc.models.steam.response.GetOwnedGamesResponseDetails;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import com.sggc.repositories.UserRepository;
import com.sggc.util.DateUtil;
import com.sggc.util.SteamRequestHandler;
import com.sggc.validation.SteamIdValidator;
import com.sggc.validation.SteamVanityUrlValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a service for interacting with User objects
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);
    private final SteamRequestHandler steamRequestHandler;
    private final Clock systemClock;

    //TODO: I dont think its best to throw an exception here, as the method is just doing its job and still functions,
    //throw in the calling method

    /**
     * Retrieves the Steam games owned by a user by user id
     *
     * @param userId the Steam id of the user
     * @return a collection of game id's owned by the user
     * @throws UserHasNoGamesException  if the user does not own any games
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    public Set<String> findOwnedGamesByUserId(String userId) throws UserHasNoGamesException, SecretRetrievalException {
        logger.debug("Attempting to find user with id: " + userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            logger.debug("User with matching id has been found in DB");
            return user.get().getOwnedGameIds();
        } else {
            logger.debug("User with matching id hasn't been found in DB, will request details from Steam API");
            Set<String> usersOwnedGameIds = new HashSet<>();
            try {
                usersOwnedGameIds = getUsersOwnedGameIds(userId);
            } catch (UserHasNoGamesException e) {
                e.setUserId(userId);
                throw e;
            } catch (SecretRetrievalException e) {
                throw e;
            }
            /*
                Cache the user to speed up searches. in a proper prod environment this would be cleaned regularly
                to catch changes in users owned games
            */
            userRepository.save(new User(userId, usersOwnedGameIds, calculateUserRemovalDate()));
            userRepository.findAll();
            return usersOwnedGameIds;
        }
    }

    /**
     * Returns a collection of game id's owned by each user specified
     *
     * @param userIds user id's to determine common games
     * @return a collection of strings representing ids of Steam games owned by all users
     * @throws UserHasNoGamesException  if the user does not own any games
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    public Set<String> getIdsOfGamesOwnedByAllUsers(Set<String> userIds) throws UserHasNoGamesException, SecretRetrievalException {
        Set<String> combinedGameIds = new HashSet<>();
        for (String userId : userIds) {
            Set<String> usersOwnedGameIds = findOwnedGamesByUserId(userId);
            if (combinedGameIds.isEmpty()) {
                combinedGameIds = usersOwnedGameIds;
            } else {
                combinedGameIds.retainAll(usersOwnedGameIds);
            }
        }
        return combinedGameIds;
    }

    /**
     * Checks whether the Steam user ids and vanity URLs are valid
     *
     * @param userIds the Steam user ids and vanity URLs to validate
     * @return a list of validation errors encountered, will be empty if all input is valid
     */
    public List<ValidationError> validateSteamIdsAndVanityUrls(Set<String> userIds) {
        SteamVanityUrlValidator vanityUrlValidator = new SteamVanityUrlValidator();
        SteamIdValidator idValidator = new SteamIdValidator();
        List<ValidationError> validationErrors = new ArrayList<>();
        for (String steamId : userIds) {
            ValidationError validationError;
            if (isSteamUserId(steamId)) {
                validationError = idValidator.validate(steamId);
            } else {
                validationError = vanityUrlValidator.validate(steamId);
            }
            if (validationError != null) {
                validationErrors.add(validationError);
            }
        }
        return validationErrors;
    }


    /**
     * Given a list containing one or more Vanity URLs returns a list of their equivalent Steam user id
     *
     * @param steamIds a list containing Steam user ids and vanity URLs
     * @return a list of Steam user ids with all existing vanity URLs replaced with their equivalent Steam user id
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    public Set<String> resolveVanityUrls(Set<String> steamIds) throws SecretRetrievalException {
        Set<String> resolvedIds = new HashSet<>(steamIds);
        for (String steamId : steamIds) {
            if (!isSteamUserId(steamId)) {
                resolvedIds.remove(steamId);
                resolvedIds.add(resolveVanityURL(steamId));
            }
        }
        return resolvedIds;
    }


    //TODO: remove exception throwing here

    /**
     * Returns a collection of the ids of Steam games owned by a user by user id
     *
     * @param userId the Steam id of the user
     * @return a collection of strings representing ids of Steam games
     * @throws UserHasNoGamesException  if the user does not own any games
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    private Set<String> getUsersOwnedGameIds(String userId) throws UserHasNoGamesException, SecretRetrievalException {
        Set<String> gameIdList;
        GetOwnedGamesResponseDetails response = steamRequestHandler.requestUsersOwnedGamesFromSteamApi(userId).getResponse();
        if (response.getGameCount() == 0) {
            throw new UserHasNoGamesException();
        }
        gameIdList = parseGameIdsFromResponse(response);
        return gameIdList;
    }

    /**
     * Resolves a vanity URL into a Steam user id
     *
     * @param vanityUrl the vanity URL to resolve
     * @return the resolved Steam user id, or null if the request was unsuccessful
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    private String resolveVanityURL(String vanityUrl) throws SecretRetrievalException {
        ResolveVanityUrlResponse.Response response = steamRequestHandler.resolveVanityUrl(vanityUrl).getResponse();
        return !response.isSuccess() ? response.getSteamId() : null;
    }


    /**
     * Parses the Steam GetOwnedGamesResponse into a collection of game id's
     *
     * @param response the Steam GetOwnedGameResponse
     * @return a collection of strings representing ids of Steam games
     */
    private Set<String> parseGameIdsFromResponse(GetOwnedGamesResponseDetails response) {
        return response.getGames()
                .stream()
                .map(Game::getAppid)
                .collect(Collectors.toSet());
    }

    /**
     * Calculates the time 24 hours from current time represented in seconds
     *
     * @return the time 24 hours from now represented in seconds
     */
    private long calculateUserRemovalDate() {
        DateUtil dateUtil = new DateUtil(systemClock);
        return dateUtil.getTimeOneDayFromNow().getEpochSecond();
    }

    /**
     * Checks whether a String is a Steam user id as opposed to a Steam vanity URL. As both a Steam id and vanity URL can be numeric and 17 characters the only way I'm aware to confirm its a Steam id is if it starts with 7, 8 or 9
     *
     * @param steamId the String to check
     * @return true if the String begins with 7, 8 or 9, otherwise false
     */
    private boolean isSteamUserId(String steamId) {
        return steamId.startsWith("7") || steamId.startsWith("8") || steamId.startsWith("9");
    }

}
