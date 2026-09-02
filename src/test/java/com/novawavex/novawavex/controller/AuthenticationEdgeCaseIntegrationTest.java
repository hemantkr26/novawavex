
package com.novawavex.novawavex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.dto.AuthRequest;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationEdgeCaseIntegrationTest {


    @Autowired
    private MockMvc mockMvc;


    /*
     * =========================================
     * OBJECT MAPPER
     * =========================================
     *
     * ObjectMapper is created locally because
     * NovaWavex does not expose ObjectMapper
     * as a Spring bean in this test context.
     */

    private final ObjectMapper objectMapper =
            new ObjectMapper();


    /*
     * =========================================
     * T16.1.1
     *
     * BLANK EMAIL
     * =========================================
     */

    @Test
    void login_withBlankEmail_shouldReturnBadRequest()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail("");
        request.setPassword("Test12345");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.2
     *
     * BLANK PASSWORD
     * =========================================
     */

    @Test
    void login_withBlankPassword_shouldReturnBadRequest()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwttest@novawavex.com"
        );

        request.setPassword("");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.3
     *
     * INVALID EMAIL FORMAT
     * =========================================
     */

    @Test
    void login_withInvalidEmail_shouldReturnBadRequest()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "invalid-email"
        );

        request.setPassword(
                "Test12345"
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.4
     *
     * UNKNOWN EMAIL
     * =========================================
     */

    @Test
    void login_withUnknownEmail_shouldReturnUnauthorized()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "does-not-exist@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.1.5
     *
     * WRONG PASSWORD
     * =========================================
     */

    @Test
    void login_withWrongPassword_shouldReturnUnauthorized()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwttest@novawavex.com"
        );

        request.setPassword(
                "WrongPassword123"
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.1.6
     *
     * NULL EMAIL
     * =========================================
     */

    @Test
    void login_withNullEmail_shouldReturnBadRequest()
            throws Exception {

        String json =
                """
                {
                    "email": null,
                    "password": "Test12345"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.7
     *
     * NULL PASSWORD
     * =========================================
     */

    @Test
    void login_withNullPassword_shouldReturnBadRequest()
            throws Exception {

        String json =
                """
                {
                    "email": "jwttest@novawavex.com",
                    "password": null
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.8
     *
     * MISSING EMAIL FIELD
     * =========================================
     */

    @Test
    void login_withMissingEmail_shouldReturnBadRequest()
            throws Exception {

        String json =
                """
                {
                    "password": "Test12345"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.9
     *
     * MISSING PASSWORD FIELD
     * =========================================
     */

    @Test
    void login_withMissingPassword_shouldReturnBadRequest()
            throws Exception {

        String json =
                """
                {
                    "email": "jwttest@novawavex.com"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.10
     *
     * EMPTY JSON OBJECT
     * =========================================
     */

    @Test
    void login_withEmptyJson_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.11
     *
     * MALFORMED JSON
     * =========================================
     *
     * Current application behavior:
     *
     * Malformed JSON reaches the global
     * Exception handler and is currently
     * returned as HTTP 500.
     *
     * We are intentionally NOT changing
     * the main application files.
     *
     * Therefore this test verifies the
     * current behavior rather than requiring
     * an application change.
     */

    @Test
    void login_withMalformedJson_shouldReturnInternalServerError()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                "{\"email\":\"jwttest@novawavex.com\","
                                        + "\"password\":"
                                        + "\"Test12345\""
                        )
        )
        .andExpect(
                status().isInternalServerError()
        );
    }


    /*
     * =========================================
     * T16.1.12
     *
     * PASSWORD BELOW MINIMUM LENGTH
     * =========================================
     */

    @Test
    void login_withShortPassword_shouldReturnBadRequest()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwttest@novawavex.com"
        );

        request.setPassword(
                "1234567"
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.13
     *
     * PASSWORD ABOVE MAXIMUM LENGTH
     * =========================================
     */

    @Test
    void login_withOversizedPassword_shouldReturnBadRequest()
            throws Exception {

        String oversizedPassword =
                "A".repeat(101);

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwttest@novawavex.com"
        );

        request.setPassword(
                oversizedPassword
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.14
     *
     * EMAIL ABOVE MAXIMUM LENGTH
     * =========================================
     */

    @Test
    void login_withOversizedEmail_shouldReturnBadRequest()
            throws Exception {

        String oversizedEmail =
                "a".repeat(140)
                        + "@example.com";

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                oversizedEmail
        );

        request.setPassword(
                "Test12345"
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.1.15
     *
     * VALID LOGIN
     *
     * CONTROL TEST
     * =========================================
     */

    @Test
    void login_withValidCredentials_shouldReturnToken()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwttest@novawavex.com"
        );

        request.setPassword(
                "Test12345"
        );

        String response =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);

        assertTrue(
                response.contains("token"),
                "Successful login response should contain a token"
        );
    }
}
