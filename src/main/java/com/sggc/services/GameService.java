package com.sggc.services;

import com.sggc.models.Game;
import com.sggc.models.GameCategory;
import com.sggc.models.GameData;
import com.sggc.repositories.GameRepository;
import com.sggc.util.SteamRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sggc.util.CommonUtil.MULTIPLAYER_ID;

/**
 * Represents a service for interacting with Game objects
 */
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final SteamRequestHandler steamRequestHandler;

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
                    //TODO improve error handling here
                } catch (IOException e) {
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
        if (game.getMultiplayer() != null) {
            return game.getMultiplayer();
        } else {
            GameData parsedResponse = steamRequestHandler.requestAppDetailsFromSteamApi(game.getAppid());
            //Check for presence of multiplayer category
            for (GameCategory category : parsedResponse.getCategories()) {
                if (category.getId() == MULTIPLAYER_ID) {
                    game.setMultiplayer(true);
                    gameRepository.save(game);
                    return true;
                }
            }
            game.setMultiplayer(false);
            gameRepository.save(game);
            return false;
        }
    }
}


