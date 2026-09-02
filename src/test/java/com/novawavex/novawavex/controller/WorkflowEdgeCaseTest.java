
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class WorkflowEdgeCaseTest {

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
     * Workflow created during the tests.
     */
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
     * HELPER - CREATE VALID WORKFLOW
     * =========================================
     */

    private Long createWorkflow(
            String name,
            String description,
            WorkflowStatus status)
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(name);
        request.setDescription(description);
        request.setStatus(status);

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
     * T16.4.1
     * CREATE WORKFLOW - VALID REQUEST
     * =========================================
     */

    @Test
    void createWorkflow_withValidData_shouldSucceed()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "T16.4 Valid Workflow"
        );

        request.setDescription(
                "Valid workflow edge-case test."
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

        assertEquals(
                "T16.4 Valid Workflow",
                workflow.get("name").asText()
        );

        assertEquals(
                "DRAFT",
                workflow.get("status").asText()
        );
    }


    /*
     * =========================================
     * T16.4.2
     * CREATE WORKFLOW - EMPTY NAME
     * =========================================
     */

    @Test
    void createWorkflow_withEmptyName_shouldBeRejected()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("");
        request.setDescription(
                "Invalid workflow."
        );
        request.setStatus(
                WorkflowStatus.DRAFT
        );

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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.3
     * CREATE WORKFLOW - WHITESPACE NAME
     * =========================================
     */

    @Test
    void createWorkflow_withWhitespaceName_shouldBeRejected()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("   ");
        request.setDescription(
                "Invalid workflow."
        );
        request.setStatus(
                WorkflowStatus.DRAFT
        );

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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.4
     * CREATE WORKFLOW - NULL NAME
     * =========================================
     */

    @Test
    void createWorkflow_withNullName_shouldBeRejected()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(null);
        request.setDescription(
                "Invalid workflow."
        );
        request.setStatus(
                WorkflowStatus.DRAFT
        );

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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.5
     * CREATE WORKFLOW - NAME > 150 CHARACTERS
     * =========================================
     */

    @Test
    void createWorkflow_withNameOver150Characters_shouldBeRejected()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "A".repeat(151)
        );

        request.setDescription(
                "Invalid workflow."
        );

        request.setStatus(
                WorkflowStatus.DRAFT
        );

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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.6
     * CREATE WORKFLOW - NULL STATUS
     * =========================================
     */

    @Test
    void createWorkflow_withNullStatus_shouldBeRejected()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "T16.4 Null Status Workflow"
        );

        request.setDescription(
                "Invalid workflow."
        );

        request.setStatus(null);

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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.7
     * CREATE WORKFLOW - DESCRIPTION > 1000
     * =========================================
     */

    @Test
    void createWorkflow_withDescriptionOver1000Characters_shouldBeRejected()
            throws Exception {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "T16.4 Long Description Workflow"
        );

        request.setDescription(
                "A".repeat(1001)
        );

        request.setStatus(
                WorkflowStatus.DRAFT
        );

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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.8
     * GET NONEXISTENT WORKFLOW
     * =========================================
     */

    @Test
    void getWorkflow_withNonexistentId_shouldReturnNotFound()
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
     * T16.4.9
     * UPDATE WORKFLOW - INVALID NAME
     * =========================================
     */

    @Test
    void updateWorkflow_withEmptyName_shouldBeRejected()
            throws Exception {

        workflowId =
                createWorkflow(
                        "T16.4 Update Test",
                        "Update edge case.",
                        WorkflowStatus.DRAFT
                );

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("");
        request.setDescription(
                "Updated description."
        );
        request.setStatus(
                WorkflowStatus.ACTIVE
        );

        mockMvc.perform(
                put("/api/workflows/" + workflowId)
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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.10
     * UPDATE WORKFLOW - NULL STATUS
     * =========================================
     */

    @Test
    void updateWorkflow_withNullStatus_shouldBeRejected()
            throws Exception {

        workflowId =
                createWorkflow(
                        "T16.4 Null Status Update",
                        "Update edge case.",
                        WorkflowStatus.DRAFT
                );

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "T16.4 Updated Workflow"
        );

        request.setDescription(
                "Updated description."
        );

        request.setStatus(null);

        mockMvc.perform(
                put("/api/workflows/" + workflowId)
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
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.4.11
     * DELETE NONEXISTENT WORKFLOW
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
     * T16.4.12
     * WORKFLOW PERSISTENCE AFTER UPDATE
     * =========================================
     */

    @Test
    void updatedWorkflow_shouldPersist()
            throws Exception {

        workflowId =
                createWorkflow(
                        "T16.4 Persistence Test",
                        "Original description.",
                        WorkflowStatus.DRAFT
                );

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "T16.4 Updated Persistence Test"
        );

        request.setDescription(
                "Updated description."
        );

        request.setStatus(
                WorkflowStatus.ACTIVE
        );

        mockMvc.perform(
                put("/api/workflows/" + workflowId)
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
                status().isOk()
        );

        String response =
                mockMvc.perform(
                        get("/api/workflows/" + workflowId)
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

        JsonNode workflow =
                objectMapper.readTree(response);

        assertEquals(
                workflowId.longValue(),
                workflow.get("id").asLong()
        );

        assertEquals(
                "T16.4 Updated Persistence Test",
                workflow.get("name").asText()
        );

        assertEquals(
                "Updated description.",
                workflow.get("description").asText()
        );

        assertEquals(
                "ACTIVE",
                workflow.get("status").asText()
        );
    }
}
