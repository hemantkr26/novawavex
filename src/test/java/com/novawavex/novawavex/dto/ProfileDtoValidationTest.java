
package com.novawavex.novawavex.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProfileDtoValidationTest {

    private static ValidatorFactory validatorFactory;

    private static Validator validator;

    @BeforeAll
    static void setUp() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {

        validatorFactory.close();
    }


    // =========================================================
    // PROFILE NAME REQUEST
    // =========================================================

    @Test
    void profileNameRequest_withValidName_shouldPassValidation() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "Hemant Kumar"
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileNameRequest_withNullName_shouldFailValidation() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        null
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }


    @Test
    void profileNameRequest_withBlankName_shouldFailValidation() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        ""
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.size() >= 1
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }


    @Test
    void profileNameRequest_withWhitespaceName_shouldFailValidation() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "   "
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }


    @Test
    void profileNameRequest_withOneCharacterName_shouldFailValidation() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "H"
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Full name must be between 2 and 100 characters"
                                        )
                        )
        );
    }


    @Test
    void profileNameRequest_withExactlyTwoCharacters_shouldPassValidation() {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "HK"
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileNameRequest_withExactly100Characters_shouldPassValidation() {

        String name =
                "A".repeat(100);

        ProfileNameRequest request =
                new ProfileNameRequest(
                        name
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileNameRequest_withMoreThan100Characters_shouldFailValidation() {

        String name =
                "A".repeat(101);

        ProfileNameRequest request =
                new ProfileNameRequest(
                        name
                );

        Set<ConstraintViolation<ProfileNameRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Full name must be between 2 and 100 characters"
                                        )
                        )
        );
    }


    // =========================================================
    // PROFILE IMAGE REQUEST
    // =========================================================

    @Test
    void profileImageRequest_withValidImage_shouldPassValidation() {

        ProfileImageRequest request =
                new ProfileImageRequest(
                        "data:image/png;base64,iVBORw0KGgoAAAANS"
                );

        Set<ConstraintViolation<ProfileImageRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileImageRequest_withNullImage_shouldFailValidation() {

        ProfileImageRequest request =
                new ProfileImageRequest(
                        null
                );

        Set<ConstraintViolation<ProfileImageRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Profile image is required")
                        )
        );
    }


    @Test
    void profileImageRequest_withBlankImage_shouldFailValidation() {

        ProfileImageRequest request =
                new ProfileImageRequest(
                        ""
                );

        Set<ConstraintViolation<ProfileImageRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Profile image is required")
                        )
        );
    }


    @Test
    void profileImageRequest_withWhitespaceImage_shouldFailValidation() {

        ProfileImageRequest request =
                new ProfileImageRequest(
                        "   "
                );

        Set<ConstraintViolation<ProfileImageRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Profile image is required")
                        )
        );
    }


    @Test
    void profileImageRequest_withMinimalNonBlankValue_shouldPassValidation() {

        ProfileImageRequest request =
                new ProfileImageRequest(
                        "x"
                );

        Set<ConstraintViolation<ProfileImageRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    // =========================================================
    // PROFILE UPDATE REQUEST
    // =========================================================

    @Test
    void profileUpdateRequest_withValidName_shouldPassValidation() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        "Hemant Kumar"
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileUpdateRequest_withNullName_shouldFailValidation() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        null
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }


    @Test
    void profileUpdateRequest_withBlankName_shouldFailValidation() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        ""
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.size() >= 1
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }


    @Test
    void profileUpdateRequest_withWhitespaceName_shouldFailValidation() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        "   "
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }


    @Test
    void profileUpdateRequest_withOneCharacterName_shouldFailValidation() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        "H"
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Full name must be between 2 and 100 characters"
                                        )
                        )
        );
    }


    @Test
    void profileUpdateRequest_withExactlyTwoCharacters_shouldPassValidation() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        "HK"
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileUpdateRequest_withExactly100Characters_shouldPassValidation() {

        String name =
                "A".repeat(100);

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        name
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }


    @Test
    void profileUpdateRequest_withMoreThan100Characters_shouldFailValidation() {

        String name =
                "A".repeat(101);

        ProfileUpdateRequest request =
                new ProfileUpdateRequest(
                        name
                );

        Set<ConstraintViolation<ProfileUpdateRequest>> violations =
                validator.validate(request);

        assertEquals(
                1,
                violations.size()
        );

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Full name must be between 2 and 100 characters"
                                        )
                        )
        );
    }


    // =========================================================
    // PROFILE UPDATE REQUEST - GETTER / SETTER
    // =========================================================

    @Test
    void profileUpdateRequest_getterAndSetter_shouldWork() {

        ProfileUpdateRequest request =
                new ProfileUpdateRequest();

        request.setFullName(
                "Updated User"
        );

        assertEquals(
                "Updated User",
                request.getFullName()
        );
    }
}
