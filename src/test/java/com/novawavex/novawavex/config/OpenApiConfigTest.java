
package com.novawavex.novawavex.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class OpenApiConfigTest {

    private OpenAPI openAPI;

    @BeforeEach
    void setUp() {
        OpenApiConfig openApiConfig = new OpenApiConfig();
        openAPI = openApiConfig.customOpenAPI();
    }

    @Test
    void customOpenAPI_shouldCreateOpenAPIObject() {

        assertNotNull(openAPI);
    }

    @Test
    void shouldConfigureApiInformation() {

        assertNotNull(openAPI.getInfo());

        assertEquals(
                "NovaWavex API",
                openAPI.getInfo().getTitle()
        );

        assertEquals(
                "1.0.0",
                openAPI.getInfo().getVersion()
        );

        assertNotNull(
                openAPI.getInfo().getDescription()
        );
    }

    @Test
    void shouldConfigureContact() {

        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getContact());

        assertEquals(
                "NovaWavex",
                openAPI.getInfo().getContact().getName()
        );
    }

    @Test
    void shouldConfigureLicense() {

        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getLicense());

        assertEquals(
                "Apache 2.0",
                openAPI.getInfo().getLicense().getName()
        );
    }

    @Test
    void shouldConfigureBearerAuthenticationScheme() {

        assertNotNull(openAPI.getComponents());
        assertNotNull(
                openAPI.getComponents().getSecuritySchemes()
        );

        SecurityScheme securityScheme =
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .get("bearerAuth");

        assertNotNull(securityScheme);

        assertEquals(
                SecurityScheme.Type.HTTP,
                securityScheme.getType()
        );

        assertEquals(
                "bearer",
                securityScheme.getScheme()
        );

        assertEquals(
                "JWT",
                securityScheme.getBearerFormat()
        );
    }

    @Test
    void shouldConfigureBearerAuthenticationDescription() {

        SecurityScheme securityScheme =
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .get("bearerAuth");

        assertNotNull(securityScheme);
        assertNotNull(securityScheme.getDescription());

        assertEquals(
                true,
                securityScheme.getDescription()
                        .contains("JWT token")
        );
    }
}

