
package com.novawavex.novawavex.repository;

import com.novawavex.novawavex.entity.PasswordResetToken;
import com.novawavex.novawavex.entity.User;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;


    // =========================================================
    // 1. SAVE PASSWORD RESET TOKEN
    // =========================================================

    @Test
    void saveToken_shouldPersistToken() {

        User user = new User(
                "Reset Token Save User",
                "reset-token-save@novawavex.com",
                "encodedPassword",
                "USER"
        );

        user = userRepository.save(user);

        PasswordResetToken token =
                new PasswordResetToken(
                        "reset-token-save-123",
                        user,
                        LocalDateTime.now().plusHours(1)
                );

        PasswordResetToken savedToken =
                passwordResetTokenRepository.save(token);

        assertNotNull(savedToken.getId());

        assertEquals(
                "reset-token-save-123",
                savedToken.getToken()
        );

        assertEquals(
                user.getId(),
                savedToken.getUser().getId()
        );

        assertFalse(savedToken.isUsed());

        assertNotNull(savedToken.getExpiryDate());
    }


    // =========================================================
    // 2. FIND TOKEN BY TOKEN VALUE
    // =========================================================

    @Test
    void findByToken_whenTokenExists_shouldReturnToken() {

        User user = new User(
                "Reset Token Find User",
                "reset-token-find@novawavex.com",
                "encodedPassword",
                "USER"
        );

        user = userRepository.save(user);

        PasswordResetToken token =
                new PasswordResetToken(
                        "reset-token-find-123",
                        user,
                        LocalDateTime.now().plusHours(1)
                );

        passwordResetTokenRepository.save(token);

        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken(
                        "reset-token-find-123"
                );

        assertTrue(result.isPresent());

        assertEquals(
                "reset-token-find-123",
                result.get().getToken()
        );

        assertEquals(
                user.getId(),
                result.get().getUser().getId()
        );

        assertFalse(result.get().isUsed());
    }


    // =========================================================
    // 3. FIND TOKEN - NOT FOUND
    // =========================================================

    @Test
    void findByToken_whenTokenDoesNotExist_shouldReturnEmpty() {

        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken(
                        "missing-reset-token-123"
                );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // 4. DELETE UNUSED TOKENS FOR USER
    // =========================================================

    @Test
    void deleteByUserAndUsedFalse_shouldDeleteUnusedTokens() {

        User user = new User(
                "Reset Token Delete User",
                "reset-token-delete@novawavex.com",
                "encodedPassword",
                "USER"
        );

        user = userRepository.save(user);

        PasswordResetToken unusedToken =
                new PasswordResetToken(
                        "unused-reset-token-123",
                        user,
                        LocalDateTime.now().plusHours(1)
                );

        PasswordResetToken usedToken =
                new PasswordResetToken(
                        "used-reset-token-123",
                        user,
                        LocalDateTime.now().plusHours(1)
                );

        usedToken.setUsed(true);

        passwordResetTokenRepository.save(unusedToken);
        passwordResetTokenRepository.save(usedToken);

        passwordResetTokenRepository.deleteByUserAndUsedFalse(user);

        Optional<PasswordResetToken> unusedResult =
                passwordResetTokenRepository.findByToken(
                        "unused-reset-token-123"
                );

        Optional<PasswordResetToken> usedResult =
                passwordResetTokenRepository.findByToken(
                        "used-reset-token-123"
                );

        assertTrue(unusedResult.isEmpty());

        assertTrue(usedResult.isPresent());

        assertTrue(usedResult.get().isUsed());
    }


    // =========================================================
    // 5. USED TOKEN SHOULD NOT BE DELETED
    // =========================================================

    @Test
    void deleteByUserAndUsedFalse_shouldKeepUsedToken() {

        User user = new User(
                "Reset Token Used User",
                "reset-token-used@novawavex.com",
                "encodedPassword",
                "USER"
        );

        user = userRepository.save(user);

        PasswordResetToken token =
                new PasswordResetToken(
                        "already-used-token-123",
                        user,
                        LocalDateTime.now().plusHours(1)
                );

        token.setUsed(true);

        passwordResetTokenRepository.save(token);

        passwordResetTokenRepository.deleteByUserAndUsedFalse(user);

        Optional<PasswordResetToken> result =
                passwordResetTokenRepository.findByToken(
                        "already-used-token-123"
                );

        assertTrue(result.isPresent());

        assertTrue(result.get().isUsed());

        assertEquals(
                user.getId(),
                result.get().getUser().getId()
        );
    }
}

