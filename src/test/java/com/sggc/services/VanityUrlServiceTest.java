package com.sggc.services;

import com.sggc.validation.ValidationResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE;
import static com.sggc.services.VanityUrlService.VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VanityUrlServiceTest {

    VanityUrlService vanityUrlService;

    @BeforeEach
    public void setup(){
        vanityUrlService = new VanityUrlService();
    }


    @Nested
    @DisplayName("If provided with a invalid vanity URL it will return a validation error with an appropriate message")
    class InvalidUserIdValidationTests {
        @Test
        @DisplayName("If provided with a vanity URL that is too short it will return a validation error with an appropriate message")
        void ifProvidedWithAVanityUrlThatIsTooShortItWillReturnAValidationErrorWithAnAppropriateMessage() {
            String generatedString = RandomStringUtils.random(2, true, true);
            ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
            ValidationResult validationResultList = vanityUrlService.validate(generatedString);
            assertEquals(expectedValidationError, validationResultList);
        }

        @Test
        @DisplayName("If provided with a vanity URL that is too long it will return a validation error with an appropriate message")
        void ifProvidedWithAVanityUrlThatIsTooLongItWillReturnAValidationErrorWithAnAppropriateMessage() {
            String generatedString = RandomStringUtils.random(33, true, true);
            ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_WITHIN_REQUIRED_LENGTH_ERROR_MESSAGE);
            ValidationResult validationResultList = vanityUrlService.validate(generatedString);
            assertEquals(expectedValidationError, validationResultList);
        }

        @Test
        @DisplayName("If provided with a vanity URL that contains special characters it will return a validation error with an appropriate message")
        void ifProvidedWithAVanityUrlThatContainsSpecialCharactersItWillReturnAValidationErrorWithAnAppropriateMessage() {
            String generatedString = "abc123%^&";
            ValidationResult expectedValidationError = new ValidationResult(true, generatedString, VANITY_URL_NOT_ALPHANUMERIC_ERROR_MESSAGE);
            ValidationResult validationResultList = vanityUrlService.validate(generatedString);
            assertEquals(expectedValidationError, validationResultList);
        }
    }

    @Test
    @DisplayName("If provided with a valid vanity URL it will not return any validation errors")
    void ifProvidedWithAVanityUrlThatContainsSpecialCharactersItWillReturnAValidationErrorWithAnAppropriateMessage() {
        String generatedString = RandomStringUtils.random(8, true, true);
        ValidationResult expectedValidationError = new ValidationResult(false, null, null);
        ValidationResult validationResultList = vanityUrlService.validate(generatedString);
        assertEquals(expectedValidationError, validationResultList);
    }
}
