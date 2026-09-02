
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
class WorkflowExecutionLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * NovaWavex does not expose ObjectMapper
     * as a Spring bean, so create it directly.
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

        assertNotNull(jwtToken);

        assertTrue(
                !jwtToken.isBlank()
        );
    }


    /*
     * =========================================
     * T15.3
     *
     * LOGIN
     *   ↓
     * CREATE WORKFLOW
     *   ↓
     * CREATE EXECUTION
     *   ↓
     * START EXECUTION
     *   ↓
     * CHECK EXECUTION
     * =========================================
     */

    @Test
    void workflowExecutionLifecycle_shouldSucceed()
            throws Exception {

        /*
         * =====================================
         * 1. CREATE WORKFLOW
         * =====================================
         */

        WorkflowRequest workflowRequest =
                new WorkflowRequest();

        workflowRequest.setName(
                "T15.3 Execution Workflow"
        );

        workflowRequest.setDescription(
                "Workflow execution integration test"
        );

        workflowRequest.setStatus(
                WorkflowStatus.ACTIVE
        );

        String workflowResponse =
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
                                                workflowRequest
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode workflowJson =
                objectMapper.readTree(
                        workflowResponse
                );

        assertNotNull(
                workflowJson.get("id")
        );

        long workflowId =
                workflowJson
                        .get("id")
                        .asLong();

        assertTrue(
                workflowId > 0
        );


        /*
         * =====================================
         * 2. CREATE EXECUTION
         * =====================================
         */

        String executionResponse =
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

        JsonNode executionJson =
                objectMapper.readTree(
                        executionResponse
                );

        assertNotNull(
                executionJson.get("id")
        );

        long executionId =
                executionJson
                        .get("id")
                        .asLong();

        assertTrue(
                executionId > 0
        );

        /*
         * New executions must start QUEUED.
         */
        assertEquals(
                "QUEUED",
                executionJson
                        .get("status")
                        .asText()
        );


        /*
         * =====================================
         * 3. GET EXECUTION
         * =====================================
         */

        String queuedExecutionResponse =
                mockMvc.perform(
                        get(
                                "/api/executions/"
                                        + executionId
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

        JsonNode queuedExecution =
                objectMapper.readTree(
                        queuedExecutionResponse
                );

        assertEquals(
                executionId,
                queuedExecution
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "QUEUED",
                queuedExecution
                        .get("status")
                        .asText()
        );


        /*
         * =====================================
         * 4. START EXECUTION
         * =====================================
         */

        String startedResponse =
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

        JsonNode startedExecution =
                objectMapper.readTree(
                        startedResponse
                );

        /*
         * Execution must now be RUNNING.
         */
        assertEquals(
                executionId,
                startedExecution
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "RUNNING",
                startedExecution
                        .get("status")
                        .asText()
        );

        /*
         * Starting an execution must
         * populate startedAt.
         */
        assertNotNull(
                startedExecution.get("startedAt")
        );


        /*
         * =====================================
         * 5. GET EXECUTION AGAIN
         * =====================================
         */

        String finalResponse =
                mockMvc.perform(
                        get(
                                "/api/executions/"
                                        + executionId
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

        JsonNode finalExecution =
                objectMapper.readTree(
                        finalResponse
                );

        /*
         * Verify execution ID.
         */
        assertEquals(
                executionId,
                finalExecution
                        .get("id")
                        .asLong()
        );

        /*
         * Verify execution status.
         */
        assertEquals(
                "RUNNING",
                finalExecution
                        .get("status")
                        .asText()
        );

        /*
         * Verify workflow relationship.
         */
        assertNotNull(
                finalExecution.get("workflow")
        );

        assertEquals(
                workflowId,
                finalExecution
                        .get("workflow")
                        .get("id")
                        .asLong()
        );

        /*
         * Verify createdBy.
         */
        assertEquals(
                "jwttest@novawavex.com",
                finalExecution
                        .get("createdBy")
                        .asText()
        );

        /*
         * Verify startedAt persisted.
         */
        assertNotNull(
                finalExecution.get("startedAt")
        );
    }
}
