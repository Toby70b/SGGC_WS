package com.sggc.services;

import com.sggc.exceptions.SecretRetrievalException;
import com.sggc.exceptions.VanityUrlResolutionException;
import com.sggc.infrastructure.SteamRequestSender;
import com.sggc.models.steam.response.ResolveVanityUrlResponse;
import com.sggc.validation.ValidationResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;
import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VanityUrlServiceTest {

    @Mock
    private SteamRequestSender steamRequestSender;

    @InjectMocks
    private VanityUrlService vanityUrlService;

    @Nested
    class VanityUrlResolutionTests {
        @Nested
        @DisplayName("If provided with valid Vanity URL(s) then it will resolve any Vanity URl(s) into Steam user ids")
        class ValidUserIdValidationTests {

            @Test
            @DisplayName("Single Vanity URL")
            void ifProvidedWithAValidSteamIdItWillReturnAnEmptyList() throws SecretRetrievalException, VanityUrlResolutionException {
                String randomSteamId = RandomStringUtils.random(17, true, true);

                ResolveVanityUrlResponse.Response response = new ResolveVanityUrlResponse.Response();
                response.setSuccess(1);
                response.setSteamId(randomSteamId);
                ResolveVanityUrlResponse vanityUrlResponse = new ResolveVanityUrlResponse();
                vanityUrlResponse.setResponse(response);

                when(steamRequestSender.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse);
                assertEquals(vanityUrlService.resolveVanityUrls(Set.of("VanityUrl1")), Set.of(randomSteamId));
            }

            @Test
            @DisplayName("Multiple Vanity URLs")
            void ifProvidedWithAValidVanityUrlItWillReturnAnEmptyList() throws SecretRetrievalException, VanityUrlResolutionException {
                String randomSteamId1 = RandomStringUtils.random(22, true, true);
                String randomSteamId2 = RandomStringUtils.random(17, true, true);

                ResolveVanityUrlResponse.Response response1 = new ResolveVanityUrlResponse.Response();
                response1.setSuccess(1);
                response1.setSteamId(randomSteamId1);
                ResolveVanityUrlResponse vanityUrlResponse1 = new ResolveVanityUrlResponse();
                vanityUrlResponse1.setResponse(response1);

                ResolveVanityUrlResponse.Response response2 = new ResolveVanityUrlResponse.Response();
                response2.setSuccess(1);
                response2.setSteamId(randomSteamId2);
                ResolveVanityUrlResponse vanityUrlResponse2 = new ResolveVanityUrlResponse();
                vanityUrlResponse2.setResponse(response2);

                when(steamRequestSender.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse1);
                when(steamRequestSender.resolveVanityUrl("VanityUrl2")).thenReturn(vanityUrlResponse2);

                assertEquals(vanityUrlService.resolveVanityUrls(Set.of("VanityUrl1", "VanityUrl2")), Set.of(randomSteamId1, randomSteamId2));
            }

            @Test
            @DisplayName("Mix of Vanity URLs and Steam user ids")
            void ifProvidedWithAMixtureOfValidVanityUrlsAndSteamIdsItWillReturnAnEmptyList() throws SecretRetrievalException, VanityUrlResolutionException {
                String randomSteamId1 = RandomStringUtils.random(17, true, true);
                String randomSteamId2 = RandomStringUtils.random(18, true, true);
                String randomSteamId3 = "7" + RandomStringUtils.random(16, false, true);

                ResolveVanityUrlResponse.Response response1 = new ResolveVanityUrlResponse.Response();
                response1.setSuccess(1);
                response1.setSteamId(randomSteamId1);
                ResolveVanityUrlResponse vanityUrlResponse1 = new ResolveVanityUrlResponse();
                vanityUrlResponse1.setResponse(response1);

                ResolveVanityUrlResponse.Response response2 = new ResolveVanityUrlResponse.Response();
                response2.setSuccess(1);
                response2.setSteamId(randomSteamId2);
                ResolveVanityUrlResponse vanityUrlResponse2 = new ResolveVanityUrlResponse();
                vanityUrlResponse2.setResponse(response2);

                when(steamRequestSender.resolveVanityUrl("VanityUrl1")).thenReturn(vanityUrlResponse1);
                when(steamRequestSender.resolveVanityUrl("VanityUrl2")).thenReturn(vanityUrlResponse2);

                assertEquals(vanityUrlService.resolveVanityUrls(Set.of("VanityUrl1", randomSteamId3, "VanityUrl2")), Set.of(randomSteamId1, randomSteamId3, randomSteamId2));
            }
        }

        @Test
        @DisplayName("Given a valid Vanity URL that doesn't resolve into a steam id when the service resolves the vanity URL then it will throw a VanityUrlResolutionException including the vanity url")
        void GivenAValidVanityUrlThatDoesntResolveIntoASteamIdWhenTheServiceResolvesTheVanityUrlThenItWillThrowAVanityUrlResolutionExceptionIncludingTheVanityUrl() throws SecretRetrievalException {
            String randomVanityUrl = RandomStringUtils.random(17, true, true);
            ResolveVanityUrlResponse.Response response = new ResolveVanityUrlResponse.Response();
            response.setSuccess(42);
            ResolveVanityUrlResponse vanityUrlResponse = new ResolveVanityUrlResponse();
            vanityUrlResponse.setResponse(response);
            when(steamRequestSender.resolveVanityUrl(randomVanityUrl)).thenReturn(vanityUrlResponse);
            VanityUrlResolutionException ex = assertThrows(VanityUrlResolutionException.class, () -> vanityUrlService.resolveVanityUrls(Set.of(randomVanityUrl)));
            assertEquals(randomVanityUrl, ex.getVanityUrl());
        }

    }

    @Nested
    class ValidationTests {

        @Test
        @DisplayName("If provided with a valid vanity URL it will return no validation errors")
        void ifProvidedWithAValidVanityUrlItWillReturnNoValidationErrrors() {
            String generatedString = RandomStringUtils.random(8, true, true);
            ValidationResult expectedValidationError = new ValidationResult(false, generatedString, null);
            List<ValidationResult> validationResultList = vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
            assertTrue(validationResultList.isEmpty());
        }

        @Nested
        @DisplayName("If provided with a invalid vanity URL it will return a list of validation errors")
        class InvalidUserIdValidationTests {
            @Test
            @DisplayName("Too short")
            void ifProvidedWithAVanityUrlThatIsTooShortItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = RandomStringUtils.random(2, true, true);
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);

                List<ValidationResult> validationResultList = vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1, validationResultList.size());
                assertEquals(expectedValidationError, validationResultList.get(0));
            }

            @Test
            @DisplayName("Too long")
            void ifProvidedWithAVanityUrlThatIsTooLongItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = RandomStringUtils.random(33, true, true);
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
                List<ValidationResult> validationResultList = vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1, validationResultList.size());
                assertEquals(expectedValidationError, validationResultList.get(0));
            }

            @Test
            @DisplayName("Special characters")
            void ifProvidedWithAVanityUrlThatContainsSpecialCharactersItWillReturnAValidationErrorWithAnAppropriateMessage() {
                String generatedString = "abc123%^&";
                ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);

                List<ValidationResult> validationResultList = vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(generatedString));
                assertEquals(1, validationResultList.size());
                assertEquals(expectedValidationError, validationResultList.get(0));
            }

            @Test
            @DisplayName("Multiple invalid Vanity URLs return multiple validation errors")
            void IfProvidedWithMultipleInvalidVanityUrlsItWillReturnMultipleValidationErrorsWithAppropriateMessages() {
                String generatedString1 = "abc123%^&";
                String generatedString2 = RandomStringUtils.random(33, true, true);

                ValidationResult expectedValidationError1 = new ValidationResult(true, generatedString1, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
                ValidationResult expectedValidationError2 = new ValidationResult(true, generatedString2, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);

                List<ValidationResult> validationResultList = vanityUrlService.validateSteamIdsAndVanityUrls(Set.of(generatedString1, generatedString2));

                assertEquals(2, validationResultList.size());
                assertTrue(validationResultList.contains(expectedValidationError1));
                assertTrue(validationResultList.contains(expectedValidationError2));
            }
        }
    }
}
