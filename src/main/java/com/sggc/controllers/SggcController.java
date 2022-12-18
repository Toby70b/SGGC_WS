package com.sggc.controllers;

import com.sggc.exceptions.*;
import com.sggc.models.Game;
import com.sggc.services.VanityUrlService;
import com.sggc.validation.ValidationResult;
import com.sggc.models.sggc.SggcResponse;
import com.sggc.models.steam.request.GetCommonGamesRequest;
import com.sggc.services.GameService;
import com.sggc.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/*
 * Represents the controller for the SGGC api. Under the URL api/sggc.
 */
@Log4j2
@RestController
@RequestMapping(SggcController.SGGC_API_URI)
@RequiredArgsConstructor
public class SggcController {
    public static final String SGGC_API_URI = "api/sggc";
    private final GameService gameService;
    private final UserService userService;
    private final VanityUrlService vanityUrlService;

    /**
     * POST endpoint that, when given a list of user id's returns the Steam games owned by all users, contains a flag to exclude multiplayer games
     *
     * @param request the request object containing information such as user id's to search and whether multiplayer games should be excluded
     * @return a response object containing a collection of games that are mutually owned by all users specified in the request
     * @throws SecretRetrievalException if an error occurs trying to retrieve a secret key from AWS secrets manager in
     * this case the controller's advice will return a 500 error response
     */
    @PostMapping(value = "/")
    public ResponseEntity<SggcResponse> getGamesAllUsersOwn(@Valid @RequestBody GetCommonGamesRequest request) throws SecretRetrievalException {
        log.info("Request received [{}]", request);
        Set<String> steamIds = request.getSteamIds();
        List<ValidationResult> validationErrorList = vanityUrlService.validateSteamIdsAndVanityUrls(steamIds);
        if (!validationErrorList.isEmpty()) {
            SggcResponse response = new SggcResponse(false, new ValidationException(validationErrorList).toApiError());
            log.info("Error occurred when validation request object returning 400 error response with body [{}]", response);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        Set<String> resolvedSteamUserIds;
        try {
            resolvedSteamUserIds = vanityUrlService.resolveVanityUrls(steamIds);
        } catch (VanityUrlResolutionException ex) {
            SggcResponse response = new SggcResponse(false, ex.toApiError());
            log.info("Error occurred while trying to resolve Vanity url returning 404 error response with body [{}]", response);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Set<String> commonGameIdsBetweenUsers;
        try {
            commonGameIdsBetweenUsers = userService.getIdsOfGamesOwnedByAllUsers(resolvedSteamUserIds);
        } catch (UserHasNoGamesException | TooFewSteamIdsException ex) {
            SggcResponse response = new SggcResponse(false, ex.toApiError());
            log.info("Error occurred while trying to find user's owned games returning 404 error response with body [{}]", response);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Set<Game> commonGames = gameService.findGamesById(commonGameIdsBetweenUsers, request.isMultiplayerOnly());
        log.info("Response successful");
        return new ResponseEntity<>(new SggcResponse(true, commonGames), HttpStatus.OK);
    }
}