package com.sggc.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.constants.SteamWebTestConstants;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.UserHasNoGamesException;
import com.sggc.exceptions.VanityUrlResolutionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.sggc.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;
import static org.junit.jupiter.api.Assertions.*;

public class VanityUrlServiceIT extends AbstractIntegrationTest {

    @Autowired
    public VanityUrlService vanityUrlService;

    @Nested
    @DisplayName("If provided with a Vanity URL then the service will attempt to resolve it into it's corresponding Steam user id")
    class VanityUrlResolutionTests{

        @Test
        @DisplayName("If provided with a Vanity URL then attempt to resolve the Vanity URL's corresponding Steam user id and return it")
        void IfProvidedWithAVanityUrlThenAttemptToResolveTheVanityUrlsCorrespondingSteamUserIdAndReturnIt() throws SecretRetrievalException, VanityUrlResolutionException {
            secretsManagerTestSupporter.createMockSteamApiKey();

            String mockVanityUrl = "SomeVanityUrl";
            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.RESOLVE_VANITY_URL_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.VANITY_URL_QUERY_PARAM_KEY, equalTo(mockVanityUrl))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/resolve-vanity-url/successful-response-1.json")
                            ).build()
            );

            String expectedResolvedSteamId = "76561197979721089";

            Set<String> resolvedSteamIds = vanityUrlService.resolveVanityUrls(Set.of(mockVanityUrl));

            assertEquals(1, resolvedSteamIds.size());
            assertTrue(resolvedSteamIds.contains(expectedResolvedSteamId));
        }

        @Test
        @DisplayName("If a Vanity URL cannot be resolved into a Steam user id then throw an appropriate exception")
        void IfAVanityUrlCannotBeResolvedIntoASteamUserIdThenThrowAnAppropriateException() {
            secretsManagerTestSupporter.createMockSteamApiKey();

            String mockVanityUrl = "SomeVanityUrl";
            wiremockClient.register(
                    WireMock.get(urlPathEqualTo(SteamWebTestConstants.Endpoints.RESOLVE_VANITY_URL_ENDPOINT))
                            .withQueryParam(SteamWebTestConstants.QueryParams.VANITY_URL_QUERY_PARAM_KEY, equalTo(mockVanityUrl))
                            .withQueryParam(SteamWebTestConstants.QueryParams.STEAM_KEY_QUERY_PARAM_KEY, equalTo(MOCK_STEAM_API_KEY_VALUE))
                            .willReturn(ok()
                                    .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                                    .withBodyFile("steam-api/resolve-vanity-url/unsuccessful-response-1.json")
                            ).build()
            );

            VanityUrlResolutionException expectedException =
                    assertThrows(VanityUrlResolutionException.class, () ->
                            vanityUrlService.resolveVanityUrls(Set.of(mockVanityUrl)));

            assertEquals("SomeVanityUrl", expectedException.getVanityUrl());
        }
    }


}
