
package com.novawavex.novawavex.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordConfigTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        PasswordConfig passwordConfig =
                new PasswordConfig();

        passwordEncoder =
                passwordConfig.passwordEncoder();
    }

    @Test
    void passwordEncoder_shouldCreateBean() {

        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoder_shouldUseBCrypt() {

        assertTrue(
                passwordEncoder instanceof BCryptPasswordEncoder
        );
    }

    @Test
    void passwordEncoder_shouldEncodePassword() {

        String rawPassword = "Test12345";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertFalse(encodedPassword.isBlank());
        assertNotEquals(
                rawPassword,
                encodedPassword
        );
    }

    @Test
    void encodedPassword_shouldMatchOriginalPassword() {

        String rawPassword = "Test12345";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword
                )
        );
    }

    @Test
    void encodedPassword_shouldNotMatchWrongPassword() {

        String rawPassword = "Test12345";
        String wrongPassword = "WrongPassword123";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertFalse(
                passwordEncoder.matches(
                        wrongPassword,
                        encodedPassword
                )
        );
    }

    @Test
    void passwordEncoder_shouldGenerateDifferentHashesForSamePassword() {

        String rawPassword = "Test12345";

        String firstEncodedPassword =
                passwordEncoder.encode(rawPassword);

        String secondEncodedPassword =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(
                firstEncodedPassword,
                secondEncodedPassword
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        firstEncodedPassword
                )
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        secondEncodedPassword
                )
        );
    }
}
