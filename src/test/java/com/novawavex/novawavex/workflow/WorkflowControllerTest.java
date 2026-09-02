package com.novawavex.novawavex.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.dto.WorkflowRequest;
import com.novawavex.novawavex.dto.WorkflowResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class WorkflowControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private WorkflowService workflowService;

    private Authentication authentication;

    private final String USER_EMAIL = "userA@example.com";


    @BeforeEach
    void setUp() {

        workflowService = mock(WorkflowService.class);

        authentication = mock(Authentication.class);

        objectMapper = new ObjectMapper();

        WorkflowController controller =
                new WorkflowController(workflowService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }


    // =========================================================
    // 1. CREATE WORKFLOW
    // =========================================================

    @Test
    void createWorkflow_shouldReturnCreated() throws Exception {

        WorkflowRequest request = new WorkflowRequest();

        request.setName("Test Workflow");
        request.setDescription("Test description");
        request.setStatus(WorkflowStatus.DRAFT);


        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Test Workflow",
                        "Test description",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        when(
                workflowService.createWorkflow(
                        any(WorkflowRequest.class),
                        eq(USER_EMAIL)
                )
        ).thenReturn(response);


        mockMvc.perform(
                post("/api/workflows")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("Test Workflow"))
        .andExpect(jsonPath("$.description")
                .value("Test description"))
        .andExpect(jsonPath("$.status")
                .value("DRAFT"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_EMAIL));


        verify(
                workflowService,
                times(1)
        ).createWorkflow(
                any(WorkflowRequest.class),
                eq(USER_EMAIL)
        );
    }


    // =========================================================
    // 2. GET ALL WORKFLOWS
    // =========================================================

    @Test
    void getAllWorkflows_shouldReturnOk() throws Exception {

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Workflow 1",
                        "Description 1",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        when(
                workflowService.getAllWorkflows(USER_EMAIL)
        ).thenReturn(List.of(response));


        mockMvc.perform(
                get("/api/workflows")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()")
                .value(1))
        .andExpect(jsonPath("$[0].name")
                .value("Workflow 1"))
        .andExpect(jsonPath("$[0].createdBy")
                .value(USER_EMAIL));


        verify(
                workflowService,
                times(1)
        ).getAllWorkflows(USER_EMAIL);
    }


    // =========================================================
    // 3. GET WORKFLOW BY ID
    // =========================================================

    @Test
    void getWorkflowById_shouldReturnOk() throws Exception {

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Test Workflow",
                        "Test description",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        when(
                workflowService.getWorkflowById(
                        1L,
                        USER_EMAIL
                )
        ).thenReturn(response);


        mockMvc.perform(
                get("/api/workflows/1")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(1))
        .andExpect(jsonPath("$.name")
                .value("Test Workflow"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_EMAIL));


        verify(
                workflowService,
                times(1)
        ).getWorkflowById(
                1L,
                USER_EMAIL
        );
    }


    // =========================================================
    // 4. UPDATE WORKFLOW
    // =========================================================

    @Test
    void updateWorkflow_shouldReturnOk() throws Exception {

        WorkflowRequest request = new WorkflowRequest();

        request.setName("Updated Workflow");
        request.setDescription("Updated description");
        request.setStatus(WorkflowStatus.ACTIVE);


        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Updated Workflow",
                        "Updated description",
                        WorkflowStatus.ACTIVE,
                        USER_EMAIL,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );


        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        when(
                workflowService.updateWorkflow(
                        eq(1L),
                        any(WorkflowRequest.class),
                        eq(USER_EMAIL)
                )
        ).thenReturn(response);


        mockMvc.perform(
                put("/api/workflows/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(1))
        .andExpect(jsonPath("$.name")
                .value("Updated Workflow"))
        .andExpect(jsonPath("$.status")
                .value("ACTIVE"))
        .andExpect(jsonPath("$.createdBy")
                .value(USER_EMAIL));


        verify(
                workflowService,
                times(1)
        ).updateWorkflow(
                eq(1L),
                any(WorkflowRequest.class),
                eq(USER_EMAIL)
        );
    }


    // =========================================================
    // 5. DELETE WORKFLOW
    // =========================================================

    @Test
    void deleteWorkflow_shouldReturnNoContent() throws Exception {

        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        doNothing().when(workflowService)
                .deleteWorkflow(
                        1L,
                        USER_EMAIL
                );


        mockMvc.perform(
                delete("/api/workflows/1")
                        .principal(authentication)
        )
        .andExpect(status().isNoContent());


        verify(
                workflowService,
                times(1)
        ).deleteWorkflow(
                1L,
                USER_EMAIL
        );
    }


    // =========================================================
    // 6. CREATE WORKFLOW - INVALID REQUEST
    // =========================================================

    @Test
    void createWorkflow_withInvalidRequest_shouldReturnBadRequest()
            throws Exception {

        WorkflowRequest request = new WorkflowRequest();

        request.setName("");
        request.setDescription("Invalid request");
        request.setStatus(null);


        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        mockMvc.perform(
                post("/api/workflows")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());


        verify(
                workflowService,
                never()
        ).createWorkflow(
                any(WorkflowRequest.class),
                anyString()
        );
    }


    // =========================================================
    // 7. UPDATE WORKFLOW - INVALID REQUEST
    // =========================================================

    @Test
    void updateWorkflow_withInvalidRequest_shouldReturnBadRequest()
            throws Exception {

        WorkflowRequest request = new WorkflowRequest();

        request.setName("");
        request.setDescription("Invalid request");
        request.setStatus(null);


        when(authentication.getName())
                .thenReturn(USER_EMAIL);


        mockMvc.perform(
                put("/api/workflows/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());


        verify(
                workflowService,
                never()
        ).updateWorkflow(
                anyLong(),
                any(WorkflowRequest.class),
                anyString()
        );
    }
}