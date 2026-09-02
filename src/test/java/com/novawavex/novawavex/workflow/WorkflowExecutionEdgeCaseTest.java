
package com.novawavex.novawavex.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.WorkflowRequest;

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
class WorkflowExecutionEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * NovaWavex does not expose ObjectMapper
     * as a Spring bean in this test context.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private String jwtToken;

    private Long workflowId;


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
     * CREATE TEST WORKFLOW
     * =========================================
     */

    private Long createWorkflow()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "T16.5 Execution Edge Test Workflow"
        );

        request.setDescription(
                "Workflow used for execution edge-case testing."
        );

        request.setStatus(
                WorkflowStatus.DRAFT
        );

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
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode workflow =
                objectMapper.readTree(response);

        assertNotNull(
                workflow.get("id")
        );

        return workflow
                .get("id")
                .asLong();
    }


    /*
     * =========================================
     * CREATE TEST EXECUTION
     * =========================================
     */

    private Long createExecution()
            throws Exception {

        workflowId =
                createWorkflow();

        String response =
                mockMvc.perform(
                        post(
                                "/api/executions/workflow/"
                                        + workflowId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode execution =
                objectMapper.readTree(response);

        assertNotNull(
                execution.get("id")
        );

        assertEquals(
                "QUEUED",
                execution.get("status").asText()
        );

        return execution
                .get("id")
                .asLong();
    }


    /*
     * =========================================
     * T16.5.1
     *
     * CREATE EXECUTION
     * =========================================
     */

    @Test
    void createExecution_shouldStartAsQueued()
            throws Exception {

        Long executionId =
                createExecution();

        assertNotNull(
                executionId
        );
    }


    /*
     * =========================================
     * T16.5.2
     *
     * GET NONEXISTENT EXECUTION
     *
     * Current application behavior:
     * Resource-not-found exception is returned
     * as HTTP 500.
     *
     * Main files are intentionally unchanged.
     * =========================================
     */

    @Test
    void getExecution_withNonexistentId_shouldReturnCurrentErrorStatus()
            throws Exception {

        mockMvc.perform(
                get("/api/executions/999999999")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isInternalServerError()
        );
    }


    /*
     * =========================================
     * T16.5.3
     *
     * CREATE EXECUTION FOR NONEXISTENT WORKFLOW
     *
     * Current application behavior:
     * Workflow-not-found exception is returned
     * as HTTP 500.
     *
     * Main files are intentionally unchanged.
     * =========================================
     */

    @Test
    void createExecution_withNonexistentWorkflow_shouldReturnCurrentErrorStatus()
            throws Exception {

        mockMvc.perform(
                post(
                        "/api/executions/workflow/999999999"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isInternalServerError()
        );
    }


    /*
     * =========================================
     * T16.5.4
     *
     * GET CURRENT USER EXECUTIONS
     * =========================================
     */

    @Test
    void getMyExecutions_shouldReturnSuccess()
            throws Exception {

        createExecution();

        String response =
                mockMvc.perform(
                        get("/api/executions")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode executions =
                objectMapper.readTree(response);

        assertTrue(
                executions.isArray()
        );
    }


    /*
     * =========================================
     * T16.5.5
     *
     * START QUEUED EXECUTION
     * =========================================
     */

    @Test
    void startExecution_fromQueued_shouldBecomeRunning()
            throws Exception {

        Long executionId =
                createExecution();

        String response =
                mockMvc.perform(
                        post(
                                "/api/executions/"
                                        + executionId
                                        + "/start"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode execution =
                objectMapper.readTree(response);

        assertEquals(
                executionId.longValue(),
                execution.get("id").asLong()
        );

        assertEquals(
                "RUNNING",
                execution.get("status").asText()
        );

        assertNotNull(
                execution.get("startedAt")
        );
    }


    /*
     * =========================================
     * T16.5.6
     *
     * COMPLETE EXECUTION
     * =========================================
     */

    @Test
    void completeExecution_fromRunning_shouldBecomeCompleted()
            throws Exception {

        Long executionId =
                createExecution();

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/start"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );

        String response =
                mockMvc.perform(
                        post(
                                "/api/executions/"
                                        + executionId
                                        + "/complete"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode execution =
                objectMapper.readTree(response);

        assertEquals(
                "COMPLETED",
                execution.get("status").asText()
        );

        assertNotNull(
                execution.get("completedAt")
        );
    }


    /*
     * =========================================
     * T16.5.7
     *
     * FAIL EXECUTION
     * =========================================
     */

    @Test
    void failExecution_fromRunning_shouldBecomeFailed()
            throws Exception {

        Long executionId =
                createExecution();

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/start"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );

        String response =
                mockMvc.perform(
                        post(
                                "/api/executions/"
                                        + executionId
                                        + "/fail"
                        )
                                .param(
                                        "errorMessage",
                                        "T16.5 test failure"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode execution =
                objectMapper.readTree(response);

        assertEquals(
                "FAILED",
                execution.get("status").asText()
        );

        assertEquals(
                "T16.5 test failure",
                execution.get("errorMessage").asText()
        );

        assertNotNull(
                execution.get("completedAt")
        );
    }


    /*
     * =========================================
     * T16.5.8
     *
     * CANCEL EXECUTION
     * =========================================
     */

    @Test
    void cancelExecution_fromQueued_shouldBecomeCancelled()
            throws Exception {

        Long executionId =
                createExecution();

        String response =
                mockMvc.perform(
                        post(
                                "/api/executions/"
                                        + executionId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode execution =
                objectMapper.readTree(response);

        assertEquals(
                "CANCELLED",
                execution.get("status").asText()
        );

        assertNotNull(
                execution.get("completedAt")
        );
    }


    /*
     * =========================================
     * T16.5.9
     *
     * RETRY FAILED EXECUTION
     * =========================================
     */

    @Test
    void retryExecution_afterFailure_shouldCreateQueuedExecution()
            throws Exception {

        Long executionId =
                createExecution();

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/start"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/fail"
                )
                        .param(
                                "errorMessage",
                                "Retry test failure"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );

        String response =
                mockMvc.perform(
                        post(
                                "/api/executions/"
                                        + executionId
                                        + "/retry"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode retry =
                objectMapper.readTree(response);

        assertNotNull(
                retry.get("id")
        );

        assertEquals(
                "QUEUED",
                retry.get("status").asText()
        );

        assertTrue(
                retry.get("id").asLong()
                        != executionId
        );
    }


    /*
     * =========================================
     * T16.5.10
     *
     * RETRY NON-FAILED EXECUTION
     *
     * Current application behavior:
     * Invalid state transition is returned
     * as HTTP 500.
     *
     * Main files are intentionally unchanged.
     * =========================================
     */

    @Test
    void retryExecution_whenQueued_shouldReturnCurrentErrorStatus()
            throws Exception {

        Long executionId =
                createExecution();

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/retry"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isInternalServerError()
        );
    }


    /*
     * =========================================
     * T16.5.11
     *
     * COMPLETE QUEUED EXECUTION
     *
     * Current application behavior:
     * Invalid state transition is returned
     * as HTTP 500.
     *
     * Main files are intentionally unchanged.
     * =========================================
     */

    @Test
    void completeExecution_fromQueued_shouldReturnCurrentErrorStatus()
            throws Exception {

        Long executionId =
                createExecution();

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/complete"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isInternalServerError()
        );
    }


    /*
     * =========================================
     * T16.5.12
     *
     * START COMPLETED EXECUTION
     *
     * Current application behavior:
     * Invalid state transition is returned
     * as HTTP 500.
     *
     * Main files are intentionally unchanged.
     * =========================================
     */

    @Test
    void startExecution_afterCompletion_shouldReturnCurrentErrorStatus()
            throws Exception {

        Long executionId =
                createExecution();

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/start"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/complete"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/start"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isInternalServerError()
        );
    }
}
