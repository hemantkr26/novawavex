
package com.novawavex.novawavex.controller;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.AuthResponse;
import com.novawavex.novawavex.dto.ChangePasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordResponse;
import com.novawavex.novawavex.dto.ResetPasswordRequest;
import com.novawavex.novawavex.dto.ResetPasswordResponse;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.service.AuthService;
import com.novawavex.novawavex.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    private AuthController authController;


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        authController =
                new AuthController(
                        authService,
                        jwtService
                );
    }


    // =========================================================
    // 1. LOGIN - SUCCESS
    // =========================================================

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse() {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "controller-test@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );


        User user =
                new User(
                        "Controller Test User",
                        "controller-test@novawavex.com",
                        "encodedPassword",
                        "USER"
                );


        when(
                authService.authenticate(
                        request
                )
        ).thenReturn(user);


        when(
                jwtService.generateToken(
                        any(),
                        eq("controller-test@novawavex.com"),
                        eq("Controller Test User"),
                        eq("USER")
                )
        ).thenReturn(
                "test-jwt-token"
        );


        AuthResponse response =
                authController.login(
                        request
                );


        assertNotNull(response);

        assertEquals(
                "test-jwt-token",
                response.getToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );


        verify(authService)
                .authenticate(
                        request
                );


        verify(jwtService)
                .generateToken(
                        any(),
                        eq("controller-test@novawavex.com"),
                        eq("Controller Test User"),
                        eq("USER")
                );
    }


    // =========================================================
    // 2. LOGIN - USER DATA PASSED TO JWT
    // =========================================================

    @Test
    void login_shouldGenerateTokenUsingUserInformation() {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwt-test@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );


        User user =
                new User(
                        "JWT Test User",
                        "jwt-test@novawavex.com",
                        "encodedPassword",
                        "ADMIN"
                );


        when(
                authService.authenticate(
                        request
                )
        ).thenReturn(user);


        when(
                jwtService.generateToken(
                        any(),
                        eq("jwt-test@novawavex.com"),
                        eq("JWT Test User"),
                        eq("ADMIN")
                )
        ).thenReturn(
                "admin-jwt-token"
        );


        AuthResponse response =
                authController.login(
                        request
                );


        assertEquals(
                "admin-jwt-token",
                response.getToken()
        );

        assertEquals(
                "Bearer",
                response.getTokenType()
        );


        verify(jwtService)
                .generateToken(
                        any(),
                        eq("jwt-test@novawavex.com"),
                        eq("JWT Test User"),
                        eq("ADMIN")
                );
    }


    // =========================================================
    // 3. CHANGE PASSWORD - SUCCESS
    // =========================================================

    @Test
    void changePassword_withAuthenticatedUser_shouldSucceed() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword(
                "OldPassword123"
        );

        request.setNewPassword(
                "NewPassword123"
        );

        request.setConfirmPassword(
                "NewPassword123"
        );


        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "controller-test@novawavex.com",
                        null
                );


        String response =
                authController.changePassword(
                        request,
                        authentication
                );


        assertEquals(
                "Password changed successfully.",
                response
        );


        verify(authService)
                .changePassword(
                        eq("controller-test@novawavex.com"),
                        eq(request)
                );
    }


    // =========================================================
    // 4. CHANGE PASSWORD - USES AUTHENTICATED EMAIL
    // =========================================================

    @Test
    void changePassword_shouldUseAuthenticatedUserEmail() {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword(
                "OldPassword123"
        );

        request.setNewPassword(
                "NewPassword123"
        );

        request.setConfirmPassword(
                "NewPassword123"
        );


        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "authenticated-user@novawavex.com",
                        null
                );


        authController.changePassword(
                request,
                authentication
        );


        verify(authService)
                .changePassword(
                        eq("authenticated-user@novawavex.com"),
                        eq(request)
                );
    }


    // =========================================================
    // 5. FORGOT PASSWORD - SUCCESS
    // =========================================================

    @Test
    void forgotPassword_shouldDelegateToAuthService() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail(
                "forgot-test@novawavex.com"
        );


        ForgotPasswordResponse expectedResponse =
                new ForgotPasswordResponse(
                        "If an account exists for this email, "
                        + "a password reset link has been generated."
                );


        when(
                authService.forgotPassword(
                        request
                )
        ).thenReturn(
                expectedResponse
        );


        ForgotPasswordResponse response =
                authController.forgotPassword(
                        request
                );


        assertNotNull(response);

        assertEquals(
                expectedResponse,
                response
        );


        verify(authService)
                .forgotPassword(
                        request
                );
    }


    // =========================================================
    // 6. FORGOT PASSWORD - REQUEST PASSED CORRECTLY
    // =========================================================

    @Test
    void forgotPassword_shouldPassRequestToService() {

        ForgotPasswordRequest request =
                new ForgotPasswordRequest();

        request.setEmail(
                "service-test@novawavex.com"
        );


        ForgotPasswordResponse expectedResponse =
                new ForgotPasswordResponse(
                        "Test response"
                );


        when(
                authService.forgotPassword(
                        request
                )
        ).thenReturn(
                expectedResponse
        );


        ForgotPasswordResponse response =
                authController.forgotPassword(
                        request
                );


        assertEquals(
                expectedResponse,
                response
        );


        verify(authService)
                .forgotPassword(
                        eq(request)
                );
    }


    // =========================================================
    // 7. RESET PASSWORD - SUCCESS
    // =========================================================

    @Test
    void resetPassword_shouldDelegateToAuthService() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setResetToken(
                "valid-reset-token"
        );

        request.setNewPassword(
                "NewPassword123"
        );

        request.setConfirmPassword(
                "NewPassword123"
        );


        ResetPasswordResponse expectedResponse =
                new ResetPasswordResponse(
                        "Password reset successfully. "
                        + "You can now login with your new password."
                );


        when(
                authService.resetPassword(
                        request
                )
        ).thenReturn(
                expectedResponse
        );


        ResetPasswordResponse response =
                authController.resetPassword(
                        request
                );


        assertNotNull(response);

        assertEquals(
                expectedResponse,
                response
        );


        verify(authService)
                .resetPassword(
                        request
                );
    }


    // =========================================================
    // 8. RESET PASSWORD - REQUEST PASSED CORRECTLY
    // =========================================================

    @Test
    void resetPassword_shouldPassRequestToService() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setResetToken(
                "controller-reset-token"
        );

        request.setNewPassword(
                "NewPassword123"
        );

        request.setConfirmPassword(
                "NewPassword123"
        );


        ResetPasswordResponse expectedResponse =
                new ResetPasswordResponse(
                        "Reset successful"
                );


        when(
                authService.resetPassword(
                        request
                )
        ).thenReturn(
                expectedResponse
        );


        ResetPasswordResponse response =
                authController.resetPassword(
                        request
                );


        assertEquals(
                expectedResponse,
                response
        );


        verify(authService)
                .resetPassword(
                        eq(request)
                );
    }


    // =========================================================
    // 9. LOGIN - SERVICE RESULT IS USED
    // =========================================================

    @Test
    void login_shouldUseAuthenticatedUserReturnedByService() {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "service-user@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );


        User user =
                new User(
                        "Service User",
                        "service-user@novawavex.com",
                        "encodedPassword",
                        "USER"
                );


        when(
                authService.authenticate(
                        request
                )
        ).thenReturn(
                user
        );


        when(
                jwtService.generateToken(
                        any(),
                        eq("service-user@novawavex.com"),
                        eq("Service User"),
                        eq("USER")
                )
        ).thenReturn(
                "service-user-token"
        );


        AuthResponse response =
                authController.login(
                        request
                );


        assertEquals(
                "service-user-token",
                response.getToken()
        );


        verify(authService)
                .authenticate(
                        request
                );
    }


    // =========================================================
    // 10. LOGIN - BEARER TOKEN TYPE
    // =========================================================

    @Test
    void login_shouldReturnBearerTokenType() {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "bearer-test@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );


        User user =
                new User(
                        "Bearer Test User",
                        "bearer-test@novawavex.com",
                        "encodedPassword",
                        "USER"
                );


        when(
                authService.authenticate(
                        request
                )
        ).thenReturn(
                user
        );


        when(
                jwtService.generateToken(
                        any(),
                        any(),
                        any(),
                        any()
                )
        ).thenReturn(
                "bearer-test-token"
        );


        AuthResponse response =
                authController.login(
                        request
                );


        assertEquals(
                "Bearer",
                response.getTokenType()
        );
    }

}
