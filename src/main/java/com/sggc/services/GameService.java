package com.sggc.services;

import com.sggc.models.Game;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.models.SteamGameCategory;
import com.sggc.repositories.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Represents a service for interacting with Game objects
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final SteamRequestService steamRequestHandler;

    /**
     * Retrieve details of games from the DB via the game's app id
     *
     * @param gameIds         a collection of game id's to search for
     * @param multiplayerOnly indicates whether non-multiplayer games should be filtered out of the returned collection
     * @return a collection of found games
     */
    public Set<Game> findGamesById(Set<String> gameIds, boolean multiplayerOnly) {
        //Sometimes games have been removed from steam but still appear in users game libraries, thus remove nulls from the list.
        Set<Game> commonGames = gameIds.stream().map(gameRepository::findGameByAppid).filter(Objects::nonNull).collect(Collectors.toSet());
        if (multiplayerOnly) {
            commonGames = commonGames.stream().filter(game -> {
                try {
                    return isGameMultiplayer(game);
                } catch (IOException e) {
                    log.error("Error occurred while trying to determine whether game is multiplayer",e);
                    throw new UncheckedIOException(e);
                }
            }).collect(Collectors.toSet());
        }
        return commonGames;
    }

    /**
     * Determines whether a provided game is considered multiplayer by Steam. If the status is not currently saved
     * within the DB, then a request will be sent to the Steam API to determine whether the game is multiplayer,
     * the result will be stored in the DB
     *
     * @param game the game to check
     * @return true if the game is multiplayer otherwise false
     * @throws IOException if an error occurs requesting the games details from the Steam API
     */
    private boolean isGameMultiplayer(Game game) throws IOException {
        log.debug("Attempting to determine whether game [{}] is multiplayer", game.getAppid());
        if (game.getMultiplayer() != null) {
            log.debug("Game's multiplayer status is known, returning");
            return game.getMultiplayer();
        } else {
            log.debug("Game's multiplayer status is unknown, contacting Steam API");
            GameData parsedResponse = steamRequestHandler.requestAppDetailsFromSteamApi(game.getAppid());
            //Check for presence of multiplayer category
            for (GameCategory category : parsedResponse.getCategories()) {
                if (category.getId() == SteamGameCategory.MULTIPLAYER) {
                    log.debug("Game is multiplayer, store in DB for future reference");
                    game.setMultiplayer(true);
                    gameRepository.save(game);
                    return true;
                }
            }
            log.debug("Game is not multiplayer, store in DB for future reference");
            game.setMultiplayer(false);
            gameRepository.save(game);
            return false;
        }
    }
}


