package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.GetOwnedGamesResponseDetails;
import com.sggc.models.User;
import com.sggc.repositories.UserRepository;
import com.sggc.util.DateUtil;
import com.sggc.util.SteamRequestHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);
    private final SteamRequestHandler steamRequestHandler;
    private final Clock systemClock;

    public Set<String> findOwnedGamesByUserId(String userId) throws UserHasNoGamesException, SecretRetrievalException {
        logger.debug("Attempting to find user with id: " + userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            logger.debug("User with matching id has been found in Mongo Repo");
            return user.get().getOwnedGameIds();
        } else {
            logger.debug("User with matching id hasnt been found in Mongo Repo, will request details from Steam API");
            Set<String> usersOwnedGameIds = new HashSet<>();
            try {
                usersOwnedGameIds = getUsersOwnedGameIds(userId);
            } catch (UserHasNoGamesException e) {
                e.setUserId(userId);
                throw e;
            }
            catch (SecretRetrievalException e){
                throw e;
            }
            /*
                Cache the user to speed up searches. in a proper prod environment this would be cleaned regularly
                to catch changes in users owned games
            */
            userRepository.save(new User(userId, usersOwnedGameIds,calculateUserRemovalDate()));
            return usersOwnedGameIds;
        }
    }

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

    private Set<String> getUsersOwnedGameIds(String userId) throws UserHasNoGamesException, SecretRetrievalException {
        Set<String> gameIdList;
        GetOwnedGamesResponseDetails response = steamRequestHandler.requestUsersOwnedGamesFromSteamApi(userId).getResponse();
        if(response.getGameCount()==0){
            throw new UserHasNoGamesException();
        }
        gameIdList = parseGameIdsFromResponse(response);
        return gameIdList;
    }

    private Set<String> parseGameIdsFromResponse(GetOwnedGamesResponseDetails response) {
        return response.getGames()
                .stream()
                .map(Game::getAppid)
                .collect(Collectors.toSet());
    }

    /**
     * Sets the removalDate to be 24 hours from current time represented in milliseconds
     * @return the time 24 hours from now represented in milliseconds
     */
    private long calculateUserRemovalDate() {
        DateUtil dateUtil = new DateUtil(systemClock);
        return dateUtil.getTimeOneDayFromNow().toEpochMilli();
    }
}
