package com.novawavex.novawavex.service;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.ChangePasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordRequest;
import com.novawavex.novawavex.dto.ForgotPasswordResponse;
import com.novawavex.novawavex.dto.RegisterRequest;
import com.novawavex.novawavex.dto.RegisterResponse;
import com.novawavex.novawavex.dto.ResetPasswordRequest;
import com.novawavex.novawavex.dto.ResetPasswordResponse;
import com.novawavex.novawavex.entity.PasswordResetToken;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.DuplicateResourceException;
import com.novawavex.novawavex.exception.UnauthorizedException;
import com.novawavex.novawavex.repository.PasswordResetTokenRepository;
import com.novawavex.novawavex.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final EmailService emailService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenRepository
                    passwordResetTokenRepository,
            EmailService emailService) {

        this.userRepository = userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.emailService =
                emailService;
    }

    /*
     * =========================================
     * REGISTRATION
     * =========================================
     */

    @Transactional
    public RegisterResponse register(
            RegisterRequest request) {

        String fullName =
                request.getFullName()
                        .trim();

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        if (!request.getPassword()
                .equals(
                        request.getConfirmPassword()
                )) {

            throw new IllegalArgumentException(
                    "Passwords do not match"
            );
        }

        if (userRepository
                .findByEmail(email)
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );

        User user = new User(
                fullName,
                email,
                encodedPassword,
                "USER"
        );

        if (request.getProfileImage() != null
                && !request.getProfileImage()
                        .isBlank()) {

            user.setProfileImage(
                    request.getProfileImage()
            );
        }

        User savedUser =
                userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getProfileImage()
        );
    }

    /*
     * =========================================
     * LOGIN AUTHENTICATION
     * =========================================
     */

    public User authenticate(
            AuthRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {

            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        return user;
    }

    /*
     * =========================================
     * CHANGE PASSWORD
     * =========================================
     */

    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {

            throw new UnauthorizedException(
                    "Unable to identify the account"
            );
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new UnauthorizedException(
                    "Current password is incorrect"
            );
        }

        if (!request.getNewPassword()
                .equals(
                        request.getConfirmPassword()
                )) {

            throw new IllegalArgumentException(
                    "Passwords do not match"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from your current password"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.setPassword(
                encodedPassword
        );

        userRepository.save(user);
    }

    /*
     * =========================================
     * FORGOT PASSWORD
     * =========================================
     */

    @Transactional
    public ForgotPasswordResponse
    forgotPassword(
            ForgotPasswordRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        String genericMessage =
                "If an account exists for this email, "
                + "a password reset link has been generated.";

        if (user == null) {

            return new ForgotPasswordResponse(
                    genericMessage
            );
        }

        /*
         * =====================================
         * DELETE OLD RESET TOKEN
         * =====================================
         */

        passwordResetTokenRepository
                .deleteByUserAndUsedFalse(user);

        /*
         * =====================================
         * GENERATE SECURE RESET TOKEN
         * =====================================
         */

        byte[] randomBytes =
                new byte[48];

        secureRandom.nextBytes(
                randomBytes
        );

        String resetToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                randomBytes
                        );

        /*
         * =====================================
         * TOKEN EXPIRY
         * =====================================
         */

        LocalDateTime expiryDate =
                LocalDateTime.now()
                        .plusMinutes(15);

        /*
         * =====================================
         * SAVE RESET TOKEN
         * =====================================
         */

        PasswordResetToken
                passwordResetToken =
                new PasswordResetToken(
                        resetToken,
                        user,
                        expiryDate
                );

        passwordResetTokenRepository.save(
                passwordResetToken
        );

        /*
         * =====================================
         * CREATE FRONTEND RESET LINK
         * =====================================
         *
         * Uses the configured frontend URL.
         *
         * Production:
         * https://novawavex-frontend.onrender.com
         *
         * Development:
         * configured by app.cors.allowed-origin
         * =====================================
         */

        String resetLink =
                frontendUrl
                + "/reset-password?token="
                + resetToken;

        /*
         * =====================================
         * SEND RESET EMAIL
         * =====================================
         */

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetLink
        );

        return new ForgotPasswordResponse(
                genericMessage
        );
    }

    /*
     * =========================================
     * RESET PASSWORD
     * =========================================
     */

    @Transactional
    public ResetPasswordResponse
    resetPassword(
            ResetPasswordRequest request) {

        if (!request.getNewPassword()
                .equals(
                        request.getConfirmPassword()
                )) {

            throw new IllegalArgumentException(
                    "Passwords do not match"
            );
        }

        String resetToken =
                request.getResetToken()
                        .trim();

        PasswordResetToken
                passwordResetToken =

                passwordResetTokenRepository
                        .findByToken(resetToken)
                        .orElse(null);

        if (passwordResetToken == null) {

            throw new IllegalArgumentException(
                    "Invalid or expired password reset token"
            );
        }

        if (passwordResetToken.isUsed()) {

            throw new IllegalArgumentException(
                    "This password reset token has already been used"
            );
        }

        if (passwordResetToken
                .getExpiryDate()
                .isBefore(
                        LocalDateTime.now()
                )) {

            throw new IllegalArgumentException(
                    "This password reset token has expired"
            );
        }

        User user =
                passwordResetToken.getUser();

        if (user == null) {

            throw new IllegalArgumentException(
                    "Unable to identify the account"
            );
        }

        /*
         * =====================================
         * UPDATE PASSWORD
         * =====================================
         */

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.setPassword(
                encodedPassword
        );

        userRepository.save(user);

        /*
         * =====================================
         * MARK TOKEN AS USED
         * =====================================
         */

        passwordResetToken.setUsed(true);

        passwordResetTokenRepository.save(
                passwordResetToken
        );

        /*
         * =====================================
         * SUCCESS RESPONSE
         * =====================================
         */

        return new ResetPasswordResponse(
                "Password reset successfully. "
                + "You can now login with your new password."
        );
    }
}