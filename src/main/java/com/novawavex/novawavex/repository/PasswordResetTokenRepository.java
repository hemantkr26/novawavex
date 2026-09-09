package com.novawavex.novawavex.repository;

import com.novawavex.novawavex.entity.PasswordResetToken;
import com.novawavex.novawavex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(
            String token
    );

    void deleteByUserAndUsedFalse(
            User user
    );

    void deleteByUser(
            User user
    );
}