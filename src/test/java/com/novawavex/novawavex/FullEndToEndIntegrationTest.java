package com.novawavex.novawavex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.ProfileNameRequest;
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

/*

* =========================================
* T15.9
*
* FULL END-TO-END APPLICATION INTEGRATION
*
* LOGIN
* ↓
* GET PROFILE
* ↓
* UPDATE PROFILE
* ↓
* CREATE WORKFLOW
* ↓
* GET WORKFLOW
* ↓
* UPDATE WORKFLOW
* ↓
* CREATE EXECUTION
* ↓
* START EXECUTION
* ↓
* COMPLETE EXECUTION
* ↓
* EXECUTION HISTORY
* ↓
* NOTIFICATIONS
* ↓
* MARK NOTIFICATIONS READ
* ↓
* CREATE DELETE-ONLY WORKFLOW
* ↓
* DELETE WORKFLOW
* ↓
* VERIFY DELETED
* ↓
* LOGIN AGAIN
* ↓
* FINAL PROFILE
*
* IMPORTANT:
* No main application files are modified.
* =========================================
  */

@SpringBootTest
@AutoConfigureMockMvc
class FullEndToEndIntegrationTest {


@Autowired
private MockMvc mockMvc;

/*
 * Use local ObjectMapper.
 *
 * This avoids requiring ObjectMapper
 * to be a Spring bean.
 */
private final ObjectMapper objectMapper =
        new ObjectMapper();

private String jwtToken;

private final String email =
        "jwttest@novawavex.com";

private final String password =
        "Test12345";


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
            email
    );

    loginRequest.setPassword(
            password
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
            objectMapper.readTree(
                    response
            );

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
 * FULL APPLICATION LIFECYCLE
 * =========================================
 */

@Test
void fullApplicationLifecycle_shouldSucceed()
        throws Exception {


    /*
     * =========================================
     * 1. GET CURRENT PROFILE
     * =========================================
     */

    String profileResponse =
            mockMvc.perform(
                    get("/api/users/me")
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

    JsonNode profile =
            objectMapper.readTree(
                    profileResponse
            );

    assertNotNull(
            profile.get("id")
    );

    assertEquals(
            email,
            profile
                    .get("email")
                    .asText()
    );

    assertNotNull(
            profile.get("fullName")
    );

    assertNotNull(
            profile.get("role")
    );

    Long userId =
            profile
                    .get("id")
                    .asLong();


    /*
     * =========================================
     * 2. UPDATE PROFILE NAME
     * =========================================
     */

    ProfileNameRequest nameRequest =
            new ProfileNameRequest(
                    "T15.9 E2E Test User"
            );

    String updatedProfileResponse =
            mockMvc.perform(
                    put("/api/users/me/name")
                            .header(
                                    "Authorization",
                                    "Bearer " + jwtToken
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .content(
                                    objectMapper.writeValueAsString(
                                            nameRequest
                                    )
                            )
            )
            .andExpect(
                    status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode updatedProfile =
            objectMapper.readTree(
                    updatedProfileResponse
            );

    assertEquals(
            "T15.9 E2E Test User",
            updatedProfile
                    .get("fullName")
                    .asText()
    );

    assertEquals(
            email,
            updatedProfile
                    .get("email")
                    .asText()
    );


    /*
     * =========================================
     * 3. CREATE WORKFLOW
     * =========================================
     */

    WorkflowRequest workflowRequest =
            new WorkflowRequest();

    workflowRequest.setName(
            "T15.9 E2E Workflow"
    );

    workflowRequest.setDescription(
            "T15.9 full end-to-end workflow."
    );

    workflowRequest.setStatus(
            WorkflowStatus.DRAFT
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

    JsonNode workflow =
            objectMapper.readTree(
                    workflowResponse
            );

    assertNotNull(
            workflow.get("id")
    );

    Long workflowId =
            workflow
                    .get("id")
                    .asLong();

    assertEquals(
            "T15.9 E2E Workflow",
            workflow
                    .get("name")
                    .asText()
    );

    assertEquals(
            "DRAFT",
            workflow
                    .get("status")
                    .asText()
    );


    /*
     * =========================================
     * 4. GET WORKFLOW
     * =========================================
     */

    String fetchedWorkflowResponse =
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

    JsonNode fetchedWorkflow =
            objectMapper.readTree(
                    fetchedWorkflowResponse
            );

    assertEquals(
            workflowId,
            fetchedWorkflow
                    .get("id")
                    .asLong()
    );

    assertEquals(
            "T15.9 E2E Workflow",
            fetchedWorkflow
                    .get("name")
                    .asText()
    );


    /*
     * =========================================
     * 5. UPDATE WORKFLOW
     * =========================================
     */

    WorkflowRequest updateRequest =
            new WorkflowRequest();

    updateRequest.setName(
            "T15.9 Updated Workflow"
    );

    updateRequest.setDescription(
            "T15.9 updated workflow."
    );

    updateRequest.setStatus(
            WorkflowStatus.ACTIVE
    );

    String updatedWorkflowResponse =
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
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode updatedWorkflow =
            objectMapper.readTree(
                    updatedWorkflowResponse
            );

    assertEquals(
            workflowId,
            updatedWorkflow
                    .get("id")
                    .asLong()
    );

    assertEquals(
            "T15.9 Updated Workflow",
            updatedWorkflow
                    .get("name")
                    .asText()
    );

    assertEquals(
            "ACTIVE",
            updatedWorkflow
                    .get("status")
                    .asText()
    );


    /*
     * =========================================
     * 6. CREATE WORKFLOW EXECUTION
     * =========================================
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

    JsonNode execution =
            objectMapper.readTree(
                    executionResponse
            );

    assertNotNull(
            execution.get("id")
    );

    Long executionId =
            execution
                    .get("id")
                    .asLong();

    assertEquals(
            "QUEUED",
            execution
                    .get("status")
                    .asText()
    );


    /*
     * =========================================
     * 7. GET EXECUTION
     * =========================================
     */

    String fetchedExecutionResponse =
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

    JsonNode fetchedExecution =
            objectMapper.readTree(
                    fetchedExecutionResponse
            );

    assertEquals(
            executionId,
            fetchedExecution
                    .get("id")
                    .asLong()
    );

    assertEquals(
            "QUEUED",
            fetchedExecution
                    .get("status")
                    .asText()
    );


    /*
     * =========================================
     * 8. START EXECUTION
     * =========================================
     */

    String startedExecutionResponse =
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
                    startedExecutionResponse
            );

    assertEquals(
            "RUNNING",
            startedExecution
                    .get("status")
                    .asText()
    );

    assertNotNull(
            startedExecution.get("startedAt")
    );


    /*
     * =========================================
     * 9. COMPLETE EXECUTION
     * =========================================
     */

    String completedExecutionResponse =
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

    JsonNode completedExecution =
            objectMapper.readTree(
                    completedExecutionResponse
            );

    assertEquals(
            "COMPLETED",
            completedExecution
                    .get("status")
                    .asText()
    );

    assertNotNull(
            completedExecution.get("completedAt")
    );


    /*
     * =========================================
     * 10. EXECUTION HISTORY
     * =========================================
     */

    String historyResponse =
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

    JsonNode history =
            objectMapper.readTree(
                    historyResponse
            );

    assertTrue(
            history.isArray()
    );

    assertTrue(
            history.size() >= 1
    );


    /*
     * =========================================
     * 11. GET NOTIFICATIONS
     * =========================================
     */

    String notificationsResponse =
            mockMvc.perform(
                    get("/api/notifications")
                            .param(
                                    "userId",
                                    String.valueOf(userId)
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

    JsonNode notifications =
            objectMapper.readTree(
                    notificationsResponse
            );

    assertTrue(
            notifications.isArray()
    );

    assertTrue(
            notifications.size() >= 1
    );


    /*
     * =========================================
     * 12. GET UNREAD COUNT
     * =========================================
     */

    String unreadCountResponse =
            mockMvc.perform(
                    get(
                            "/api/notifications/unread-count"
                    )
                            .param(
                                    "userId",
                                    String.valueOf(userId)
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

    JsonNode unreadCount =
            objectMapper.readTree(
                    unreadCountResponse
            );

    assertTrue(
            unreadCount.asLong() >= 1
    );


    /*
     * =========================================
     * 13. MARK EACH NOTIFICATION AS READ
     * =========================================
     */

    for (JsonNode notification : notifications) {

        JsonNode notificationIdNode =
                notification.get("id");

        assertNotNull(
                notificationIdNode
        );

        Long notificationId =
                notificationIdNode.asLong();

        mockMvc.perform(
                put(
                        "/api/notifications/"
                                + notificationId
                                + "/read"
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isOk()
        );
    }


    /*
     * =========================================
     * 14. VERIFY UNREAD COUNT = 0
     * =========================================
     */

    String finalUnreadCountResponse =
            mockMvc.perform(
                    get(
                            "/api/notifications/unread-count"
                    )
                            .param(
                                    "userId",
                                    String.valueOf(userId)
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

    JsonNode finalUnreadCount =
            objectMapper.readTree(
                    finalUnreadCountResponse
            );

    assertEquals(
            0,
            finalUnreadCount.asLong()
    );


    /*
     * =========================================
     * 15. CREATE DELETE-ONLY WORKFLOW
     *
     * IMPORTANT:
     *
     * The first workflow already has an
     * execution associated with it.
     *
     * Therefore we do NOT delete that
     * workflow.
     *
     * Instead, create a second workflow
     * without executions specifically to
     * test the existing delete endpoint.
     * =========================================
     */

    WorkflowRequest deleteWorkflowRequest =
            new WorkflowRequest();

    deleteWorkflowRequest.setName(
            "T15.9 Delete Test Workflow"
    );

    deleteWorkflowRequest.setDescription(
            "T15.9 workflow used to verify deletion."
    );

    deleteWorkflowRequest.setStatus(
            WorkflowStatus.DRAFT
    );

    String deleteWorkflowResponse =
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
                                            deleteWorkflowRequest
                                    )
                            )
            )
            .andExpect(
                    status().isCreated()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode deleteWorkflow =
            objectMapper.readTree(
                    deleteWorkflowResponse
            );

    assertNotNull(
            deleteWorkflow.get("id")
    );

    Long deleteWorkflowId =
            deleteWorkflow
                    .get("id")
                    .asLong();

    assertEquals(
            "T15.9 Delete Test Workflow",
            deleteWorkflow
                    .get("name")
                    .asText()
    );


    /*
     * =========================================
     * 16. DELETE WORKFLOW
     * =========================================
     */

    mockMvc.perform(
            delete(
                    "/api/workflows/"
                            + deleteWorkflowId
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
     * =========================================
     * 17. VERIFY WORKFLOW DELETED
     * =========================================
     */

    mockMvc.perform(
            get(
                    "/api/workflows/"
                            + deleteWorkflowId
            )
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isNotFound()
    );


    /*
     * =========================================
     * 18. LOGIN AGAIN
     * =========================================
     */

    AuthRequest secondLoginRequest =
            new AuthRequest();

    secondLoginRequest.setEmail(
            email
    );

    secondLoginRequest.setPassword(
            password
    );

    String secondLoginResponse =
            mockMvc.perform(
                    post("/api/auth/login")
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .content(
                                    objectMapper.writeValueAsString(
                                            secondLoginRequest
                                    )
                            )
            )
            .andExpect(
                    status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode secondLogin =
            objectMapper.readTree(
                    secondLoginResponse
            );

    assertNotNull(
            secondLogin.get("token")
    );

    String secondJwtToken =
            secondLogin
                    .get("token")
                    .asText();

    assertTrue(
            !secondJwtToken.isBlank()
    );


    /*
     * =========================================
     * 19. FINAL PROFILE
     * =========================================
     */

    String finalProfileResponse =
            mockMvc.perform(
                    get("/api/users/me")
                            .header(
                                    "Authorization",
                                    "Bearer " + secondJwtToken
                            )
            )
            .andExpect(
                    status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode finalProfile =
            objectMapper.readTree(
                    finalProfileResponse
            );

    assertEquals(
            email,
            finalProfile
                    .get("email")
                    .asText()
    );

    assertEquals(
            "T15.9 E2E Test User",
            finalProfile
                    .get("fullName")
                    .asText()
    );

    assertNotNull(
            finalProfile.get("id")
    );

    assertNotNull(
            finalProfile.get("role")
    );
}

}
