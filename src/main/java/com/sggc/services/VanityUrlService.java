package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.VanityUrlResolutionException;
import com.sggc.infrastructure.SteamRequestSender;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import com.sggc.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a service for interacting with Steam Vanity URLs
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class VanityUrlService {

    public static final String VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE = "Vanity URL must be between 3 and 32 characters long";
    public static final String VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE = "Vanity URL must not contain special characters";

    private final SteamRequestSender steamRequestSender;

    /**
     * Checks whether the Steam user ids and vanity URLs are valid
     *
     * @param userIds the Steam user ids and vanity URLs to validate
     * @return a list of validation errors encountered, will be empty if all input is valid
     */
    public List<ValidationResult> validateSteamIdsAndVanityUrls(Set<String> userIds) {
        List<ValidationResult> validationErrors = new ArrayList<>();
        for (String steamId : userIds) {
            ValidationResult validationResult;
            if (!isSteamUserId(steamId)) {
                validationResult = validate(steamId);
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
     * Validate a Steam Vanity URL
     *
     * @param vanityUrl the Steam Vanity URL to validate against
     * @return a ValidationResult object, indicating if the validation failed, and if so, why.
     */
    private ValidationResult validate(String vanityUrl) {
        if (!withinRequiredLength(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
        }
        if (!StringUtils.isAlphanumeric(vanityUrl)) {
            return new ValidationResult(true, vanityUrl, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
        }
        return new ValidationResult(false);
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
        ResolveVanityUrlResponse.Response response = steamRequestSender.resolveVanityUrl(vanityUrl).getResponse();
        if (!response.isSuccess()) {
            throw new VanityUrlResolutionException(vanityUrl);
        }
        return response.getSteamId();
    }

    /**
     * Checks whether a String is within 3 and 32 characters long i.e. The limits of a Steam vanity URL
     *
     * @param vanityUrl the Steam vanity URL String to validate
     * @return true if the length of the vanity URL is within 3 and 32 characters, otherwise false
     */
    private boolean withinRequiredLength(String vanityUrl) {
        return vanityUrl.length() > 2 && vanityUrl.length() < 33;
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
