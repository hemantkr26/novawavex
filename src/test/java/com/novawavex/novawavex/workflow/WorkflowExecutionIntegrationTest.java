package com.novawavex.novawavex.workflow;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class WorkflowExecutionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * Use a local ObjectMapper.
     *
     * This avoids requiring ObjectMapper
     * to be registered as a Spring bean.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private String jwtToken;

    private Long workflowId;


    /*
     * =========================================
     * LOGIN + CREATE WORKFLOW
     * =========================================
     */

    @BeforeEach
    void setUp() throws Exception {

        /*
         * =====================================
         * LOGIN
         * =====================================
         */

        AuthRequest loginRequest =
                new AuthRequest();

        loginRequest.setEmail(
                "jwttest@novawavex.com"
        );

        loginRequest.setPassword(
                "Test12345"
        );

        String loginResponse =
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

        JsonNode loginJson =
                objectMapper.readTree(
                        loginResponse
                );

        assertNotNull(
                loginJson.get("token")
        );

        jwtToken =
                loginJson
                        .get("token")
                        .asText();

        assertTrue(
                !jwtToken.isBlank()
        );


        /*
         * =====================================
         * CREATE TEST WORKFLOW
         * =====================================
         */

        String workflowRequest =
                """
                {
                    "name": "T15.7 Execution Integration Workflow",
                    "description": "Workflow created for T15.7 integration testing",
                    "status": "ACTIVE"
                }
                """;

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
                                        workflowRequest
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

        workflowId =
                workflowJson
                        .get("id")
                        .asLong();

        assertTrue(
                workflowId > 0
        );
    }


    /*
     * =========================================
     * T15.7.1
     *
     * CREATE EXECUTION
     * ↓
     * GET EXECUTION
     * =========================================
     */

    @Test
    void createAndGetExecution_shouldSucceed()
            throws Exception {

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

        assertEquals(
                "QUEUED",
                executionJson
                        .get("status")
                        .asText()
        );

        assertNotNull(
                executionJson.get("createdBy")
        );

        Long executionId =
                executionJson
                        .get("id")
                        .asLong();


        /*
         * =====================================
         * GET EXECUTION
         * =====================================
         */

        String getResponse =
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

        JsonNode getJson =
                objectMapper.readTree(
                        getResponse
                );

        assertEquals(
                executionId,
                getJson
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "QUEUED",
                getJson
                        .get("status")
                        .asText()
        );
    }


    /*
     * =========================================
     * T15.7.2
     *
     * QUEUED → RUNNING → COMPLETED
     * =========================================
     */

    @Test
    void executionLifecycle_queuedToRunningToCompleted()
            throws Exception {

        Long executionId =
                createExecution();


        /*
         * =====================================
         * START
         * =====================================
         */

        String startResponse =
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

        JsonNode startJson =
                objectMapper.readTree(
                        startResponse
                );

        assertEquals(
                executionId,
                startJson
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "RUNNING",
                startJson
                        .get("status")
                        .asText()
        );

        assertNotNull(
                startJson.get("startedAt")
        );


        /*
         * =====================================
         * COMPLETE
         * =====================================
         */

        String completeResponse =
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

        JsonNode completeJson =
                objectMapper.readTree(
                        completeResponse
                );

        assertEquals(
                executionId,
                completeJson
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "COMPLETED",
                completeJson
                        .get("status")
                        .asText()
        );

        assertNotNull(
                completeJson.get("completedAt")
        );
    }


    /*
     * =========================================
     * T15.7.3
     *
     * QUEUED → RUNNING → FAILED
     * =========================================
     */

    @Test
    void executionLifecycle_queuedToRunningToFailed()
            throws Exception {

        Long executionId =
                createExecution();


        /*
         * =====================================
         * START
         * =====================================
         */

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


        /*
         * =====================================
         * FAIL
         * =====================================
         */

        String failResponse =
                mockMvc.perform(
                        post(
                                "/api/executions/"
                                        + executionId
                                        + "/fail"
                        )
                                .param(
                                        "errorMessage",
                                        "T15.7 test failure"
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

        JsonNode failJson =
                objectMapper.readTree(
                        failResponse
                );

        assertEquals(
                executionId,
                failJson
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "FAILED",
                failJson
                        .get("status")
                        .asText()
        );

        assertEquals(
                "T15.7 test failure",
                failJson
                        .get("errorMessage")
                        .asText()
        );

        assertNotNull(
                failJson.get("completedAt")
        );
    }


    /*
     * =========================================
     * T15.7.4
     *
     * FAILED → RETRY
     * =========================================
     */

    @Test
    void failedExecution_shouldBeRetryable()
            throws Exception {

        Long executionId =
                createExecution();


        /*
         * =====================================
         * START
         * =====================================
         */

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


        /*
         * =====================================
         * FAIL
         * =====================================
         */

        mockMvc.perform(
                post(
                        "/api/executions/"
                                + executionId
                                + "/fail"
                )
                        .param(
                                "errorMessage",
                                "T15.7 retry test failure"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );


        /*
         * =====================================
         * RETRY
         * =====================================
         */

        String retryResponse =
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

        JsonNode retryJson =
                objectMapper.readTree(
                        retryResponse
                );

        assertNotNull(
                retryJson.get("id")
        );

        Long retryId =
                retryJson
                        .get("id")
                        .asLong();

        assertTrue(
                retryId > 0
        );

        /*
         * Retry must create a NEW execution.
         */
        assertTrue(
                !retryId.equals(executionId)
        );

        assertEquals(
                "QUEUED",
                retryJson
                        .get("status")
                        .asText()
        );

        assertEquals(
                "jwttest@novawavex.com",
                retryJson
                        .get("createdBy")
                        .asText()
        );
    }


    /*
     * =========================================
     * T15.7.5
     *
     * QUEUED → CANCELLED
     * =========================================
     */

    @Test
    void queuedExecution_shouldBeCancellable()
            throws Exception {

        Long executionId =
                createExecution();


        /*
         * =====================================
         * CANCEL
         * =====================================
         */

        String cancelResponse =
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

        JsonNode cancelJson =
                objectMapper.readTree(
                        cancelResponse
                );

        assertEquals(
                executionId,
                cancelJson
                        .get("id")
                        .asLong()
        );

        assertEquals(
                "CANCELLED",
                cancelJson
                        .get("status")
                        .asText()
        );

        assertNotNull(
                cancelJson.get("completedAt")
        );
    }


    /*
     * =========================================
     * T15.7.6
     *
     * GET CURRENT USER EXECUTION HISTORY
     * =========================================
     */

    @Test
    void getMyExecutions_shouldReturnUserExecutions()
            throws Exception {

        /*
         * Create at least one execution.
         */
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
                objectMapper.readTree(
                        response
                );

        assertTrue(
                executions.isArray()
        );

        assertTrue(
                executions.size() >= 1
        );


        /*
         * Verify that the authenticated user's
         * execution is present.
         */
        boolean found =
                false;

        for (JsonNode execution :
                executions) {

            if (execution.has("createdBy")
                    && "jwttest@novawavex.com"
                    .equals(
                            execution
                                    .get("createdBy")
                                    .asText()
                    )) {

                found = true;
                break;
            }
        }

        assertTrue(found);
    }


    /*
     * =========================================
     * HELPER
     *
     * CREATE EXECUTION
     * =========================================
     */

    private Long createExecution()
            throws Exception {

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

        JsonNode json =
                objectMapper.readTree(
                        response
                );

        assertNotNull(
                json.get("id")
        );

        assertEquals(
                "QUEUED",
                json
                        .get("status")
                        .asText()
        );

        return json
                .get("id")
                .asLong();
    }
}