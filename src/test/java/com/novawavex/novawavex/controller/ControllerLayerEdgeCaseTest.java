
package com.novawavex.novawavex.controller;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ControllerLayerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private String jwtToken;


    /*
     * =========================================
     * LOGIN
     * =========================================
     *
     * Obtain a fresh JWT before every test that
     * requires authentication.
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

        assertFalse(
                jwtToken.isBlank()
        );
    }


    /*
     * =========================================
     * T16.12.1
     *
     * LOGIN - EMPTY BODY
     * =========================================
     */

    @Test
    void login_withEmptyBody_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.2
     *
     * LOGIN - MALFORMED JSON
     * =========================================
     */

    @Test
    void login_withMalformedJson_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                "{\"email\":\"broken\""
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.12.3
     *
     * LOGIN - WRONG HTTP METHOD
     * =========================================
     */

    @Test
    void login_withWrongHttpMethod_shouldReturnMethodError()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/login")
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.4
     *
     * CHANGE PASSWORD - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void changePassword_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        String json =
                """
                {
                    "currentPassword": "Test12345",
                    "newPassword": "NewPassword123",
                    "confirmPassword": "NewPassword123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/change-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.5
     *
     * FORGOT PASSWORD - EMPTY BODY
     * =========================================
     */

    @Test
    void forgotPassword_withEmptyBody_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.6
     *
     * RESET PASSWORD - EMPTY BODY
     * =========================================
     */

    @Test
    void resetPassword_withEmptyBody_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.7
     *
     * USER CREATE - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void createUser_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        String json =
                """
                {
                    "fullName": "Controller Edge User",
                    "email": "controller-edge@example.com",
                    "password": "Test12345"
                }
                """;

        mockMvc.perform(
                post("/api/users")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.8
     *
     * USER PROFILE - NO AUTHENTICATION
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
     * T16.12.9
     *
     * UPDATE PROFILE NAME - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void updateProfileName_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        String json =
                """
                {
                    "fullName": "Updated Controller Name"
                }
                """;

        mockMvc.perform(
                put("/api/users/me/name")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.10
     *
     * GET USERS - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void getAllUsers_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/users")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.11
     *
     * GET USER BY INVALID ID
     * =========================================
     */

    @Test
    void getUserByInvalidId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                get("/api/users/not-a-number")
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
     * T16.12.12
     *
     * WORKFLOW CREATE - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void createWorkflow_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "Controller Edge Workflow"
        );

        request.setDescription(
                "Controller edge-case test"
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
     * T16.12.13
     *
     * WORKFLOW LIST - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void getWorkflows_withoutAuthentication_shouldReturnUnauthorized()
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
     * T16.12.14
     *
     * WORKFLOW INVALID ID FORMAT
     * =========================================
     */

    @Test
    void getWorkflow_withInvalidId_shouldReturnClientError()
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
     * T16.12.15
     *
     * WORKFLOW UPDATE INVALID ID FORMAT
     * =========================================
     */

    @Test
    void updateWorkflow_withInvalidId_shouldReturnClientError()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "Invalid ID Update"
        );

        request.setDescription(
                "Controller edge-case test"
        );

        request.setStatus(
                WorkflowStatus.DRAFT
        );

        mockMvc.perform(
                put("/api/workflows/not-a-number")
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
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.16
     *
     * WORKFLOW DELETE INVALID ID FORMAT
     * =========================================
     */

    @Test
    void deleteWorkflow_withInvalidId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                delete("/api/workflows/not-a-number")
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
     * T16.12.17
     *
     * EXECUTION CREATE - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void createExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/executions/workflow/1")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.18
     *
     * EXECUTION GET - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void getExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/executions/1")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.19
     *
     * EXECUTION LIST - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void getExecutions_withoutAuthentication_shouldReturnUnauthorized()
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
     * T16.12.20
     *
     * EXECUTION INVALID ID FORMAT
     * =========================================
     */

    @Test
    void getExecution_withInvalidId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                get("/api/executions/not-a-number")
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
     * T16.12.21
     *
     * EXECUTION START - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void startExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/executions/1/start")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.22
     *
     * EXECUTION COMPLETE - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void completeExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/executions/1/complete")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.23
     *
     * EXECUTION FAIL - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void failExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/executions/1/fail")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.24
     *
     * EXECUTION CANCEL - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void cancelExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/executions/1/cancel")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.25
     *
     * EXECUTION RETRY - NO AUTHENTICATION
     * =========================================
     */

    @Test
    void retryExecution_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/executions/1/retry")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T16.12.26
     *
     * NOTIFICATION - MISSING USER ID
     * =========================================
     *
     * Notification endpoints are protected by
     * SecurityConfig through:
     *
     * .anyRequest().authenticated()
     *
     * Therefore a valid JWT is required before
     * Spring can reach the controller and validate
     * the missing userId parameter.
     */

    @Test
    void getNotifications_withoutUserId_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                get("/api/notifications")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.12.27
     *
     * UNREAD NOTIFICATIONS - MISSING USER ID
     * =========================================
     */

    @Test
    void getUnreadNotifications_withoutUserId_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                get("/api/notifications/unread")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.12.28
     *
     * UNREAD COUNT - MISSING USER ID
     * =========================================
     */

    @Test
    void getUnreadCount_withoutUserId_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                get("/api/notifications/unread-count")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.12.29
     *
     * NOTIFICATION READ - INVALID ID FORMAT
     * =========================================
     */

    @Test
    void markNotificationRead_withInvalidId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                put("/api/notifications/not-a-number/read")
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
     * T16.12.30
     *
     * NOTIFICATION READ ALL - MISSING USER ID
     * =========================================
     */

    @Test
    void markAllNotificationsRead_withoutUserId_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                put("/api/notifications/read-all")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.12.31
     *
     * NOTIFICATION DELETE - INVALID ID FORMAT
     * =========================================
     */

    @Test
    void deleteNotification_withInvalidId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                delete("/api/notifications/not-a-number")
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
     * T16.12.32
     *
     * NOTIFICATION - INVALID USER ID FORMAT
     * =========================================
     */

    @Test
    void getNotifications_withInvalidUserId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                get("/api/notifications")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .param(
                                "userId",
                                "not-a-number"
                        )
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.33
     *
     * NOTIFICATION UNREAD - INVALID USER ID
     * =========================================
     */

    @Test
    void getUnreadNotifications_withInvalidUserId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                get("/api/notifications/unread")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .param(
                                "userId",
                                "not-a-number"
                        )
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.34
     *
     * NOTIFICATION COUNT - INVALID USER ID
     * =========================================
     */

    @Test
    void getUnreadCount_withInvalidUserId_shouldReturnClientError()
            throws Exception {

        mockMvc.perform(
                get("/api/notifications/unread-count")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .param(
                                "userId",
                                "not-a-number"
                        )
        )
        .andExpect(
                status().is4xxClientError()
        );
    }


    /*
     * =========================================
     * T16.12.35
     *
     * UNKNOWN CONTROLLER ENDPOINT
     * =========================================
     */

    @Test
    void unknownControllerEndpoint_shouldReturnNotFound()
            throws Exception {

        mockMvc.perform(
                get("/api/controller-edge-case-does-not-exist")
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
     * T16.12.36
     *
     * WORKFLOW ENDPOINT WRONG HTTP METHOD
     * =========================================
     */

    @Test
    void workflowEndpoint_withWrongHttpMethod_shouldReturnMethodError()
            throws Exception {

        mockMvc.perform(
                patchRequest(
                        "/api/workflows"
                )
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
     * HELPER
     * =========================================
     *
     * Spring MockMvc's standard request builders
     * do not expose PATCH through the imported
     * static methods above.
     */

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    patchRequest(String url) {

        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch(url);
    }
}
