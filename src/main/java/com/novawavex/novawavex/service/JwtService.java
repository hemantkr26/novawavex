package com.novawavex.novawavex.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	private static final String SECRET_KEY =
	        System.getenv("JWT_SECRET");

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60;

    private final SecretKey key;

    public JwtService() {

        this.key = Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    /*
     * =========================================
     * GENERATE JWT
     * =========================================
     */

    public String generateToken(
            Long id,
            String email,
            String fullName,
            String role
    ) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()

                /*
                 * Subject
                 */

                .subject(email)

                /*
                 * User ID
                 */

                .claim("id", id)

                /*
                 * Email
                 */

                .claim("email", email)

                /*
                 * Full Name
                 */

                .claim("fullName", fullName)

                /*
                 * Role
                 */

                .claim("role", role)

                /*
                 * Timestamps
                 */

                .issuedAt(now)

                .expiration(expiration)

                /*
                 * Sign token
                 */

                .signWith(key)

                .compact();
    }


    /*
     * =========================================
     * EXTRACT EMAIL
     * =========================================
     */

    public String extractEmail(String token) {

        return extractAllClaims(token)
                .getSubject();
    }


    /*
     * =========================================
     * EXTRACT ROLE
     * =========================================
     */

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }


    /*
     * =========================================
     * EXTRACT USER ID
     * =========================================
     */

    public Long extractId(String token) {

        return extractAllClaims(token)
                .get("id", Long.class);
    }


    /*
     * =========================================
     * EXTRACT FULL NAME
     * =========================================
     */

    public String extractFullName(String token) {

        return extractAllClaims(token)
                .get("fullName", String.class);
    }


    /*
     * =========================================
     * EXTRACT ALL CLAIMS
     * =========================================
     */

    private Claims extractAllClaims(String token) {

        return Jwts.parser()

                .verifyWith(key)

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }
}