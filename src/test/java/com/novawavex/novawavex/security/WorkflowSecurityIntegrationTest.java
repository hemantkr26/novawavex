
package com.novawavex.novawavex.security;

import com.novawavex.novawavex.dto.WorkflowRequest;
import com.novawavex.novawavex.dto.WorkflowResponse;
import com.novawavex.novawavex.exception.ResourceNotFoundException;
import com.novawavex.novawavex.service.JwtService;
import com.novawavex.novawavex.workflow.WorkflowService;
import com.novawavex.novawavex.workflow.WorkflowStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class WorkflowSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private WorkflowService workflowService;


    private String userAToken;

    private String userBToken;


    private final String USER_A =
            "userA@example.com";

    private final String USER_B =
            "userB@example.com";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        userAToken =
                jwtService.generateToken(
                        1L,
                        USER_A,
                        "User A",
                        "USER"
                );

        userBToken =
                jwtService.generateToken(
                        2L,
                        USER_B,
                        "User B",
                        "USER"
                );
    }


    // =========================================================
    // TEST 1
    // NO JWT
    // =========================================================

    @Test
    void getWorkflows_withoutJwt_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
        )
        .andExpect(
                status().isUnauthorized()
        );

        verifyNoInteractions(
                workflowService
        );
    }


    // =========================================================
    // TEST 2
    // INVALID JWT
    // =========================================================

    @Test
    void getWorkflows_withInvalidJwt_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );

        verifyNoInteractions(
                workflowService
        );
    }


    // =========================================================
    // TEST 3
    // VALID JWT
    // =========================================================

    @Test
    void getWorkflows_withValidJwt_shouldReturnOk()
            throws Exception {

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Test Workflow",
                        "Test Description",
                        WorkflowStatus.DRAFT,
                        USER_A,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(
                workflowService.getAllWorkflows(
                        USER_A
                )
        ).thenReturn(
                java.util.List.of(response)
        );


        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + userAToken
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.length()")
                        .value(1)
        )
        .andExpect(
                jsonPath("$[0].name")
                        .value("Test Workflow")
        )
        .andExpect(
                jsonPath("$[0].createdBy")
                        .value(USER_A)
        );


        verify(
                workflowService,
                times(1)
        ).getAllWorkflows(
                USER_A
        );
    }


    // =========================================================
    // TEST 4
    // JWT EMAIL SHOULD REACH SERVICE
    // =========================================================

    @Test
    void getWorkflows_shouldUseEmailFromJwt()
            throws Exception {

        when(
                workflowService.getAllWorkflows(
                        USER_A
                )
        ).thenReturn(
                java.util.List.of()
        );


        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + userAToken
                        )
        )
        .andExpect(
                status().isOk()
        );


        verify(
                workflowService,
                times(1)
        ).getAllWorkflows(
                USER_A
        );


        verify(
                workflowService,
                never()
        ).getAllWorkflows(
                USER_B
        );
    }


    // =========================================================
    // TEST 5
    // USER B IDENTITY
    // =========================================================

    @Test
    void userB_withValidJwt_shouldUseUserBEmail()
            throws Exception {

        when(
                workflowService.getAllWorkflows(
                        USER_B
                )
        ).thenReturn(
                java.util.List.of()
        );


        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + userBToken
                        )
        )
        .andExpect(
                status().isOk()
        );


        verify(
                workflowService,
                times(1)
        ).getAllWorkflows(
                USER_B
        );


        verify(
                workflowService,
                never()
        ).getAllWorkflows(
                USER_A
        );
    }


    // =========================================================
    // TEST 6
    // CREATE USING JWT
    // =========================================================

    @Test
    void createWorkflow_withValidJwt_shouldUseJwtUser()
            throws Exception {

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "JWT Workflow",
                        "Created using JWT authentication",
                        WorkflowStatus.DRAFT,
                        USER_A,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(
                workflowService.createWorkflow(
                        any(WorkflowRequest.class),
                        eq(USER_A)
                )
        ).thenReturn(
                response
        );


        String requestJson = """
                {
                    "name": "JWT Workflow",
                    "description": "Created using JWT authentication",
                    "status": "DRAFT"
                }
                """;


        mockMvc.perform(
                post("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + userAToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                requestJson
                        )
        )
        .andExpect(
                status().isCreated()
        )
        .andExpect(
                jsonPath("$.createdBy")
                        .value(USER_A)
        );


        verify(
                workflowService,
                times(1)
        ).createWorkflow(
                any(WorkflowRequest.class),
                eq(USER_A)
        );
    }


    // =========================================================
    // TEST 7
    // USER A CAN ACCESS OWN WORKFLOW
    // =========================================================

    @Test
    void userA_canAccessOwnWorkflow()
            throws Exception {

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "User A Workflow",
                        "Owned by User A",
                        WorkflowStatus.DRAFT,
                        USER_A,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(
                workflowService.getWorkflowById(
                        1L,
                        USER_A
                )
        ).thenReturn(
                response
        );


        mockMvc.perform(
                get("/api/workflows/1")
                        .header(
                                "Authorization",
                                "Bearer " + userAToken
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.id")
                        .value(1)
        )
        .andExpect(
                jsonPath("$.createdBy")
                        .value(USER_A)
        );


        verify(
                workflowService,
                times(1)
        ).getWorkflowById(
                1L,
                USER_A
        );
    }


    // =========================================================
    // TEST 8
    // USER B CANNOT ACCESS USER A WORKFLOW
    // =========================================================

    @Test
    void userB_cannotAccessUserAWorkflow()
            throws Exception {

        when(
                workflowService.getWorkflowById(
                        1L,
                        USER_B
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "Workflow not found"
                )
        );


        mockMvc.perform(
                get("/api/workflows/1")
                        .header(
                                "Authorization",
                                "Bearer " + userBToken
                        )
        )
        .andExpect(
                status().isNotFound()
        )
        .andExpect(
                jsonPath("$.message")
                        .value("Workflow not found")
        );


        verify(
                workflowService,
                times(1)
        ).getWorkflowById(
                1L,
                USER_B
        );


        verify(
                workflowService,
                never()
        ).getWorkflowById(
                1L,
                USER_A
        );
    }


    // =========================================================
    // TEST 9
    // USER A CAN UPDATE OWN WORKFLOW
    // =========================================================

    @Test
    void userA_canUpdateOwnWorkflow()
            throws Exception {

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Updated Workflow",
                        "Updated by User A",
                        WorkflowStatus.ACTIVE,
                        USER_A,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(
                workflowService.updateWorkflow(
                        eq(1L),
                        any(WorkflowRequest.class),
                        eq(USER_A)
                )
        ).thenReturn(
                response
        );


        String requestJson = """
                {
                    "name": "Updated Workflow",
                    "description": "Updated by User A",
                    "status": "ACTIVE"
                }
                """;


        mockMvc.perform(
                put("/api/workflows/1")
                        .header(
                                "Authorization",
                                "Bearer " + userAToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                requestJson
                        )
        )
        .andExpect(
                status().isOk()
        )
        .andExpect(
                jsonPath("$.createdBy")
                        .value(USER_A)
        )
        .andExpect(
                jsonPath("$.status")
                        .value("ACTIVE")
        );


        verify(
                workflowService,
                times(1)
        ).updateWorkflow(
                eq(1L),
                any(WorkflowRequest.class),
                eq(USER_A)
        );
    }


    // =========================================================
    // TEST 10
    // USER B CANNOT UPDATE USER A WORKFLOW
    // =========================================================

    @Test
    void userB_cannotUpdateUserAWorkflow()
            throws Exception {

        when(
                workflowService.updateWorkflow(
                        eq(1L),
                        any(WorkflowRequest.class),
                        eq(USER_B)
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "Workflow not found"
                )
        );


        String requestJson = """
                {
                    "name": "Hacked Workflow",
                    "description": "User B attempt",
                    "status": "ACTIVE"
                }
                """;


        mockMvc.perform(
                put("/api/workflows/1")
                        .header(
                                "Authorization",
                                "Bearer " + userBToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                requestJson
                        )
        )
        .andExpect(
                status().isNotFound()
        )
        .andExpect(
                jsonPath("$.message")
                        .value("Workflow not found")
        );


        verify(
                workflowService,
                times(1)
        ).updateWorkflow(
                eq(1L),
                any(WorkflowRequest.class),
                eq(USER_B)
        );


        verify(
                workflowService,
                never()
        ).updateWorkflow(
                eq(1L),
                any(WorkflowRequest.class),
                eq(USER_A)
        );
    }


    // =========================================================
    // TEST 11
    // USER A CAN DELETE OWN WORKFLOW
    // =========================================================

    @Test
    void userA_canDeleteOwnWorkflow()
            throws Exception {

        doNothing().when(
                workflowService
        ).deleteWorkflow(
                1L,
                USER_A
        );


        mockMvc.perform(
                delete("/api/workflows/1")
                        .header(
                                "Authorization",
                                "Bearer " + userAToken
                        )
        )
        .andExpect(
                status().isNoContent()
        );


        verify(
                workflowService,
                times(1)
        ).deleteWorkflow(
                1L,
                USER_A
        );
    }


    // =========================================================
    // TEST 12
    // USER B CANNOT DELETE USER A WORKFLOW
    // =========================================================

    @Test
    void userB_cannotDeleteUserAWorkflow()
            throws Exception {

        doThrow(
                new ResourceNotFoundException(
                        "Workflow not found"
                )
        ).when(
                workflowService
        ).deleteWorkflow(
                1L,
                USER_B
        );


        mockMvc.perform(
                delete("/api/workflows/1")
                        .header(
                                "Authorization",
                                "Bearer " + userBToken
                        )
        )
        .andExpect(
                status().isNotFound()
        )
        .andExpect(
                jsonPath("$.message")
                        .value("Workflow not found")
        );


        verify(
                workflowService,
                times(1)
        ).deleteWorkflow(
                1L,
                USER_B
        );


        verify(
                workflowService,
                never()
        ).deleteWorkflow(
                1L,
                USER_A
        );
    }
}
