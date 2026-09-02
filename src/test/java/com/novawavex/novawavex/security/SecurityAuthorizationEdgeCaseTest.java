package com.novawavex.novawavex.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.WorkflowRequest;
import com.novawavex.novawavex.workflow.WorkflowStatus;

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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationEdgeCaseTest {


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
 * T16.9.1
 *
 * PROTECTED ENDPOINT
 * NO JWT
 * =========================================
 */

@Test
void protectedEndpoint_withoutJwt_shouldReturnUnauthorized()
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
 * T16.9.2
 *
 * MALFORMED JWT
 * =========================================
 */

@Test
void protectedEndpoint_withMalformedJwt_shouldReturnUnauthorized()
        throws Exception {

    mockMvc.perform(
            get("/api/workflows")
                    .header(
                            "Authorization",
                            "Bearer malformed-token"
                    )
    )
    .andExpect(
            status().isUnauthorized()
    );
}


/*
 * =========================================
 * T16.9.3
 *
 * INVALID JWT
 * =========================================
 */

@Test
void protectedEndpoint_withInvalidJwt_shouldReturnUnauthorized()
        throws Exception {

    mockMvc.perform(
            get("/api/workflows")
                    .header(
                            "Authorization",
                            "Bearer invalid.jwt.token"
                    )
    )
    .andExpect(
            status().isUnauthorized()
    );
}


/*
 * =========================================
 * T16.9.4
 *
 * WORKFLOW ENDPOINT
 * NO AUTHENTICATION
 * =========================================
 */

@Test
void workflowEndpoint_withoutAuthentication_shouldReturnUnauthorized()
        throws Exception {

    WorkflowRequest request =
            new WorkflowRequest();

    request.setName(
            "Unauthorized Workflow"
    );

    request.setDescription(
            "Security edge-case test"
    );

    request.setStatus(
            WorkflowStatus.DRAFT
    );

    mockMvc.perform(
            post("/api/workflows")
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
 * T16.9.5
 *
 * EXECUTION ENDPOINT
 * NO AUTHENTICATION
 * =========================================
 */

@Test
void executionEndpoint_withoutAuthentication_shouldReturnUnauthorized()
        throws Exception {

    mockMvc.perform(
            get("/api/executions")
    )
    .andExpect(
            status().isUnauthorized()
    );
}


/*
 * =========================================
 * T16.9.6
 *
 * NOTIFICATION ENDPOINT
 * NO AUTHENTICATION
 * =========================================
 */

@Test
void notificationEndpoint_withoutAuthentication_shouldReturnUnauthorized()
        throws Exception {

    mockMvc.perform(
            get("/api/notifications")
                    .param(
                            "userId",
                            "1"
                    )
    )
    .andExpect(
            status().isUnauthorized()
    );
}


/*
 * =========================================
 * T16.9.7
 *
 * WORKFLOW
 * NONEXISTENT RESOURCE
 * =========================================
 */

@Test
void workflowEndpoint_withNonexistentId_shouldReturnNotFound()
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
    );
}


/*
 * =========================================
 * T16.9.8
 *
 * WORKFLOW UPDATE
 * NONEXISTENT RESOURCE
 * =========================================
 */

@Test
void updateWorkflow_withNonexistentId_shouldReturnNotFound()
        throws Exception {

    WorkflowRequest request =
            new WorkflowRequest();

    request.setName(
            "Security Update Test"
    );

    request.setDescription(
            "Security edge-case test"
    );

    request.setStatus(
            WorkflowStatus.DRAFT
    );

    mockMvc.perform(
            put("/api/workflows/999999999")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
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
            status().isNotFound()
    );
}


/*
 * =========================================
 * T16.9.9
 *
 * WORKFLOW DELETE
 * NONEXISTENT RESOURCE
 * =========================================
 */

@Test
void deleteWorkflow_withNonexistentId_shouldReturnNotFound()
        throws Exception {

    mockMvc.perform(
            delete("/api/workflows/999999999")
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
 * T16.9.10
 *
 * PROTECTED USER PROFILE
 * NO AUTHENTICATION
 * =========================================
 */

@Test
void userProfile_withoutAuthentication_shouldReturnUnauthorized()
        throws Exception {

    mockMvc.perform(
            get("/api/users/me")
    )
    .andExpect(
            status().isUnauthorized()
    );
}


/*
 * =========================================
 * T16.9.11
 *
 * PUBLIC LOGIN ENDPOINT
 * NO JWT REQUIRED
 * =========================================
 */

@Test
void loginEndpoint_withoutJwt_shouldRemainAccessible()
        throws Exception {

    AuthRequest loginRequest =
            new AuthRequest();

    loginRequest.setEmail(
            "jwttest@novawavex.com"
    );

    loginRequest.setPassword(
            "Test12345"
    );

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
    );
}

}