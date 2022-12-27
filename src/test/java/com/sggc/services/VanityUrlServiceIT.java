package com.sggc.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import testsupport.constants.SteamWebTestConstants;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.VanityUrlResolutionException;
import testsupport.extentions.SggcLocalStackCleanerExtension;
import testsupport.extentions.WiremockCleanerExtension;
import testsupport.util.AwsSecretsManagerTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static testsupport.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;
import static testsupport.containers.SggcLocalStackContainer.ENABLED_SERVICES;
import static testsupport.util.TestClientInitializer.initializeAwsSecretsManagerClient;
import static testsupport.util.TestClientInitializer.initializeWiremockClient;
import static org.junit.jupiter.api.Assertions.*;

public class VanityUrlServiceIT extends AbstractIntegrationTest {

    @RegisterExtension
    WiremockCleanerExtension wiremockCleanerExtension = new WiremockCleanerExtension(wiremockContainer.getFirstMappedPort());

    @RegisterExtension
    SggcLocalStackCleanerExtension localStackCleanerExtension
            = new SggcLocalStackCleanerExtension(localStackContainer.getFirstMappedPort(), List.of(ENABLED_SERVICES));

    @Autowired
    public VanityUrlService vanityUrlService;

    private static WireMock wiremockClient;
    private static AWSSecretsManager secretsManagerClient;

    @BeforeAll
    static void beforeAll() {
        wiremockClient = initializeWiremockClient(wiremockContainer.getFirstMappedPort());
        secretsManagerClient = initializeAwsSecretsManagerClient(localStackContainer.getFirstMappedPort());
    }

    @Nested
    @DisplayName("If provided with a Vanity URL then the service will attempt to resolve it into it's corresponding Steam user id")
    class VanityUrlResolutionTests {

        @Test
        @DisplayName("If provided with a Vanity URL then attempt to resolve the Vanity URL's corresponding Steam user id and return it")
        void IfProvidedWithAVanityUrlThenAttemptToResolveTheVanityUrlsCorrespondingSteamUserIdAndReturnIt() throws SecretRetrievalException, VanityUrlResolutionException {
            AwsSecretsManagerTestUtil.createMockSteamApiKey(secretsManagerClient);

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
            AwsSecretsManagerTestUtil.createMockSteamApiKey(secretsManagerClient);

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
