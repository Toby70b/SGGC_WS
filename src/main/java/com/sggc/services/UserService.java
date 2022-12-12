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
import com.sggc.util.DateUtil;
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
    private final VanityUrlService vanityUrlService;
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
        log.debug("Attempting to find user with id [{}]",userId);
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
            userRepository.save(new User(userId, usersOwnedGameIds, calculateUserRemovalDate()));
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


}
