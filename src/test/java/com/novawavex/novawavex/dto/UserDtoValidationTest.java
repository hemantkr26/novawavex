
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

class UserDtoValidationTest {

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
    // USER REQUEST
    // =========================================================

    @Test
    void userRequest_withValidData_shouldPassValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    // =========================================================
    // FULL NAME
    // =========================================================

    @Test
    void userRequest_withNullFullName_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName(null);
        request.setEmail("hemant@example.com");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }

    @Test
    void userRequest_withBlankFullName_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("");
        request.setEmail("hemant@example.com");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Full name is required")
                        )
        );
    }

    @Test
    void userRequest_withFullNameOver100Characters_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName(
                "A".repeat(101)
        );

        request.setEmail("hemant@example.com");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Full name must not exceed 100 characters"
                                        )
                        )
        );
    }

    @Test
    void userRequest_withExactly100CharacterFullName_shouldPassValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName(
                "A".repeat(100)
        );

        request.setEmail("hemant@example.com");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    // =========================================================
    // EMAIL
    // =========================================================

    @Test
    void userRequest_withNullEmail_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail(null);
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Email is required")
                        )
        );
    }

    @Test
    void userRequest_withBlankEmail_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Email is required")
                        )
        );
    }

    @Test
    void userRequest_withInvalidEmail_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("invalid-email");
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Please provide a valid email address"
                                        )
                        )
        );
    }

    @Test
    void userRequest_withEmailOver150Characters_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");

        String longEmail =
                "a".repeat(142) + "@test.com";

        request.setEmail(longEmail);
        request.setPassword("Password123");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Email must not exceed 150 characters"
                                        )
                        )
        );
    }

    // =========================================================
    // PASSWORD
    // =========================================================

    @Test
    void userRequest_withNullPassword_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword(null);

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Password is required")
                        )
        );
    }

    @Test
    void userRequest_withBlankPassword_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword("");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals("Password is required")
                        )
        );
    }

    @Test
    void userRequest_withPasswordLessThan8Characters_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword("1234567");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Password must be between 8 and 100 characters"
                                        )
                        )
        );
    }

    @Test
    void userRequest_withExactly8CharacterPassword_shouldPassValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword("12345678");

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void userRequest_withExactly100CharacterPassword_shouldPassValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword(
                "A".repeat(100)
        );

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void userRequest_withPasswordOver100Characters_shouldFailValidation() {

        UserRequest request =
                new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@example.com");
        request.setPassword(
                "A".repeat(101)
        );

        Set<ConstraintViolation<UserRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Password must be between 8 and 100 characters"
                                        )
                        )
        );
    }

    // =========================================================
    // USER RESPONSE
    // =========================================================

    @Test
    void userResponse_defaultConstructor_shouldHaveNullValues() {

        UserResponse response =
                new UserResponse();

        assertEquals(
                null,
                response.getId()
        );

        assertEquals(
                null,
                response.getFullName()
        );

        assertEquals(
                null,
                response.getEmail()
        );

        assertEquals(
                null,
                response.getRole()
        );

        assertEquals(
                null,
                response.getProfileImage()
        );
    }

    @Test
    void userResponse_fourArgumentConstructor_shouldMapValues() {

        UserResponse response =
                new UserResponse(
                        1L,
                        "Hemant Kumar",
                        "hemant@example.com",
                        "USER"
                );

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "Hemant Kumar",
                response.getFullName()
        );

        assertEquals(
                "hemant@example.com",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        assertEquals(
                null,
                response.getProfileImage()
        );
    }

    @Test
    void userResponse_fiveArgumentConstructor_shouldMapProfileImage() {

        UserResponse response =
                new UserResponse(
                        1L,
                        "Hemant Kumar",
                        "hemant@example.com",
                        "USER",
                        "profile-image-data"
                );

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "Hemant Kumar",
                response.getFullName()
        );

        assertEquals(
                "hemant@example.com",
                response.getEmail()
        );

        assertEquals(
                "USER",
                response.getRole()
        );

        assertEquals(
                "profile-image-data",
                response.getProfileImage()
        );
    }
}

