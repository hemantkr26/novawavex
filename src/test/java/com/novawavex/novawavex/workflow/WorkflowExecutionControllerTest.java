
package com.novawavex.novawavex.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class WorkflowExecutionControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private WorkflowExecutionService executionService;

    private WorkflowRepository workflowRepository;

    private Authentication authentication;

    private WorkflowExecutionController controller;

    private Workflow workflow;

    private WorkflowExecution execution;

    private final String USER_A =
            "userA@example.com";

    private final String USER_B =
            "userB@example.com";


    @BeforeEach
    void setUp() {

        executionService =
                mock(WorkflowExecutionService.class);

        workflowRepository =
                mock(WorkflowRepository.class);

        authentication =
                mock(Authentication.class);

        objectMapper =
                new ObjectMapper();

        controller =
                new WorkflowExecutionController(
                        executionService,
                        workflowRepository
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();


        workflow =
                new Workflow(
                        "Test Workflow",
                        "Test workflow description",
                        WorkflowStatus.ACTIVE,
                        USER_A
                );

        execution =
                new WorkflowExecution(
                        workflow,
                        USER_A
                );

        execution.setStatus(
                ExecutionStatus.QUEUED
        );
    }


    // =========================================================
    // 1. CREATE EXECUTION
    // =========================================================

    @Test
    void createExecution_shouldReturnCreated()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        when(
                workflowRepository.findByIdAndCreatedBy(
                        1L,
                        USER_A
                )
        ).thenReturn(
                Optional.of(workflow)
        );

        when(
                executionService.createExecution(
                        workflow,
                        USER_A
                )
        ).thenReturn(execution);


        mockMvc.perform(
                post("/api/executions/workflow/1")
                        .principal(authentication)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status")
                .value("QUEUED"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                workflowRepository,
                times(1)
        ).findByIdAndCreatedBy(
                1L,
                USER_A
        );

        verify(
                executionService,
                times(1)
        ).createExecution(
                workflow,
                USER_A
        );
    }


    // =========================================================
    // 2. CREATE EXECUTION - WORKFLOW NOT FOUND
    // =========================================================

    @Test
    void createExecution_whenWorkflowNotFound_shouldThrowException()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        when(
                workflowRepository.findByIdAndCreatedBy(
                        999L,
                        USER_A
                )
        ).thenReturn(
                Optional.empty()
        );


        Exception exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        Exception.class,
                        () ->
                                mockMvc.perform(
                                        post(
                                                "/api/executions/workflow/999"
                                        )
                                                .principal(authentication)
                                )
                );


        assertEquals(
                "Workflow not found",
                exception.getCause() != null
                        ? exception.getCause().getMessage()
                        : exception.getMessage()
        );


        verify(
                executionService,
                never()
        ).createExecution(
                any(Workflow.class),
                anyString()
        );
    }


    // =========================================================
    // 3. GET EXECUTION BY ID
    // =========================================================

    @Test
    void getExecutionById_shouldReturnOk()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);


        mockMvc.perform(
                get("/api/executions/1")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("QUEUED"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).getExecutionById(1L);
    }


    // =========================================================
    // 4. GET EXECUTION - WRONG OWNER
    // =========================================================

    @Test
    void getExecutionById_whenWrongOwner_shouldThrowException()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_B);

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);


        Exception exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        Exception.class,
                        () ->
                                mockMvc.perform(
                                        get("/api/executions/1")
                                                .principal(authentication)
                                )
                );


        assertEquals(
                "Execution not found",
                exception.getCause() != null
                        ? exception.getCause().getMessage()
                        : exception.getMessage()
        );
    }


    // =========================================================
    // 5. GET MY EXECUTIONS
    // =========================================================

    @Test
    void getMyExecutions_shouldReturnExecutions()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        when(
                executionService.getExecutionsByUser(USER_A)
        ).thenReturn(
                List.of(execution)
        );


        mockMvc.perform(
                get("/api/executions")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()")
                .value(1))
        .andExpect(jsonPath("$[0].status")
                .value("QUEUED"))
        .andExpect(jsonPath("$[0].createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).getExecutionsByUser(USER_A);
    }


    // =========================================================
    // 6. START EXECUTION
    // =========================================================

    @Test
    void startExecution_shouldReturnUpdatedExecution()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        when(
                executionService.startExecution(
                        1L,
                        USER_A
                )
        ).thenReturn(execution);


        mockMvc.perform(
                post("/api/executions/1/start")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("RUNNING"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).getExecutionById(1L);

        verify(
                executionService,
                times(1)
        ).startExecution(
                1L,
                USER_A
        );
    }


    // =========================================================
    // 7. START EXECUTION - WRONG OWNER
    // =========================================================

    @Test
    void startExecution_whenWrongOwner_shouldThrowException()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_B);

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);


        Exception exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        Exception.class,
                        () ->
                                mockMvc.perform(
                                        post(
                                                "/api/executions/1/start"
                                        )
                                                .principal(authentication)
                                )
                );


        assertEquals(
                "Execution not found",
                exception.getCause() != null
                        ? exception.getCause().getMessage()
                        : exception.getMessage()
        );


        verify(
                executionService,
                never()
        ).startExecution(
                anyLong(),
                anyString()
        );
    }


    // =========================================================
    // 8. COMPLETE EXECUTION
    // =========================================================

    @Test
    void completeExecution_shouldReturnCompletedExecution()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);

        when(
                executionService.completeExecution(
                        1L,
                        USER_A
                )
        ).thenReturn(execution);


        mockMvc.perform(
                post("/api/executions/1/complete")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("COMPLETED"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).completeExecution(
                1L,
                USER_A
        );
    }


    // =========================================================
    // 9. FAIL EXECUTION
    // =========================================================

    @Test
    void failExecution_shouldReturnFailedExecution()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        execution.setErrorMessage(
                "Test failure"
        );

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);

        when(
                executionService.failExecution(
                        1L,
                        "Test failure",
                        USER_A
                )
        ).thenReturn(execution);


        mockMvc.perform(
                post("/api/executions/1/fail")
                        .param(
                                "errorMessage",
                                "Test failure"
                        )
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("FAILED"))
        .andExpect(jsonPath("$.errorMessage")
                .value("Test failure"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).failExecution(
                1L,
                "Test failure",
                USER_A
        );
    }


    // =========================================================
    // 10. CANCEL EXECUTION
    // =========================================================

    @Test
    void cancelExecution_shouldReturnCancelledExecution()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        execution.setStatus(
                ExecutionStatus.CANCELLED
        );

        when(
                executionService.getExecutionById(1L)
        ).thenReturn(execution);

        when(
                executionService.cancelExecution(
                        1L,
                        USER_A
                )
        ).thenReturn(execution);


        mockMvc.perform(
                post("/api/executions/1/cancel")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("CANCELLED"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).cancelExecution(
                1L,
                USER_A
        );
    }


    // =========================================================
    // 11. RETRY EXECUTION
    // =========================================================

    @Test
    void retryExecution_shouldReturnQueuedExecution()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_A);

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        WorkflowExecution retryExecution =
                new WorkflowExecution(
                        workflow,
                        USER_A
                );

        retryExecution.setStatus(
                ExecutionStatus.QUEUED
        );

        when(
                executionService.retryExecution(
                        1L,
                        USER_A
                )
        ).thenReturn(
                retryExecution
        );


        mockMvc.perform(
                post("/api/executions/1/retry")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("QUEUED"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_A));


        verify(
                executionService,
                times(1)
        ).retryExecution(
                1L,
                USER_A
        );
    }


    // =========================================================
    // 12. RETRY EXECUTION - SERVICE HANDLES OWNERSHIP
    // =========================================================

    @Test
    void retryExecution_shouldDelegateOwnershipToService()
            throws Exception {

        when(authentication.getName())
                .thenReturn(USER_B);

        when(
                executionService.retryExecution(
                        1L,
                        USER_B
                )
        ).thenThrow(
                new IllegalArgumentException(
                        "Execution not found"
                )
        );


        Exception exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        Exception.class,
                        () ->
                                mockMvc.perform(
                                        post(
                                                "/api/executions/1/retry"
                                        )
                                                .principal(authentication)
                                )
                );


        assertEquals(
                "Execution not found",
                exception.getCause() != null
                        ? exception.getCause().getMessage()
                        : exception.getMessage()
        );


        verify(
                executionService,
                times(1)
        ).retryExecution(
                1L,
                USER_B
        );
    }
}
