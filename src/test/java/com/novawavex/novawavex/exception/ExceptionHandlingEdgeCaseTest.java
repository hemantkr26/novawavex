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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlingEdgeCaseTest {


@Autowired
private MockMvc mockMvc;

/*
 * NovaWavex does not expose ObjectMapper
 * as a Spring bean in this test context.
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
 * T16.10.1
 *
 * VALIDATION EXCEPTION
 * =========================================
 */

@Test
void validationError_shouldReturnBadRequest()
        throws Exception {

    String json =
            """
            {
                "email": "",
                "password": ""
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
    )
    .andExpect(
            jsonPath("$.status").value(400)
    )
    .andExpect(
            jsonPath("$.error").value("Bad Request")
    )
    .andExpect(
            jsonPath("$.message").value(
                    "Validation failed"
            )
    )
    .andExpect(
            jsonPath("$.path").value(
                    "/api/auth/login"
            )
    );
}


/*
 * =========================================
 * T16.10.2
 *
 * VALIDATION ERROR
 * FIELD DETAILS
 * =========================================
 */

@Test
void validationError_shouldContainFieldErrors()
        throws Exception {

    String json =
            """
            {
                "email": "",
                "password": ""
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
    )
    .andExpect(
            jsonPath("$.errors").exists()
    )
    .andExpect(
            jsonPath("$.errors.email").exists()
    )
    .andExpect(
            jsonPath("$.errors.password").exists()
    );
}


/*
 * =========================================
 * T16.10.3
 *
 * RESOURCE NOT FOUND
 * =========================================
 */

@Test
void nonexistentWorkflow_shouldReturnNotFound()
        throws Exception {

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
    .andExpect(
            jsonPath("$.status").value(404)
    )
    .andExpect(
            jsonPath("$.error").value("Not Found")
    )
    .andExpect(
            jsonPath("$.path").value(
                    "/api/workflows/999999999"
            )
    );
}


/*
 * =========================================
 * T16.10.4
 *
 * RESOURCE NOT FOUND
 * EXECUTION
 * =========================================
 */

@Test
void nonexistentExecution_shouldReturnNotFound()
        throws Exception {

    mockMvc.perform(
            get("/api/executions/999999999")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isNotFound()
    )
    .andExpect(
            jsonPath("$.status").value(404)
    )
    .andExpect(
            jsonPath("$.error").value("Not Found")
    );
}


/*
 * =========================================
 * T16.10.5
 *
 * UNAUTHORIZED REQUEST
 * =========================================
 */

@Test
void protectedEndpointWithoutAuthentication_shouldReturnUnauthorized()
        throws Exception {

    mockMvc.perform(
            get("/api/workflows")
    )
    .andExpect(
            status().isUnauthorized()
    );
}


/*
 * =========================================
 * T16.10.6
 *
 * MALFORMED JSON
 * =========================================
 */

@Test
void malformedJson_shouldReturnBadRequest()
        throws Exception {

    String malformedJson =
            """
            {
                "email": "broken@example.com",
                "password":
            """;

    mockMvc.perform(
            post("/api/auth/login")
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .content(malformedJson)
    )
    .andExpect(
            status().isBadRequest()
    );
}


/*
 * =========================================
 * T16.10.7
 *
 * INVALID PATH VARIABLE
 * =========================================
 */

@Test
void invalidWorkflowId_shouldReturnClientError()
        throws Exception {

    mockMvc.perform(
            get("/api/workflows/not-a-number")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().is4xxClientError()
    );
}


/*
 * =========================================
 * T16.10.8
 *
 * INVALID HTTP METHOD
 * =========================================
 */

@Test
void unsupportedHttpMethod_shouldReturnMethodError()
        throws Exception {

    mockMvc.perform(
            put("/api/auth/login")
                    .contentType(
                            MediaType.APPLICATION_JSON
                    )
                    .content(
                            """
                            {
                                "email": "jwttest@novawavex.com",
                                "password": "Test12345"
                            }
                            """
                    )
    )
    .andExpect(
            status().is4xxClientError()
    );
}


/*
 * =========================================
 * T16.10.9
 *
 * UNKNOWN ENDPOINT
 * =========================================
 */

@Test
void unknownEndpoint_shouldReturnNotFound()
        throws Exception {

    mockMvc.perform(
            get("/api/this-endpoint-does-not-exist")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isNotFound()
    );
}


/*
 * =========================================
 * T16.10.10
 *
 * INVALID JSON CONTENT TYPE
 * =========================================
 */

@Test
void invalidContentType_shouldReturnClientError()
        throws Exception {

    mockMvc.perform(
            post("/api/auth/login")
                    .contentType(
                            MediaType.TEXT_PLAIN
                    )
                    .content(
                            "email=jwttest@novawavex.com"
                    )
    )
    .andExpect(
            status().is4xxClientError()
    );
}

}
