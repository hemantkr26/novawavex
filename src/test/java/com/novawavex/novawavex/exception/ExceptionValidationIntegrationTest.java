package com.novawavex.novawavex.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.dto.AuthRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ExceptionValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * Use a local ObjectMapper.
     *
     * This avoids requiring ObjectMapper
     * to be available as a Spring bean.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private String jwtToken;


    /*
     * =========================================
     * LOGIN
     * =========================================
     */

    @BeforeEach
    void login() throws Exception {

        AuthRequest loginRequest =
                new AuthRequest();

        loginRequest.setEmail(
                "jwttest@novawavex.com"
        );

        loginRequest.setPassword(
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
                                                loginRequest
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

        jwtToken =
                json.get("token").asText();

        assertTrue(
                !jwtToken.isBlank()
        );
    }


    /*
     * =========================================
     * T15.8.1
     *
     * VALIDATION ERROR
     *
     * Invalid login request.
     * =========================================
     */

    @Test
    void invalidLoginRequest_shouldReturn400()
            throws Exception {

        String invalidRequest =
                """
                {
                    "email": "",
                    "password": ""
                }
                """;

        String response =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        invalidRequest
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        assertEquals(
                400,
                json.get("status").asInt()
        );

        assertEquals(
                "Bad Request",
                json.get("error").asText()
        );

        assertEquals(
                "Validation failed",
                json.get("message").asText()
        );

        assertNotNull(
                json.get("timestamp")
        );

        assertNotNull(
                json.get("path")
        );

        assertNotNull(
                json.get("errors")
        );

        assertTrue(
                json.get("errors").has("email")
        );

        assertTrue(
                json.get("errors").has("password")
        );
    }


    /*
     * =========================================
     * T15.8.2
     *
     * UNAUTHORIZED
     *
     * Protected endpoint without JWT.
     * =========================================
     */

    @Test
    void protectedEndpointWithoutJwt_shouldReturn401Or403()
            throws Exception {

        int status =
                mockMvc.perform(
                        get("/api/users/me")
                )
                .andReturn()
                .getResponse()
                .getStatus();

        /*
         * Spring Security may return 401 or 403
         * depending on the configured authentication
         * entry point.
         *
         * Both indicate that the protected endpoint
         * was not accessible.
         */
        assertTrue(
                status == 401 || status == 403
        );
    }


    /*
     * =========================================
     * T15.8.3
     *
     * INVALID JWT
     * =========================================
     */

    @Test
    void protectedEndpointWithInvalidJwt_shouldBeRejected()
            throws Exception {

        int status =
                mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer invalid.jwt.token"
                                )
                )
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(
                status == 401 || status == 403
        );
    }


    /*
     * =========================================
     * T15.8.4
     *
     * RESOURCE NOT FOUND
     *
     * Request a workflow that does not exist.
     * =========================================
     */

    @Test
    void workflowNotFound_shouldReturn404()
            throws Exception {

        String response =
                mockMvc.perform(
                        get("/api/workflows/999999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        assertEquals(
                404,
                json.get("status").asInt()
        );

        assertEquals(
                "Not Found",
                json.get("error").asText()
        );

        assertNotNull(
                json.get("message")
        );

        assertNotNull(
                json.get("path")
        );

        assertNotNull(
                json.get("timestamp")
        );
    }


    /*
     * =========================================
     * T15.8.5
     *
     * INVALID WORKFLOW REQUEST
     *
     * Missing required workflow fields.
     * =========================================
     */

    @Test
    void invalidWorkflowRequest_shouldReturn400()
            throws Exception {

        String invalidWorkflow =
                """
                {
                    "name": "",
                    "description": "",
                    "status": null
                }
                """;

        String response =
                mockMvc.perform(
                        post("/api/workflows")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        invalidWorkflow
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        assertEquals(
                400,
                json.get("status").asInt()
        );

        assertEquals(
                "Bad Request",
                json.get("error").asText()
        );

        assertEquals(
                "Validation failed",
                json.get("message").asText()
        );

        assertNotNull(
                json.get("errors")
        );

        assertTrue(
                json.get("errors").has("name")
        );

        assertTrue(
                json.get("errors").has("status")
        );
    }


    /*
     * =========================================
     * T15.8.6
     *
     * UNKNOWN ENDPOINT
     *
     * Verifies the application's general
     * exception response for an unmapped API path.
     * =========================================
     */

    @Test
    void unknownApiEndpoint_shouldReturnErrorResponse()
            throws Exception {

        String response =
                mockMvc.perform(
                        get("/api/this-endpoint-does-not-exist")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        int status =
                objectMapper
                        .readTree(response)
                        .get("status")
                        .asInt();

        /*
         * Depending on Spring Boot's resource handling
         * configuration, this may be 404 or 500.
         *
         * The important part is that the request does
         * not result in a successful 2xx response.
         */
        assertTrue(
                status == 404 || status == 500
        );

        JsonNode json =
                objectMapper.readTree(response);

        assertNotNull(
                json.get("status")
        );

        assertNotNull(
                json.get("message")
        );

        assertNotNull(
                json.get("path")
        );

        assertNotNull(
                json.get("timestamp")
        );
    }

}