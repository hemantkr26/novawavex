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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordDtoValidationTest {


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
// ChangePasswordRequest
// =========================================================

@Test
void changePasswordRequest_withValidData_shouldPassValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "OldPassword123",
                    "NewPassword123",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    assertTrue(violations.isEmpty());
}

@Test
void changePasswordRequest_withBlankCurrentPassword_shouldFailValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "",
                    "NewPassword123",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("currentPassword")
                    )
    );
}

@Test
void changePasswordRequest_withNullCurrentPassword_shouldFailValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    null,
                    "NewPassword123",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void changePasswordRequest_withBlankNewPassword_shouldFailValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "OldPassword123",
                    "",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("newPassword")
                    )
    );
}

@Test
void changePasswordRequest_withNewPasswordShorterThan8Characters_shouldFailValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "OldPassword123",
                    "1234567",
                    "1234567"
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void changePasswordRequest_withBlankConfirmPassword_shouldFailValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "OldPassword123",
                    "NewPassword123",
                    ""
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("confirmPassword")
                    )
    );
}

@Test
void changePasswordRequest_withMismatchedPasswords_shouldCurrentlyPassDtoValidation() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "OldPassword123",
                    "NewPassword123",
                    "DifferentPassword123"
            );

    Set<ConstraintViolation<ChangePasswordRequest>> violations =
            validator.validate(request);

    /*
     * The DTO currently has @NotBlank on
     * confirmPassword, but no class-level
     * password-match constraint.
     *
     * Therefore Bean Validation itself does
     * not reject the mismatch.
     */

    assertTrue(violations.isEmpty());
}

@Test
void changePasswordRequest_parameterizedConstructor_shouldSetValues() {

    ChangePasswordRequest request =
            new ChangePasswordRequest(
                    "Current123",
                    "NewPassword123",
                    "NewPassword123"
            );

    assertEquals(
            "Current123",
            request.getCurrentPassword()
    );

    assertEquals(
            "NewPassword123",
            request.getNewPassword()
    );

    assertEquals(
            "NewPassword123",
            request.getConfirmPassword()
    );
}

// =========================================================
// ForgotPasswordRequest
// =========================================================

@Test
void forgotPasswordRequest_withValidEmail_shouldPassValidation() {

    ForgotPasswordRequest request =
            new ForgotPasswordRequest(
                    "user@novawavex.com"
            );

    Set<ConstraintViolation<ForgotPasswordRequest>> violations =
            validator.validate(request);

    assertTrue(violations.isEmpty());
}

@Test
void forgotPasswordRequest_withBlankEmail_shouldFailValidation() {

    ForgotPasswordRequest request =
            new ForgotPasswordRequest(
                    ""
            );

    Set<ConstraintViolation<ForgotPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("email")
                    )
    );
}

@Test
void forgotPasswordRequest_withNullEmail_shouldFailValidation() {

    ForgotPasswordRequest request =
            new ForgotPasswordRequest(
                    null
            );

    Set<ConstraintViolation<ForgotPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void forgotPasswordRequest_withInvalidEmail_shouldFailValidation() {

    ForgotPasswordRequest request =
            new ForgotPasswordRequest(
                    "invalid-email"
            );

    Set<ConstraintViolation<ForgotPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void forgotPasswordRequest_withWhitespaceEmail_shouldFailValidation() {

    ForgotPasswordRequest request =
            new ForgotPasswordRequest(
                    "   "
            );

    Set<ConstraintViolation<ForgotPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void forgotPasswordRequest_parameterizedConstructor_shouldSetEmail() {

    ForgotPasswordRequest request =
            new ForgotPasswordRequest(
                    "user@novawavex.com"
            );

    assertEquals(
            "user@novawavex.com",
            request.getEmail()
    );
}

// =========================================================
// ResetPasswordRequest
// =========================================================

@Test
void resetPasswordRequest_withValidData_shouldPassValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "valid-reset-token",
                    "NewPassword123",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    assertTrue(violations.isEmpty());
}

@Test
void resetPasswordRequest_withBlankResetToken_shouldFailValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "",
                    "NewPassword123",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("resetToken")
                    )
    );
}

@Test
void resetPasswordRequest_withNullResetToken_shouldFailValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    null,
                    "NewPassword123",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void resetPasswordRequest_withBlankNewPassword_shouldFailValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "valid-reset-token",
                    "",
                    "NewPassword123"
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("newPassword")
                    )
    );
}

@Test
void resetPasswordRequest_withNewPasswordShorterThan8Characters_shouldFailValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "valid-reset-token",
                    "1234567",
                    "1234567"
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());
}

@Test
void resetPasswordRequest_withBlankConfirmPassword_shouldFailValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "valid-reset-token",
                    "NewPassword123",
                    ""
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getPropertyPath()
                                    .toString()
                                    .equals("confirmPassword")
                    )
    );
}

@Test
void resetPasswordRequest_withMismatchedPasswords_shouldCurrentlyPassDtoValidation() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "valid-reset-token",
                    "NewPassword123",
                    "DifferentPassword123"
            );

    Set<ConstraintViolation<ResetPasswordRequest>> violations =
            validator.validate(request);

    /*
     * There is currently no class-level
     * password-match validation annotation.
     */

    assertTrue(violations.isEmpty());
}

@Test
void resetPasswordRequest_parameterizedConstructor_shouldSetValues() {

    ResetPasswordRequest request =
            new ResetPasswordRequest(
                    "reset-token",
                    "NewPassword123",
                    "NewPassword123"
            );

    assertEquals(
            "reset-token",
            request.getResetToken()
    );

    assertEquals(
            "NewPassword123",
            request.getNewPassword()
    );

    assertEquals(
            "NewPassword123",
            request.getConfirmPassword()
    );
}

// =========================================================
// ResetPasswordResponse
// =========================================================

@Test
void resetPasswordResponse_shouldStoreMessage() {

    ResetPasswordResponse response =
            new ResetPasswordResponse(
                    "Password reset successful"
            );

    assertEquals(
            "Password reset successful",
            response.getMessage()
    );
}

@Test
void resetPasswordResponse_defaultConstructor_shouldCreateObject() {

    ResetPasswordResponse response =
            new ResetPasswordResponse();

    assertNull(
            response.getMessage()
    );
}

@Test
void resetPasswordResponse_setMessage_shouldUpdateMessage() {

    ResetPasswordResponse response =
            new ResetPasswordResponse();

    response.setMessage(
            "Password reset successful"
    );

    assertEquals(
            "Password reset successful",
            response.getMessage()
    );
}

// =========================================================
// ForgotPasswordResponse
// =========================================================

@Test
void forgotPasswordResponse_shouldStoreMessage() {

    ForgotPasswordResponse response =
            new ForgotPasswordResponse(
                    "Reset email sent"
            );

    assertEquals(
            "Reset email sent",
            response.getMessage()
    );
}

@Test
void forgotPasswordResponse_defaultConstructor_shouldCreateObject() {

    ForgotPasswordResponse response =
            new ForgotPasswordResponse();

    assertNull(
            response.getMessage()
    );
}

@Test
void forgotPasswordResponse_setMessage_shouldUpdateMessage() {

    ForgotPasswordResponse response =
            new ForgotPasswordResponse();

    response.setMessage(
            "Reset email sent"
    );

    assertEquals(
            "Reset email sent",
            response.getMessage()
    );
}


}
