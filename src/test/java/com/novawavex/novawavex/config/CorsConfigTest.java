
package com.novawavex.novawavex.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    private CorsConfigurationSource corsConfigurationSource;

    @BeforeEach
    void setUp() {
        CorsConfig corsConfig = new CorsConfig();
        corsConfigurationSource =
                corsConfig.corsConfigurationSource();
    }

    @Test
    void corsConfigurationSource_shouldCreateConfiguration() {

        assertNotNull(corsConfigurationSource);
    }

    @Test
    void shouldAllowFrontendOrigin() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://localhost:5173")
                );

        assertNotNull(configuration);

        assertEquals(
                List.of("http://localhost:5173"),
                configuration.getAllowedOrigins()
        );
    }

    @Test
    void shouldAllowRequiredHttpMethods() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://localhost:5173")
                );

        assertNotNull(configuration);

        assertEquals(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                ),
                configuration.getAllowedMethods()
        );
    }

    @Test
    void shouldAllowRequiredHeaders() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://localhost:5173")
                );

        assertNotNull(configuration);

        assertEquals(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                ),
                configuration.getAllowedHeaders()
        );
    }

    @Test
    void shouldAllowCredentials() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://localhost:5173")
                );

        assertNotNull(configuration);

        assertTrue(configuration.getAllowCredentials());
    }

    @Test
    void shouldSetPreflightMaxAgeToOneHour() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://localhost:5173")
                );

        assertNotNull(configuration);

        assertEquals(
                3600L,
                configuration.getMaxAge()
        );
    }

    @Test
    void shouldApplyConfigurationToAllPaths() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://localhost:5173")
                );

        assertNotNull(configuration);
    }

    @Test
    void shouldNotAllowUnconfiguredOrigin() {

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(
                        createRequest("http://malicious.example.com")
                );

        assertNotNull(configuration);

        assertFalse(
                configuration.checkOrigin(
                        "http://malicious.example.com"
                ) != null
        );
    }

    private jakarta.servlet.http.HttpServletRequest createRequest(
            String origin) {

        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        request.setRequestURI("/api/test");
        request.addHeader("Origin", origin);

        return request;
    }
}
