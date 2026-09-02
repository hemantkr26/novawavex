package com.novawavex.novawavex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {


@Value("${app.cors.allowed-origin}")
private String allowedOrigin;

@Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration =
            new CorsConfiguration();

    /*
     * Allowed frontend origin.
     *
     * Development:
     * http://localhost:5173
     *
     * Production:
     * Value comes from FRONTEND_URL environment variable.
     */
    configuration.setAllowedOrigins(
            List.of(allowedOrigin)
    );

    /*
     * Only allow the HTTP methods required
     * by the application.
     */
    configuration.setAllowedMethods(
            List.of(
                    "GET",
                    "POST",
                    "PUT",
                    "DELETE",
                    "OPTIONS"
            )
    );

    /*
     * Allow headers required by the frontend,
     * including the Authorization header used
     * for JWT authentication.
     */
    configuration.setAllowedHeaders(
            List.of(
                    "Authorization",
                    "Content-Type",
                    "Accept"
            )
    );

    /*
     * Allow cookies/credentials when required.
     */
    configuration.setAllowCredentials(true);

    /*
     * Cache preflight requests for one hour.
     */
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration(
            "/**",
            configuration
    );

    return source;
}


}
