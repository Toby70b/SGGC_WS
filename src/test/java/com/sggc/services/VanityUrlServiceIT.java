package com.sggc.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sggc.AbstractIntegrationTest;
import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.VanityUrlResolutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import util.clientfactories.AWSSecretsManagerClientFactory;
import util.constants.SteamWebTestConstants;
import util.extentions.SggcLocalStackCleanerExtension;
import util.extentions.WiremockCleanerExtension;
import util.util.AwsSecretsManagerTestUtil;

import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SECRETSMANAGER;
import static util.constants.SecretsTestConstants.MOCK_STEAM_API_KEY_VALUE;

public class VanityUrlServiceIT extends AbstractIntegrationTest {

    @RegisterExtension
    WiremockCleanerExtension wiremockCleanerExtension = new WiremockCleanerExtension(wiremockContainer.getFirstMappedPort());

    @RegisterExtension
    SggcLocalStackCleanerExtension localStackCleanerExtension
            = new SggcLocalStackCleanerExtension(localStackContainer.getFirstMappedPort(), List.of(SECRETSMANAGER));

    @Autowired
    public VanityUrlService vanityUrlService;

    private static WireMock wiremockClient;
    private static AWSSecretsManager secretsManagerClient;

    @BeforeAll
    static void beforeAll() {
        wiremockClient = new WireMock("localhost", wiremockContainer.getFirstMappedPort());
        secretsManagerClient = new AWSSecretsManagerClientFactory().createClient(localStackContainer.getFirstMappedPort());
    }


    @Test
    @DisplayName("Given a valid Vanity URL which corresponds to a Steam user ID, when the service to resolves the Vanity URL, " +
            "then the corresponding Steam user ID will be returned.")
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
    @DisplayName("Given a valid Vanity URL which does not correspond to a Steam user ID, when the service to resolves the Vanity URL, " +
            "then an appropriate exception will be thrown.")
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



