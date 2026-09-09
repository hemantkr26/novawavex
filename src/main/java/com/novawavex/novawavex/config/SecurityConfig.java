package com.novawavex.novawavex.config;

import com.novawavex.novawavex.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        /*
         * =========================================
         * JWT / REST SECURITY
         * =========================================
         */

        http
                .csrf(csrf -> csrf.disable())

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                /*
                 * =========================================
                 * AUTHORIZATION
                 * =========================================
                 */

                .authorizeHttpRequests(auth -> auth

                        /*
                         * =========================================
                         * PUBLIC AUTHENTICATION ENDPOINTS
                         * =========================================
                         */

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        /*
                         * =========================================
                         * SWAGGER / API DOCUMENTATION
                         * =========================================
                         */

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        /*
                         * =========================================
                         * ACTUATOR HEALTH
                         * =========================================
                         */

                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        /*
                         * =========================================
                         * AUTHENTICATED USER PROFILE ENDPOINTS
                         * =========================================
                         */

                        .requestMatchers(
                                "/api/users/me",
                                "/api/users/me/name",
                                "/api/users/me/profile-image"
                        ).authenticated()

                        /*
                         * =========================================
                         * SELF ACCOUNT DELETION
                         * =========================================
                         *
                         * USER and ADMIN can delete only
                         * their own account through /me.
                         *
                         * This matcher is intentionally before
                         * the ADMIN-only /api/users/** matcher.
                         */

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/me"
                        ).authenticated()

                        /*
                         * =========================================
                         * ADMIN-ONLY USER MANAGEMENT
                         * =========================================
                         */

                        .requestMatchers(
                                "/api/users/**"
                        ).hasRole("ADMIN")

                        /*
                         * =========================================
                         * EVERYTHING ELSE
                         * =========================================
                         */

                        .anyRequest()
                        .authenticated()
                )

                /*
                 * =========================================
                 * EXCEPTION HANDLING
                 * =========================================
                 */

                .exceptionHandling(exception -> exception

                        /*
                         * 401
                         */

                        .authenticationEntryPoint(
                                (request, response, authException) -> {

                                    response.setStatus(
                                            HttpServletResponse.SC_UNAUTHORIZED
                                    );

                                    response.setContentType(
                                            "application/json"
                                    );

                                    response.getWriter().write(
                                            """
                                            {
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Authentication is required to access this resource"
                                            }
                                            """
                                    );
                                }
                        )

                        /*
                         * 403
                         */

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {

                                    response.setStatus(
                                            HttpServletResponse.SC_FORBIDDEN
                                    );

                                    response.setContentType(
                                            "application/json"
                                    );

                                    response.getWriter().write(
                                            """
                                            {
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "You do not have permission to access this resource"
                                            }
                                            """
                                    );
                                }
                        )
                )

                /*
                 * =========================================
                 * STATELESS SESSION
                 * =========================================
                 */

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * =========================================
                 * JWT AUTHENTICATION FILTER
                 * =========================================
                 */

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}