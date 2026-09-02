package com.novawavex.novawavex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                /*
                 * API Information
                 */
                .info(
                        new Info()
                                .title("NovaWavex API")
                                .version("1.0.0")
                                .description(
                                        "NovaWavex Workflow Management API. "
                                                + "Provides authentication, "
                                                + "workflow management, "
                                                + "workflow execution, "
                                                + "user management, and "
                                                + "system health APIs."
                                )
                                .contact(
                                        new Contact()
                                                .name("NovaWavex")
                                )
                                .license(
                                        new License()
                                                .name("Apache 2.0")
                                )
                )

                /*
                 * JWT Security Configuration
                 */
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(
                                                        SecurityScheme.Type.HTTP
                                                )
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Enter your JWT token. "
                                                                + "Example: "
                                                                + "Bearer eyJhbGciOiJIUzI1NiJ9..."
                                                )
                                )
                );
    }
}