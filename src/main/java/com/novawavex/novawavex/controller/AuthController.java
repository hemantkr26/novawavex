
package com.novawavex.novawavex.controller;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.AuthResponse;
import com.novawavex.novawavex.dto.ChangePasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordResponse;
import com.novawavex.novawavex.dto.RegisterRequest;
import com.novawavex.novawavex.dto.RegisterResponse;
import com.novawavex.novawavex.dto.ResetPasswordRequest;
import com.novawavex.novawavex.dto.ResetPasswordResponse;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.service.AuthService;
import com.novawavex.novawavex.service.JwtService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService) {

        this.authService = authService;

        this.jwtService = jwtService;
    }


    /*
     * =========================================
     * LOGIN
     * =========================================
     */

    @PostMapping("/login")
    public AuthResponse login(
            @Valid
            @RequestBody
            AuthRequest request) {

        User user =
                authService.authenticate(request);

        String token =
                jwtService.generateToken(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole()
                );

        return new AuthResponse(
                token,
                "Bearer"
        );
    }


    /*
     * =========================================
     * REGISTER
     * =========================================
     *
     * Public endpoint.
     *
     * A new user does not have a JWT yet,
     * therefore registration must not require
     * authentication.
     */

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid
            @RequestBody
            RegisterRequest request) {

        return authService.register(
                request
        );
    }


    /*
     * =========================================
     * CHANGE PASSWORD
     * =========================================
     *
     * Requires an authenticated JWT.
     *
     * The user's email is taken from the
     * authenticated SecurityContext.
     *
     * It is NOT accepted from the frontend.
     */

    @PostMapping("/change-password")
    public String changePassword(
            @Valid
            @RequestBody
            ChangePasswordRequest request,
            Authentication authentication) {

        /*
         * =====================================
         * GET AUTHENTICATED USER
         * =====================================
         */

        String email =
                authentication.getName();


        /*
         * =====================================
         * CHANGE PASSWORD
         * =====================================
         */

        authService.changePassword(
                email,
                request
        );


        /*
         * =====================================
         * SUCCESS RESPONSE
         * =====================================
         */

        return "Password changed successfully.";
    }


    /*
     * =========================================
     * FORGOT PASSWORD
     * =========================================
     */

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse
    forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request) {

        return authService.forgotPassword(
                request
        );
    }


    /*
     * =========================================
     * RESET PASSWORD
     * =========================================
     */

    @PostMapping("/reset-password")
    public ResetPasswordResponse
    resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request) {

        return authService.resetPassword(
                request
        );
    }
}
