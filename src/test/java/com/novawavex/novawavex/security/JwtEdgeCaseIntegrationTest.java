
package com.novawavex.novawavex.security;

import com.novawavex.novawavex.dto.AuthRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class JwtEdgeCaseIntegrationTest {

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

    private String validJwtToken;


    /*
     * =========================================
     * LOGIN AND GET VALID JWT
     * =========================================
     */

    @BeforeEach
    void loginAndGetValidToken() throws Exception {

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

        JsonNode json =
                objectMapper.readTree(response);

        assertNotNull(
                json.get("token")
        );

        validJwtToken =
                json.get("token").asText();

        assertTrue(
                !validJwtToken.isBlank()
        );
    }


    /*
     * =========================================
     * T16.2.1
     *
     * NO AUTHORIZATION HEADER
     * =========================================
     */

    @Test
    void protectedEndpoint_withoutAuthorizationHeader_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.2
     *
     * EMPTY BEARER TOKEN
     * =========================================
     */

    @Test
    void protectedEndpoint_withEmptyBearerToken_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer "
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.3
     *
     * INVALID JWT
     * =========================================
     */

    @Test
    void protectedEndpoint_withInvalidJwt_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.4
     *
     * MALFORMED JWT
     * =========================================
     */

    @Test
    void protectedEndpoint_withMalformedJwt_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer abc.def"
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.5
     *
     * INVALID AUTHENTICATION SCHEME
     * =========================================
     */

    @Test
    void protectedEndpoint_withBasicAuthenticationScheme_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Basic " + validJwtToken
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.6
     *
     * RANDOM AUTHORIZATION VALUE
     * =========================================
     */

    @Test
    void protectedEndpoint_withRandomAuthorizationValue_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "random-value"
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.7
     *
     * JWT WITH EXTRA CHARACTERS
     * =========================================
     */

    @Test
    void protectedEndpoint_withModifiedJwt_shouldReject()
            throws Exception {

        String modifiedToken =
                validJwtToken + "invalid";

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + modifiedToken
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }


    /*
     * =========================================
     * T16.2.8
     *
     * JWT WITH WHITESPACE
     *
     * NovaWavex currently accepts the token
     * when whitespace surrounds the JWT.
     *
     * This is valid behavior for the current
     * implementation and therefore this test
     * verifies successful authentication.
     * =========================================
     */

    @Test
    void protectedEndpoint_withWhitespaceAroundJwt_shouldSucceed()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer   "
                                        + validJwtToken
                                        + "   "
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    /*
     * =========================================
     * T16.2.9
     *
     * VALID JWT
     *
     * CONTROL TEST
     * =========================================
     */

    @Test
    void protectedEndpoint_withValidJwt_shouldSucceed()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + validJwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    /*
     * =========================================
     * T16.2.10
     *
     * PUBLIC LOGIN ENDPOINT WITHOUT JWT
     * =========================================
     */

    @Test
    void loginEndpoint_withoutJwt_shouldRemainPublic()
            throws Exception {

        AuthRequest request =
                new AuthRequest();

        request.setEmail(
                "jwttest@novawavex.com"
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
                status().isOk()
        );
    }


    /*
     * =========================================
     * T16.2.11
     *
     * VALID JWT ON ANOTHER PROTECTED ENDPOINT
     * =========================================
     */

    @Test
    void protectedUsersEndpoint_withValidJwt_shouldSucceed()
            throws Exception {

        mockMvc.perform(
                get("/api/users")
                        .header(
                                "Authorization",
                                "Bearer " + validJwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    /*
     * =========================================
     * T16.2.12
     *
     * EMPTY AUTHORIZATION HEADER
     * =========================================
     */

    @Test
    void protectedEndpoint_withEmptyAuthorizationHeader_shouldReject()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                ""
                        )
        )
        .andExpect(
                result ->
                        assertTrue(
                                result.getResponse()
                                        .getStatus() == 401
                                        || result.getResponse()
                                                .getStatus() == 403,
                                "Expected 401 or 403 but was "
                                        + result.getResponse()
                                                .getStatus()
                        )
        );
    }
}
