package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.TooFewSteamIdsException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.exceptions.VanityUrlResolutionException;
import com.sggc.models.Game;
import com.sggc.models.User;
import com.sggc.models.ValidationResult;
import com.sggc.models.steam.response.GetOwnedGamesResponse;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import com.sggc.repositories.UserRepository;
import com.sggc.util.DateUtil;
import com.sggc.validation.SteamVanityUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a service for interacting with User objects
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SteamRequestService steamRequestHandler;
    private final Clock systemClock;

    /**
     * Retrieves the Steam games owned by a user by user id
     *
     * @param userId the Steam id of the user
     * @return a collection of game id's owned by the user
     * @throws UserHasNoGamesException  if the user does not own any games
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    public Set<String> findOwnedGamesByUserId(String userId) throws SecretRetrievalException, UserHasNoGamesException {
        log.debug("Attempting to find user with id: " + userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            log.debug("User with matching id has been found in DB");
            return user.get().getOwnedGameIds();
        } else {
            log.debug("User with matching id hasn't been found in DB, will request details from Steam API");
            Set<String> usersOwnedGameIds = getUsersOwnedGameIds(userId);
            if (usersOwnedGameIds.isEmpty()) {
                log.error("User: [{}] owns no Steam games, throwing exception", userId);
                throw new UserHasNoGamesException(userId);
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
     * @throws TooFewSteamIdsException if userIds contains less than two entries
     */
    public Set<String> getIdsOfGamesOwnedByAllUsers(Set<String> userIds) throws UserHasNoGamesException, SecretRetrievalException, TooFewSteamIdsException {
        if(userIds.size() > 1) {
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
        log.error("Collection of user ids contains less than two entries, throwing exception");
        throw new TooFewSteamIdsException();
    }

    /**
     * Checks whether the Steam user ids and vanity URLs are valid
     *
     * @param userIds the Steam user ids and vanity URLs to validate
     * @return a list of validation errors encountered, will be empty if all input is valid
     */
    public List<ValidationResult> validateSteamIdsAndVanityUrls(Set<String> userIds) {
        SteamVanityUrlValidator vanityUrlValidator = new SteamVanityUrlValidator();
        List<ValidationResult> validationErrors = new ArrayList<>();
        for (String steamId : userIds) {
            ValidationResult validationResult;
            if (!isSteamUserId(steamId)) {
                validationResult = vanityUrlValidator.validate(steamId);
                if (validationResult.isError()) {
                    validationErrors.add(validationResult);
                }
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
    public Set<String> resolveVanityUrls(Set<String> steamIds) throws SecretRetrievalException, VanityUrlResolutionException {
        Set<String> resolvedIds = new HashSet<>(steamIds);
        for (String steamId : steamIds) {
            if (!isSteamUserId(steamId)) {
                resolvedIds.remove(steamId);
                resolvedIds.add(resolveVanityURL(steamId));
            }
        }
        return resolvedIds;
    }

    /**
     * Returns a collection of the ids of Steam games owned by a user by user id, empty if the user owns no Steam games
     *
     * @param userId the Steam id of the user
     * @return a collection of strings representing ids of Steam games, empty if the user owns no Steam games
     * @throws SecretRetrievalException if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                  secrets manager
     */
    private Set<String> getUsersOwnedGameIds(String userId) throws SecretRetrievalException {
        Set<String> gameIdList = new HashSet<>();
        GetOwnedGamesResponse.Response response = steamRequestHandler.requestUsersOwnedGamesFromSteamApi(userId).getResponse();
        return response.getGames() != null ? parseGameIdsFromResponse(response) : gameIdList;
    }

    /**
     * Resolves a vanity URL into a Steam user id
     *
     * @param vanityUrl the vanity URL to resolve
     * @return the resolved Steam user id, or null if the request was unsuccessful
     * @throws SecretRetrievalException     if an error occurs attempting to retrieve the Steam API key secret from AWS
     *                                      secrets manager
     * @throws VanityUrlResolutionException if the resolution request from the Steam API responds other than success
     */
    private String resolveVanityURL(String vanityUrl) throws SecretRetrievalException, VanityUrlResolutionException {
        ResolveVanityUrlResponse.Response response = steamRequestHandler.resolveVanityUrl(vanityUrl).getResponse();
        if (!response.isSuccess()) {
            throw new VanityUrlResolutionException(vanityUrl);
        }
        return response.getSteamId();
    }


    /**
     * Parses the Steam GetOwnedGamesResponse into a collection of game id's
     *
     * @param response the Steam GetOwnedGameResponse
     * @return a collection of strings representing ids of Steam games
     */
    private Set<String> parseGameIdsFromResponse(GetOwnedGamesResponse.Response response) {
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
     * Checks whether a String is a Steam user id as opposed to a Steam vanity URL. A steam id will be numeric, 17 characters long and begins with 7, 8 or 9
     *
     * @param steamId the String to check
     * @return true if the String is numeric, 17 characters long and begins with 7, 8 or 9
     */
    private boolean isSteamUserId(String steamId) {
        boolean beginsWithSteamIdNumber = steamId.startsWith("7") || steamId.startsWith("8") || steamId.startsWith("9");
        boolean isSeventeenCharactersLong = steamId.length() == 17;
        boolean isNumeric = StringUtils.isNumeric(steamId);
        return beginsWithSteamIdNumber && isSeventeenCharactersLong && isNumeric;
    }

}
