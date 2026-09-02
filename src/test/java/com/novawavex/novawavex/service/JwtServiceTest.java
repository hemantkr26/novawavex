package com.novawavex.novawavex.service;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;

    private static final Long USER_ID = 101L;

    private static final String EMAIL =
            "jwt-test@novawavex.com";

    private static final String FULL_NAME =
            "JWT Test User";

    private static final String ROLE =
            "USER";

    private static final String SECRET_KEY =
            "NovaWavexSecretKeyForJwtAuthentication2026SecureKey";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();
    }


    // =========================================================
    // 1. GENERATE TOKEN
    // =========================================================

    @Test
    void generateToken_shouldReturnNonNullToken() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        assertNotNull(token);

        assertFalse(
                token.isBlank()
        );
    }


    // =========================================================
    // 2. EXTRACT EMAIL
    // =========================================================

    @Test
    void extractEmail_shouldReturnCorrectEmail() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        String extractedEmail =
                jwtService.extractEmail(token);

        assertEquals(
                EMAIL,
                extractedEmail
        );
    }


    // =========================================================
    // 3. EXTRACT ROLE
    // =========================================================

    @Test
    void extractRole_shouldReturnCorrectRole() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        String extractedRole =
                jwtService.extractRole(token);

        assertEquals(
                ROLE,
                extractedRole
        );
    }


    // =========================================================
    // 4. EXTRACT USER ID
    // =========================================================

    @Test
    void extractId_shouldReturnCorrectUserId() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        Long extractedId =
                jwtService.extractId(token);

        assertEquals(
                USER_ID,
                extractedId
        );
    }


    // =========================================================
    // 5. EXTRACT FULL NAME
    // =========================================================

    @Test
    void extractFullName_shouldReturnCorrectFullName() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        String extractedFullName =
                jwtService.extractFullName(token);

        assertEquals(
                FULL_NAME,
                extractedFullName
        );
    }


    // =========================================================
    // 6. TOKEN SUBJECT SHOULD BE EMAIL
    // =========================================================

    @Test
    void generateToken_shouldUseEmailAsSubject() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        assertEquals(
                EMAIL,
                jwtService.extractEmail(token)
        );
    }


    // =========================================================
    // 7. TOKEN SHOULD CONTAIN USER ID CLAIM
    // =========================================================

    @Test
    void generateToken_shouldContainUserIdClaim() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        assertEquals(
                USER_ID,
                jwtService.extractId(token)
        );
    }


    // =========================================================
    // 8. TOKEN SHOULD CONTAIN FULL NAME CLAIM
    // =========================================================

    @Test
    void generateToken_shouldContainFullNameClaim() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        assertEquals(
                FULL_NAME,
                jwtService.extractFullName(token)
        );
    }


    // =========================================================
    // 9. TOKEN SHOULD CONTAIN ROLE CLAIM
    // =========================================================

    @Test
    void generateToken_shouldContainRoleClaim() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        assertEquals(
                ROLE,
                jwtService.extractRole(token)
        );
    }


    // =========================================================
    // 10. TOKEN SHOULD HAVE ISSUED AT
    // =========================================================

    @Test
    void generateToken_shouldContainIssuedAt() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        Claims claims =
                parseToken(token);

        assertNotNull(
                claims.getIssuedAt()
        );
    }


    // =========================================================
    // 11. TOKEN SHOULD HAVE EXPIRATION
    // =========================================================

    @Test
    void generateToken_shouldContainExpiration() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        Claims claims =
                parseToken(token);

        assertNotNull(
                claims.getExpiration()
        );
    }


    // =========================================================
    // 12. TOKEN SHOULD EXPIRE IN APPROXIMATELY ONE HOUR
    // =========================================================

    @Test
    void generateToken_shouldExpireApproximatelyOneHourAfterIssuance() {

        long beforeGeneration =
                System.currentTimeMillis();

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        long afterGeneration =
                System.currentTimeMillis();

        Claims claims =
                parseToken(token);

        Date issuedAt =
                claims.getIssuedAt();

        Date expiration =
                claims.getExpiration();

        assertNotNull(issuedAt);

        assertNotNull(expiration);

        long expirationDuration =
                expiration.getTime()
                        - issuedAt.getTime();

        long expectedDuration =
                60 * 60 * 1000L;

        assertEquals(
                expectedDuration,
                expirationDuration
        );

        assertTrue(
                issuedAt.getTime()
                        >= beforeGeneration - 1000
        );

        assertTrue(
                issuedAt.getTime()
                        <= afterGeneration + 1000
        );
    }


    // =========================================================
    // 13. VALID TOKEN SHOULD BE PARSED SUCCESSFULLY
    // =========================================================

    @Test
    void generateToken_shouldProduceValidSignedToken() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        assertDoesNotThrow(
                () -> parseToken(token)
        );
    }


    // =========================================================
    // 14. TAMPERED TOKEN SHOULD BE REJECTED
    // =========================================================

    @Test
    void extractEmail_withTamperedToken_shouldThrowException() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        String tamperedToken =
                token.substring(
                        0,
                        token.length() - 1
                )
                + (token.endsWith("a") ? "b" : "a");

        assertThrows(
                Exception.class,
                () -> jwtService.extractEmail(
                        tamperedToken
                )
        );
    }


    // =========================================================
    // 15. INVALID TOKEN SHOULD BE REJECTED
    // =========================================================

    @Test
    void extractEmail_withInvalidToken_shouldThrowException() {

        String invalidToken =
                "this.is.not.a.valid.jwt";

        assertThrows(
                Exception.class,
                () -> jwtService.extractEmail(
                        invalidToken
                )
        );
    }


    // =========================================================
    // 16. NULL TOKEN SHOULD BE REJECTED
    // =========================================================

    @Test
    void extractEmail_withNullToken_shouldThrowException() {

        assertThrows(
                Exception.class,
                () -> jwtService.extractEmail(null)
        );
    }


    // =========================================================
    // 17. EMPTY TOKEN SHOULD BE REJECTED
    // =========================================================

    @Test
    void extractEmail_withEmptyToken_shouldThrowException() {

        assertThrows(
                Exception.class,
                () -> jwtService.extractEmail("")
        );
    }


    // =========================================================
    // 18. DIFFERENT USERS SHOULD PRODUCE DIFFERENT TOKENS
    // =========================================================

    @Test
    void generateToken_forDifferentUsers_shouldProduceDifferentTokens() {

        String tokenA =
                jwtService.generateToken(
                        101L,
                        "user-a@novawavex.com",
                        "User A",
                        "USER"
                );

        String tokenB =
                jwtService.generateToken(
                        102L,
                        "user-b@novawavex.com",
                        "User B",
                        "USER"
                );

        assertNotEquals(
                tokenA,
                tokenB
        );

        assertEquals(
                "user-a@novawavex.com",
                jwtService.extractEmail(tokenA)
        );

        assertEquals(
                "user-b@novawavex.com",
                jwtService.extractEmail(tokenB)
        );

        assertEquals(
                101L,
                jwtService.extractId(tokenA)
        );

        assertEquals(
                102L,
                jwtService.extractId(tokenB)
        );
    }


    // =========================================================
    // 19. ADMIN ROLE SHOULD BE PRESERVED
    // =========================================================

    @Test
    void generateToken_withAdminRole_shouldPreserveAdminRole() {

        String token =
                jwtService.generateToken(
                        500L,
                        "admin@novawavex.com",
                        "Admin User",
                        "ADMIN"
                );

        assertEquals(
                "ADMIN",
                jwtService.extractRole(token)
        );
    }


    // =========================================================
    // 20. SPECIAL CHARACTERS IN FULL NAME
    // =========================================================

    @Test
    void generateToken_withSpecialCharactersInFullName_shouldPreserveFullName() {

        String specialFullName =
                "Hemant Kumar - Test User";

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        specialFullName,
                        ROLE
                );

        assertEquals(
                specialFullName,
                jwtService.extractFullName(token)
        );
    }


    // =========================================================
    // 21. TOKEN SHOULD BE SIGNED WITH EXPECTED SECRET
    // =========================================================

    @Test
    void generateToken_shouldBeVerifiableWithConfiguredSecret() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        SecretKey key =
                Keys.hmacShaKeyFor(
                        SECRET_KEY.getBytes()
                );

        assertDoesNotThrow(
                () -> Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
        );
    }


    // =========================================================
    // 22. TOKEN CLAIMS SHOULD CONTAIN EXPECTED VALUES
    // =========================================================

    @Test
    void generateToken_shouldContainAllExpectedClaims() {

        String token =
                jwtService.generateToken(
                        USER_ID,
                        EMAIL,
                        FULL_NAME,
                        ROLE
                );

        Claims claims =
                parseToken(token);

        assertEquals(
                EMAIL,
                claims.getSubject()
        );

        assertEquals(
                USER_ID,
                claims.get("id", Long.class)
        );

        assertEquals(
                EMAIL,
                claims.get("email", String.class)
        );

        assertEquals(
                FULL_NAME,
                claims.get("fullName", String.class)
        );

        assertEquals(
                ROLE,
                claims.get("role", String.class)
        );

        assertNotNull(
                claims.getIssuedAt()
        );

        assertNotNull(
                claims.getExpiration()
        );
    }


    // =========================================================
    // HELPER
    // =========================================================

    private Claims parseToken(
            String token) {

        SecretKey key =
                Keys.hmacShaKeyFor(
                        SECRET_KEY.getBytes()
                );

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}