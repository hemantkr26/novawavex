
package com.novawavex.novawavex.service;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.ChangePasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordResponse;
import com.novawavex.novawavex.dto.ResetPasswordRequest;
import com.novawavex.novawavex.dto.ResetPasswordResponse;
import com.novawavex.novawavex.entity.PasswordResetToken;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.UnauthorizedException;
import com.novawavex.novawavex.repository.PasswordResetTokenRepository;
import com.novawavex.novawavex.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User user;


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        user = new User(
                "Auth Test User",
                "auth-test@novawavex.com",
                "encodedOldPassword",
                "USER"
        );
    }


    // =========================================================
    // 1. LOGIN - SUCCESS
    // =========================================================

    @Test
    void authenticate_withValidCredentials_shouldReturnUser() {

        AuthRequest request = new AuthRequest();

        request.setEmail(
                "auth-test@novawavex.com"
        );

        request.setPassword(
                "CorrectPassword123"
        );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "CorrectPassword123",
                        "encodedOldPassword"
                )
        ).thenReturn(true);


        User result =
                authService.authenticate(request);


        assertNotNull(result);

        assertEquals(
                "auth-test@novawavex.com",
                result.getEmail()
        );

        assertEquals(
                "Auth Test User",
                result.getFullName()
        );

        verify(
                userRepository,
                times(1)
        ).findByEmail(
                "auth-test@novawavex.com"
        );

        verify(
                passwordEncoder,
                times(1)
        ).matches(
                "CorrectPassword123",
                "encodedOldPassword"
        );
    }


    // =========================================================
    // 2. LOGIN - USER NOT FOUND
    // =========================================================

    @Test
    void authenticate_whenUserDoesNotExist_shouldThrowUnauthorized() {

        AuthRequest request = new AuthRequest();

        request.setEmail(
                "missing@novawavex.com"
        );

        request.setPassword(
                "CorrectPassword123"
        );

        when(
                userRepository.findByEmail(
                        "missing@novawavex.com"
                )
        ).thenReturn(
                Optional.empty()
        );


        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> authService.authenticate(request)
                );


        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(
                userRepository,
                times(1)
        ).findByEmail(
                "missing@novawavex.com"
        );

        verifyNoInteractions(
                passwordEncoder
        );
    }


    // =========================================================
    // 3. LOGIN - WRONG PASSWORD
    // =========================================================

    @Test
    void authenticate_withWrongPassword_shouldThrowUnauthorized() {

        AuthRequest request = new AuthRequest();

        request.setEmail(
                "auth-test@novawavex.com"
        );

        request.setPassword(
                "WrongPassword123"
        );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "WrongPassword123",
                        "encodedOldPassword"
                )
        ).thenReturn(false);


        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> authService.authenticate(request)
                );


        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(
                userRepository,
                times(1)
        ).findByEmail(
                "auth-test@novawavex.com"
        );

        verify(
                passwordEncoder,
                times(1)
        ).matches(
                "WrongPassword123",
                "encodedOldPassword"
        );
    }


    // =========================================================
    // 4. CHANGE PASSWORD - SUCCESS
    // =========================================================

    @Test
    void changePassword_withValidCurrentPassword_shouldUpdatePassword() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword123",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "OldPassword123",
                        "encodedOldPassword"
                )
        ).thenReturn(true);

        when(
                passwordEncoder.matches(
                        "NewPassword123",
                        "encodedOldPassword"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(
                        "NewPassword123"
                )
        ).thenReturn(
                "encodedNewPassword"
        );


        authService.changePassword(
                "auth-test@novawavex.com",
                request
        );


        assertEquals(
                "encodedNewPassword",
                user.getPassword()
        );

        verify(
                passwordEncoder,
                times(1)
        ).encode(
                "NewPassword123"
        );

        verify(
                userRepository,
                times(1)
        ).save(user);
    }


    // =========================================================
    // 5. CHANGE PASSWORD - USER NOT FOUND
    // =========================================================

    @Test
    void changePassword_whenUserDoesNotExist_shouldThrowUnauthorized() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword123",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                userRepository.findByEmail(
                        "missing@novawavex.com"
                )
        ).thenReturn(
                Optional.empty()
        );


        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> authService.changePassword(
                                "missing@novawavex.com",
                                request
                        )
                );


        assertEquals(
                "Unable to identify the account",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 6. CHANGE PASSWORD - WRONG CURRENT PASSWORD
    // =========================================================

    @Test
    void changePassword_withWrongCurrentPassword_shouldThrowUnauthorized() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "WrongOldPassword",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "WrongOldPassword",
                        "encodedOldPassword"
                )
        ).thenReturn(false);


        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> authService.changePassword(
                                "auth-test@novawavex.com",
                                request
                        )
                );


        assertEquals(
                "Current password is incorrect",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 7. CHANGE PASSWORD - PASSWORDS DO NOT MATCH
    // =========================================================

    @Test
    void changePassword_whenPasswordsDoNotMatch_shouldThrowException() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword123",
                        "NewPassword123",
                        "DifferentPassword123"
                );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "OldPassword123",
                        "encodedOldPassword"
                )
        ).thenReturn(true);


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.changePassword(
                                "auth-test@novawavex.com",
                                request
                        )
                );


        assertEquals(
                "Passwords do not match",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 8. CHANGE PASSWORD - SAME PASSWORD
    // =========================================================

    @Test
    void changePassword_whenNewPasswordIsSame_shouldThrowException() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "OldPassword123",
                        "OldPassword123",
                        "OldPassword123"
                );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "OldPassword123",
                        "encodedOldPassword"
                )
        ).thenReturn(true);


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.changePassword(
                                "auth-test@novawavex.com",
                                request
                        )
                );


        assertEquals(
                "New password must be different from your current password",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 9. FORGOT PASSWORD - UNKNOWN EMAIL
    // =========================================================

    @Test
    void forgotPassword_whenUserDoesNotExist_shouldReturnGenericMessage() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest(
                        "missing@novawavex.com"
                );

        when(
                userRepository.findByEmail(
                        "missing@novawavex.com"
                )
        ).thenReturn(
                Optional.empty()
        );


        ForgotPasswordResponse response =
                authService.forgotPassword(request);


        assertNotNull(response);

        assertEquals(
                "If an account exists for this email, "
                        + "a password reset link has been generated.",
                response.getMessage()
        );

        verify(
                passwordResetTokenRepository,
                never()
        ).save(any(PasswordResetToken.class));

        verify(
                passwordResetTokenRepository,
                never()
        ).deleteByUserAndUsedFalse(any(User.class));
    }


    // =========================================================
    // 10. FORGOT PASSWORD - SUCCESS
    // =========================================================

    @Test
    void forgotPassword_withExistingUser_shouldCreateResetToken() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest(
                        "AUTH-TEST@NOVAWAVEX.COM"
                );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );


        ForgotPasswordResponse response =
                authService.forgotPassword(request);


        assertNotNull(response);

        assertEquals(
                "If an account exists for this email, "
                        + "a password reset link has been generated.",
                response.getMessage()
        );

        verify(
                passwordResetTokenRepository,
                times(1)
        ).deleteByUserAndUsedFalse(user);

        verify(
                passwordResetTokenRepository,
                times(1)
        ).save(
                any(PasswordResetToken.class)
        );
    }


    // =========================================================
    // 11. FORGOT PASSWORD - TOKEN PROPERTIES
    // =========================================================

    @Test
    void forgotPassword_shouldCreateValidUnusedToken() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest(
                        "auth-test@novawavex.com"
                );

        when(
                userRepository.findByEmail(
                        "auth-test@novawavex.com"
                )
        ).thenReturn(
                Optional.of(user)
        );


        authService.forgotPassword(request);


        verify(
                passwordResetTokenRepository,
                times(1)
        ).save(
                argThat(token ->
                        token.getToken() != null
                                && !token.getToken().isBlank()
                                && token.getToken().length() >= 60
                                && token.getUser() == user
                                && !token.isUsed()
                                && token.getExpiryDate() != null
                                && token.getExpiryDate().isAfter(
                                        LocalDateTime.now()
                                )
                )
        );
    }


    // =========================================================
    // 12. RESET PASSWORD - SUCCESS
    // =========================================================

    @Test
    void resetPassword_withValidToken_shouldUpdatePassword() {

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        "valid-reset-token",
                        user,
                        LocalDateTime.now().plusMinutes(10)
                );

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "valid-reset-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                passwordResetTokenRepository.findByToken(
                        "valid-reset-token"
                )
        ).thenReturn(
                Optional.of(resetToken)
        );

        when(
                passwordEncoder.encode(
                        "NewPassword123"
                )
        ).thenReturn(
                "encodedNewPassword"
        );


        ResetPasswordResponse response =
                authService.resetPassword(request);


        assertNotNull(response);

        assertEquals(
                "Password reset successfully. "
                        + "You can now login with your new password.",
                response.getMessage()
        );

        assertEquals(
                "encodedNewPassword",
                user.getPassword()
        );

        assertTrue(
                resetToken.isUsed()
        );

        verify(
                userRepository,
                times(1)
        ).save(user);

        verify(
                passwordResetTokenRepository,
                times(1)
        ).save(resetToken);
    }


    // =========================================================
    // 13. RESET PASSWORD - PASSWORDS DO NOT MATCH
    // =========================================================

    @Test
    void resetPassword_whenPasswordsDoNotMatch_shouldThrowException() {

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "valid-reset-token",
                        "NewPassword123",
                        "DifferentPassword123"
                );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.resetPassword(request)
                );


        assertEquals(
                "Passwords do not match",
                exception.getMessage()
        );

        verifyNoInteractions(
                passwordResetTokenRepository
        );

        verifyNoInteractions(
                userRepository
        );
    }


    // =========================================================
    // 14. RESET PASSWORD - TOKEN NOT FOUND
    // =========================================================

    @Test
    void resetPassword_whenTokenDoesNotExist_shouldThrowException() {

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "missing-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                passwordResetTokenRepository.findByToken(
                        "missing-token"
                )
        ).thenReturn(
                Optional.empty()
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.resetPassword(request)
                );


        assertEquals(
                "Invalid or expired password reset token",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 15. RESET PASSWORD - USED TOKEN
    // =========================================================

    @Test
    void resetPassword_withUsedToken_shouldThrowException() {

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        "used-reset-token",
                        user,
                        LocalDateTime.now().plusMinutes(10)
                );

        resetToken.setUsed(true);

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "used-reset-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                passwordResetTokenRepository.findByToken(
                        "used-reset-token"
                )
        ).thenReturn(
                Optional.of(resetToken)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.resetPassword(request)
                );


        assertEquals(
                "This password reset token has already been used",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 16. RESET PASSWORD - EXPIRED TOKEN
    // =========================================================

    @Test
    void resetPassword_withExpiredToken_shouldThrowException() {

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        "expired-reset-token",
                        user,
                        LocalDateTime.now().minusMinutes(1)
                );

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "expired-reset-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                passwordResetTokenRepository.findByToken(
                        "expired-reset-token"
                )
        ).thenReturn(
                Optional.of(resetToken)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.resetPassword(request)
                );


        assertEquals(
                "This password reset token has expired",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 17. RESET PASSWORD - USER MISSING
    // =========================================================

    @Test
    void resetPassword_whenTokenHasNoUser_shouldThrowException() {

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setToken(
                "orphan-reset-token"
        );

        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(10)
        );

        resetToken.setUsed(false);

        resetToken.setUser(null);


        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "orphan-reset-token",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                passwordResetTokenRepository.findByToken(
                        "orphan-reset-token"
                )
        ).thenReturn(
                Optional.of(resetToken)
        );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.resetPassword(request)
                );


        assertEquals(
                "Unable to identify the account",
                exception.getMessage()
        );

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    // =========================================================
    // 18. RESET PASSWORD - TOKEN IS TRIMMED
    // =========================================================

    @Test
    void resetPassword_shouldTrimResetToken() {

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        "trimmed-reset-token",
                        user,
                        LocalDateTime.now().plusMinutes(10)
                );

        ResetPasswordRequest request =
                new ResetPasswordRequest(
                        "  trimmed-reset-token  ",
                        "NewPassword123",
                        "NewPassword123"
                );

        when(
                passwordResetTokenRepository.findByToken(
                        "trimmed-reset-token"
                )
        ).thenReturn(
                Optional.of(resetToken)
        );

        when(
                passwordEncoder.encode(
                        "NewPassword123"
                )
        ).thenReturn(
                "encodedNewPassword"
        );


        ResetPasswordResponse response =
                authService.resetPassword(request);


        assertNotNull(response);

        assertTrue(
                resetToken.isUsed()
        );

        verify(
                passwordResetTokenRepository,
                times(1)
        ).findByToken(
                "trimmed-reset-token"
        );
    }
}

