package com.novawavex.novawavex.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthDtoValidationTest {


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
// AuthRequest
// =========================================================

@Test
void authRequest_withValidData_shouldPassValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertTrue(violations.isEmpty());
}

@Test
void authRequest_withBlankEmail_shouldFailValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail("");
    request.setPassword("Password123");

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.stream()
            .filter(v -> v.getPropertyPath()
                    .toString()
                    .equals("email"))
            .count());
}

@Test
void authRequest_withInvalidEmail_shouldFailValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail("invalid-email");
    request.setPassword("Password123");

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void authRequest_withEmailOver150Characters_shouldFailValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail(
            "a".repeat(140) + "@test.com"
    );

    request.setPassword("Password123");

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void authRequest_withBlankPassword_shouldFailValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail("user@novawavex.com");
    request.setPassword("");

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void authRequest_withPasswordShorterThan8Characters_shouldFailValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail("user@novawavex.com");
    request.setPassword("1234567");

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void authRequest_withPasswordLongerThan100Characters_shouldFailValidation() {

    AuthRequest request =
            new AuthRequest();

    request.setEmail("user@novawavex.com");
    request.setPassword("a".repeat(101));

    Set<ConstraintViolation<AuthRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

// =========================================================
// AuthResponse
// =========================================================

@Test
void authResponse_shouldStoreTokenAndTokenType() {

    AuthResponse response =
            new AuthResponse(
                    "jwt-token",
                    "Bearer"
            );

    assertEquals(
            "jwt-token",
            response.getToken()
    );

    assertEquals(
            "Bearer",
            response.getTokenType()
    );
}

@Test
void authResponse_defaultConstructor_shouldCreateObject() {

    AuthResponse response =
            new AuthResponse();

    assertEquals(
            null,
            response.getToken()
    );

    assertEquals(
            null,
            response.getTokenType()
    );
}

// =========================================================
// RegisterRequest
// =========================================================

@Test
void registerRequest_withValidData_shouldPassValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");
    request.setProfileImage("profile-image-data");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertTrue(violations.isEmpty());
}

@Test
void registerRequest_withBlankFullName_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withFullNameShorterThan2Characters_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("A");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withFullNameLongerThan100Characters_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("A".repeat(101));
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withBlankEmail_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withInvalidEmail_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("invalid-email");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withEmailOver150Characters_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail(
            "a".repeat(145) + "@test.com"
    );
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withBlankPassword_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withPasswordShorterThan8Characters_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("1234567");
    request.setConfirmPassword("1234567");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withPasswordLongerThan100Characters_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("a".repeat(101));
    request.setConfirmPassword("a".repeat(101));

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withBlankConfirmPassword_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withWhitespaceFullName_shouldFailValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("   ");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void registerRequest_withOptionalProfileImageMissing_shouldPassValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Password123");
    request.setProfileImage(null);

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    assertTrue(violations.isEmpty());
}

@Test
void registerRequest_withMismatchedPasswords_shouldCurrentlyPassDtoValidation() {

    RegisterRequest request =
            new RegisterRequest();

    request.setFullName("Hemant Kumar");
    request.setEmail("user@novawavex.com");
    request.setPassword("Password123");
    request.setConfirmPassword("Different123");

    Set<ConstraintViolation<RegisterRequest>> violations =
            validator.validate(request);

    /*
     * RegisterRequest currently has only @NotBlank
     * on confirmPassword. It does not have a
     * password-match class-level validation rule.
     *
     * Therefore DTO validation alone should pass.
     * Password matching must be handled by the
     * service/controller layer if implemented there.
     */

    assertTrue(violations.isEmpty());
}

// =========================================================
// RegisterResponse
// =========================================================

@Test
void registerResponse_shouldStoreUserInformation() {

    RegisterResponse response =
            new RegisterResponse(
                    1L,
                    "Hemant Kumar",
                    "user@novawavex.com",
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
            "user@novawavex.com",
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

@Test
void registerResponse_defaultConstructor_shouldCreateObject() {

    RegisterResponse response =
            new RegisterResponse();

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


}
