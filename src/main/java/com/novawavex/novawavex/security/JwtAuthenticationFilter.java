package com.novawavex.novawavex.security;

import com.novawavex.novawavex.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService = jwtService;
    }

    /*
     * =========================================
     * PUBLIC AUTHENTICATION ENDPOINTS
     * =========================================
     */

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password");
    }

    /*
     * =========================================
     * JWT FILTER
     * =========================================
     */

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * =====================================
         * AUTHORIZATION HEADER
         * =====================================
         */

        String authHeader =
                request.getHeader("Authorization");

        /*
         * =====================================
         * NO JWT
         * =====================================
         *
         * Allow Spring Security to decide
         * whether the endpoint is public or
         * requires authentication.
         */

        if (authHeader == null || authHeader.isBlank()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        /*
         * =====================================
         * INVALID AUTHORIZATION SCHEME
         * =====================================
         */

        if (!authHeader.startsWith("Bearer ")) {

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(
                    response,
                    request,
                    "Invalid Authorization header"
            );

            return;
        }

        /*
         * =====================================
         * EXTRACT TOKEN
         * =====================================
         */

        String token =
                authHeader
                        .substring(7)
                        .trim();

        /*
         * =====================================
         * EMPTY TOKEN
         * =====================================
         */

        if (token.isBlank()) {

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(
                    response,
                    request,
                    "JWT token is missing"
            );

            return;
        }

        /*
         * =====================================
         * VALIDATE JWT
         * =====================================
         */

        try {

            String email =
                    jwtService.extractEmail(token);

            String role =
                    jwtService.extractRole(token);

            /*
             * =================================
             * VALIDATE EMAIL
             * =================================
             */

            if (email == null || email.isBlank()) {

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(
                        response,
                        request,
                        "Invalid JWT token"
                );

                return;
            }

            /*
             * =================================
             * VALIDATE ROLE
             * =================================
             */

            if (role == null || role.isBlank()) {

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(
                        response,
                        request,
                        "Invalid JWT token"
                );

                return;
            }

            /*
             * =================================
             * VALIDATE SUPPORTED ROLE
             * =================================
             *
             * NovaWavex currently supports only:
             *
             * USER
             * ADMIN
             *
             * Reject any unexpected role value.
             */

            if (!role.equals("USER")
                    && !role.equals("ADMIN")) {

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(
                        response,
                        request,
                        "Invalid JWT role"
                );

                return;
            }

            /*
             * =================================
             * CREATE AUTHENTICATION
             * =================================
             */

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }

            /*
             * =================================
             * CONTINUE REQUEST
             * =================================
             */

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (Exception exception) {

            /*
             * =================================
             * INVALID / EXPIRED JWT
             * =================================
             */

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(
                    response,
                    request,
                    "Invalid or expired JWT token"
            );
        }
    }

    /*
     * =========================================
     * 401 RESPONSE
     * =========================================
     */

    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            HttpServletRequest request,
            String message)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        /*
         * Keep the content type exactly
         * application/json.
         */

        response.setContentType(
                "application/json"
        );

        String jsonResponse =
                "{"
                        + "\"status\":401,"
                        + "\"error\":\"Unauthorized\","
                        + "\"message\":\""
                        + escapeJson(message)
                        + "\","
                        + "\"path\":\""
                        + escapeJson(
                                request.getRequestURI()
                        )
                        + "\""
                        + "}";

        response.getWriter().write(
                jsonResponse
        );
    }

    /*
     * =========================================
     * JSON ESCAPING
     * =========================================
     */

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}