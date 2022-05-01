package com.sggc.controllers;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.models.Game;
import com.sggc.models.GetCommonGamesRequest;
import com.sggc.models.SGGCResponse;
import com.sggc.models.ValidationError;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/*
 * Represents the controller for the SGGC api. Under the URL api/sggc. Currently the only controller to provide functionality
 */
@RestController
@RequestMapping("api/sggc")
@RequiredArgsConstructor
public class SGGCController {
    private final GameService gameService;
    private final UserService userService;

    /**
     * POST endpoint that, when given a list of user id's returns the Steam games owned by all users, contains a flag to exclude multiplayer games
     *
     * @param request the request object containing information such as user id's to search and whether multiplayer games should be excluded
     * @return a response object containing a collection of games that are mutually owned by all users specified in the request
     */
    @CrossOrigin
    @PostMapping(value = "/")
    public ResponseEntity<SGGCResponse> getGamesAllUsersOwn(@Valid @RequestBody GetCommonGamesRequest request) throws UserHasNoGamesException, SecretRetrievalException {
        Set<String> steamUserIds = request.getSteamIds();
        List<ValidationError> validationErrorList = userService.validateSteamIdsAndVanityUrls(steamUserIds);
        if(!validationErrorList.isEmpty()){
            return new ResponseEntity<>(new SGGCResponse(false,validationErrorList), HttpStatus.BAD_REQUEST);
        }
        //Call resolve id logic
        Set<String> commonGameIdsBetweenUsers = userService.getIdsOfGamesOwnedByAllUsers(steamUserIds);
        Set<Game> commonGames = gameService.findGamesById(commonGameIdsBetweenUsers, request.isMultiplayerOnly());
        return new ResponseEntity<>(new SGGCResponse(true,commonGames), HttpStatus.OK);
    }

}
