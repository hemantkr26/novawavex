
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class WorkflowLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * ObjectMapper is created directly because
     * NovaWavex does not currently expose an
     * ObjectMapper Spring bean.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    /*
     * =========================================
     * TEST DATA
     * =========================================
     */

    private String jwtToken;


    /*
     * =========================================
     * LOGIN BEFORE TEST
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
     * T15.2
     *
     * LOGIN
     *   ↓
     * CREATE WORKFLOW
     *   ↓
     * GET WORKFLOW
     *   ↓
     * UPDATE WORKFLOW
     *   ↓
     * DELETE WORKFLOW
     * =========================================
     */

    @Test
    void completeWorkflowLifecycle_shouldSucceed()
            throws Exception {

        /*
         * =====================================
         * CREATE WORKFLOW
         * =====================================
         */

        WorkflowRequest createRequest =
                new WorkflowRequest();

        createRequest.setName(
                "T15.2 Integration Workflow"
        );

        createRequest.setDescription(
                "Workflow lifecycle integration test"
        );

        createRequest.setStatus(
                WorkflowStatus.DRAFT
        );

        String createResponse =
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
                                                createRequest
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdWorkflow =
                objectMapper.readTree(
                        createResponse
                );

        assertNotNull(
                createdWorkflow.get("id")
        );

        long workflowId =
                createdWorkflow
                        .get("id")
                        .asLong();

        assertTrue(
                workflowId > 0
        );


        /*
         * =====================================
         * GET WORKFLOW
         * =====================================
         */

        mockMvc.perform(
                get(
                        "/api/workflows/"
                                + workflowId
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
         * UPDATE WORKFLOW
         * =====================================
         */

        WorkflowRequest updateRequest =
                new WorkflowRequest();

        updateRequest.setName(
                "T15.2 Updated Workflow"
        );

        updateRequest.setDescription(
                "Updated workflow lifecycle test"
        );

        updateRequest.setStatus(
                WorkflowStatus.ACTIVE
        );

        mockMvc.perform(
                put(
                        "/api/workflows/"
                                + workflowId
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        updateRequest
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );


        /*
         * =====================================
         * VERIFY UPDATED WORKFLOW
         * =====================================
         */

        String updatedResponse =
                mockMvc.perform(
                        get(
                                "/api/workflows/"
                                        + workflowId
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

        JsonNode updatedWorkflow =
                objectMapper.readTree(
                        updatedResponse
                );

        assertTrue(
                "T15.2 Updated Workflow".equals(
                        updatedWorkflow
                                .get("name")
                                .asText()
                )
        );

        assertTrue(
                "ACTIVE".equals(
                        updatedWorkflow
                                .get("status")
                                .asText()
                )
        );


        /*
         * =====================================
         * DELETE WORKFLOW
         * =====================================
         */

        mockMvc.perform(
                delete(
                        "/api/workflows/"
                                + workflowId
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isNoContent()
        );


        /*
         * =====================================
         * VERIFY DELETED
         * =====================================
         */

        mockMvc.perform(
                get(
                        "/api/workflows/"
                                + workflowId
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isNotFound()
        );
    }
}
