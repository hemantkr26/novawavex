package com.novawavex.novawavex.workflow;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.notification.NotificationPriority;
import com.novawavex.novawavex.notification.NotificationService;
import com.novawavex.novawavex.notification.NotificationType;
import com.novawavex.novawavex.repository.UserRepository;
import com.novawavex.novawavex.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionServiceTest {

    @Mock
    private WorkflowExecutionRepository executionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkflowExecutionService executionService;

    private Workflow workflow;

    private WorkflowExecution execution;

    private User user;

    private final String USER_A =
            "userA@example.com";

    private final String USER_B =
            "userB@example.com";


    @BeforeEach
    void setUp() {

        workflow = new Workflow(
                "Test Workflow",
                "Test workflow description",
                WorkflowStatus.DRAFT,
                USER_A
        );

        execution = new WorkflowExecution(
                workflow,
                USER_A
        );

        execution.setStatus(
                ExecutionStatus.QUEUED
        );

        user = new User();

        user.setFullName(
                "Test User"
        );

        user.setEmail(
                USER_A
        );

        user.setRole(
                "USER"
        );
    }


    // =========================================================
    // 1. CREATE EXECUTION
    // =========================================================

    @Test
    void createExecution_shouldCreateQueuedExecution() {

        when(executionRepository.save(
                any(WorkflowExecution.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        WorkflowExecution result =
                executionService.createExecution(
                        workflow,
                        USER_A
                );

        assertNotNull(result);

        assertEquals(
                workflow,
                result.getWorkflow()
        );

        assertEquals(
                USER_A,
                result.getCreatedBy()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                result.getStatus()
        );

        verify(executionRepository)
                .save(any(WorkflowExecution.class));

        verifyNoInteractions(
                notificationService
        );

        verifyNoInteractions(
                userRepository
        );
    }


    // =========================================================
    // 2. CREATE EXECUTION - NULL WORKFLOW
    // =========================================================

    @Test
    void createExecution_withNullWorkflow_shouldThrowException() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                executionService.createExecution(
                                        null,
                                        USER_A
                                )
                );

        assertEquals(
                "Workflow cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                executionRepository,
                notificationService,
                userRepository
        );
    }


    // =========================================================
    // 3. CREATE EXECUTION - INVALID USER
    // =========================================================

    @Test
    void createExecution_withEmptyCreatedBy_shouldThrowException() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                executionService.createExecution(
                                        workflow,
                                        " "
                                )
                );

        assertEquals(
                "Created by cannot be empty",
                exception.getMessage()
        );

        verifyNoInteractions(
                executionRepository,
                notificationService,
                userRepository
        );
    }


    // =========================================================
    // 4. CREATE EXECUTION - NULL USER
    // =========================================================

    @Test
    void createExecution_withNullCreatedBy_shouldThrowException() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                executionService.createExecution(
                                        workflow,
                                        null
                                )
                );

        assertEquals(
                "Created by cannot be empty",
                exception.getMessage()
        );

        verifyNoInteractions(
                executionRepository,
                notificationService,
                userRepository
        );
    }


    // =========================================================
    // 5. GET EXECUTION BY ID
    // =========================================================

    @Test
    void getExecutionById_shouldReturnExecution() {

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        WorkflowExecution result =
                executionService.getExecutionById(
                        1L
                );

        assertNotNull(result);

        assertEquals(
                execution,
                result
        );

        verify(executionRepository)
                .findById(1L);
    }


    // =========================================================
    // 6. GET EXECUTION BY ID - NOT FOUND
    // =========================================================

    @Test
    void getExecutionById_whenNotFound_shouldThrowException() {

        when(
                executionRepository.findById(999L)
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                executionService.getExecutionById(
                                        999L
                                )
                );

        assertEquals(
                "Execution not found: 999",
                exception.getMessage()
        );

        verify(executionRepository)
                .findById(999L);
    }


    // =========================================================
    // 7. GET WORKFLOW EXECUTIONS
    // =========================================================

    @Test
    void getExecutionsByWorkflow_shouldReturnExecutions() {

        when(
                executionRepository.findByWorkflowId(1L)
        ).thenReturn(
                List.of(execution)
        );

        List<WorkflowExecution> result =
                executionService
                        .getExecutionsByWorkflow(1L);

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                execution,
                result.get(0)
        );

        verify(executionRepository)
                .findByWorkflowId(1L);
    }


    // =========================================================
    // 8. GET WORKFLOW EXECUTIONS - EMPTY
    // =========================================================

    @Test
    void getExecutionsByWorkflow_whenNoneExist_shouldReturnEmptyList() {

        when(
                executionRepository.findByWorkflowId(1L)
        ).thenReturn(
                List.of()
        );

        List<WorkflowExecution> result =
                executionService
                        .getExecutionsByWorkflow(1L);

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );

        verify(executionRepository)
                .findByWorkflowId(1L);
    }


    // =========================================================
    // 9. GET USER EXECUTIONS
    // =========================================================

    @Test
    void getExecutionsByUser_shouldReturnUserExecutions() {

        when(
                executionRepository.findByCreatedBy(USER_A)
        ).thenReturn(
                List.of(execution)
        );

        List<WorkflowExecution> result =
                executionService
                        .getExecutionsByUser(USER_A);

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                USER_A,
                result.get(0).getCreatedBy()
        );

        verify(executionRepository)
                .findByCreatedBy(USER_A);
    }


    // =========================================================
    // 10. GET USER EXECUTIONS - EMPTY
    // =========================================================

    @Test
    void getExecutionsByUser_whenNoneExist_shouldReturnEmptyList() {

        when(
                executionRepository.findByCreatedBy(USER_B)
        ).thenReturn(
                List.of()
        );

        List<WorkflowExecution> result =
                executionService
                        .getExecutionsByUser(USER_B);

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );

        verify(executionRepository)
                .findByCreatedBy(USER_B);
    }


    // =========================================================
    // 11. GET USER WORKFLOW EXECUTIONS
    // =========================================================

    @Test
    void getExecutionsByWorkflowAndUser_shouldReturnExecutions() {

        when(
                executionRepository
                        .findByWorkflowIdAndCreatedBy(
                                1L,
                                USER_A
                        )
        ).thenReturn(
                List.of(execution)
        );

        List<WorkflowExecution> result =
                executionService
                        .getExecutionsByWorkflowAndUser(
                                1L,
                                USER_A
                        );

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                execution,
                result.get(0)
        );

        verify(
                executionRepository
        ).findByWorkflowIdAndCreatedBy(
                1L,
                USER_A
        );
    }


    // =========================================================
    // 12. START EXECUTION
    // =========================================================

    @Test
    void startExecution_shouldChangeStatusToRunning() {

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        WorkflowExecution result =
                executionService.startExecution(
                        1L,
                        USER_A
                );

        assertNotNull(result);

        assertEquals(
                ExecutionStatus.RUNNING,
                result.getStatus()
        );

        assertNotNull(
                result.getStartedAt()
        );

        assertEquals(
                USER_A,
                result.getUpdatedBy()
        );

        verify(executionRepository)
                .findById(1L);

        verify(executionRepository)
                .save(execution);

        verify(userRepository)
                .findByEmail(USER_A);

        verify(notificationService)
                .createNotification(
                        eq("Workflow started"),
                        eq("Test Workflow was started successfully."),
                        eq(NotificationType.WORKFLOW_STARTED),
                        eq(NotificationPriority.SUCCESS),
                        eq(user),
                        eq(workflow)
                );
    }


    // =========================================================
    // 13. START EXECUTION - INVALID TRANSITION
    // =========================================================

    @Test
    void startExecution_whenTransitionIsInvalid_shouldThrowException() {

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                executionService.startExecution(
                                        1L,
                                        USER_A
                                )
                );

        assertEquals(
                "Execution cannot transition from COMPLETED to RUNNING",
                exception.getMessage()
        );

        verify(
                executionRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                userRepository,
                notificationService
        );
    }


    // =========================================================
    // 14. COMPLETE EXECUTION
    // =========================================================

    @Test
    void completeExecution_shouldChangeStatusToCompleted() {

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        WorkflowExecution result =
                executionService.completeExecution(
                        1L,
                        USER_A
                );

        assertNotNull(result);

        assertEquals(
                ExecutionStatus.COMPLETED,
                result.getStatus()
        );

        assertNotNull(
                result.getCompletedAt()
        );

        assertEquals(
                USER_A,
                result.getUpdatedBy()
        );

        verify(executionRepository)
                .save(execution);

        verify(userRepository)
                .findByEmail(USER_A);

        verify(notificationService)
                .createNotification(
                        eq("Workflow completed"),
                        eq("Test Workflow was completed successfully."),
                        eq(NotificationType.WORKFLOW_COMPLETED),
                        eq(NotificationPriority.SUCCESS),
                        eq(user),
                        eq(workflow)
                );
    }


    // =========================================================
    // 15. COMPLETE EXECUTION - INVALID TRANSITION
    // =========================================================

    @Test
    void completeExecution_whenTransitionIsInvalid_shouldThrowException() {

        execution.setStatus(
                ExecutionStatus.QUEUED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                executionService.completeExecution(
                                        1L,
                                        USER_A
                                )
                );

        assertEquals(
                "Execution cannot transition from QUEUED to COMPLETED",
                exception.getMessage()
        );

        verify(
                executionRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                userRepository,
                notificationService
        );
    }


    // =========================================================
    // 16. FAIL EXECUTION
    // =========================================================

    @Test
    void failExecution_shouldChangeStatusToFailed() {

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        WorkflowExecution result =
                executionService.failExecution(
                        1L,
                        "Test failure",
                        USER_A
                );

        assertNotNull(result);

        assertEquals(
                ExecutionStatus.FAILED,
                result.getStatus()
        );

        assertEquals(
                "Test failure",
                result.getErrorMessage()
        );

        assertNotNull(
                result.getCompletedAt()
        );

        assertEquals(
                USER_A,
                result.getUpdatedBy()
        );

        verify(executionRepository)
                .save(execution);

        verify(userRepository)
                .findByEmail(USER_A);

        verify(notificationService)
                .createNotification(
                        eq("Workflow failed"),
                        eq("Test Workflow failed during execution."),
                        eq(NotificationType.WORKFLOW_FAILED),
                        eq(NotificationPriority.ERROR),
                        eq(user),
                        eq(workflow)
                );
    }


    // =========================================================
    // 17. FAIL EXECUTION - INVALID TRANSITION
    // =========================================================

    @Test
    void failExecution_whenTransitionIsInvalid_shouldThrowException() {

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                executionService.failExecution(
                                        1L,
                                        "Test failure",
                                        USER_A
                                )
                );

        assertEquals(
                "Execution cannot transition from COMPLETED to FAILED",
                exception.getMessage()
        );

        verify(
                executionRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                userRepository,
                notificationService
        );
    }


    // =========================================================
    // 18. CANCEL EXECUTION
    // =========================================================

    @Test
    void cancelExecution_shouldChangeStatusToCancelled() {

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        WorkflowExecution result =
                executionService.cancelExecution(
                        1L,
                        USER_A
                );

        assertNotNull(result);

        assertEquals(
                ExecutionStatus.CANCELLED,
                result.getStatus()
        );

        assertNotNull(
                result.getCompletedAt()
        );

        assertEquals(
                USER_A,
                result.getUpdatedBy()
        );

        verify(executionRepository)
                .save(execution);

        verify(userRepository)
                .findByEmail(USER_A);

        verify(notificationService)
                .createNotification(
                        eq("Workflow cancelled"),
                        eq("Test Workflow was cancelled successfully."),
                        eq(NotificationType.WORKFLOW_CANCELLED),
                        eq(NotificationPriority.WARNING),
                        eq(user),
                        eq(workflow)
                );
    }


    // =========================================================
    // 19. CANCEL EXECUTION - INVALID TRANSITION
    // =========================================================

    @Test
    void cancelExecution_whenTransitionIsInvalid_shouldThrowException() {

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                executionService.cancelExecution(
                                        1L,
                                        USER_A
                                )
                );

        assertEquals(
                "Execution cannot transition from COMPLETED to CANCELLED",
                exception.getMessage()
        );

        verify(
                executionRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                userRepository,
                notificationService
        );
    }


    // =========================================================
    // 20. RETRY FAILED EXECUTION
    // =========================================================

    @Test
    void retryExecution_shouldCreateNewQueuedExecution() {

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        WorkflowExecution result =
                executionService.retryExecution(
                        1L,
                        USER_A
                );

        assertNotNull(result);

        assertEquals(
                ExecutionStatus.QUEUED,
                result.getStatus()
        );

        assertEquals(
                USER_A,
                result.getCreatedBy()
        );

        assertEquals(
                workflow,
                result.getWorkflow()
        );

        verify(executionRepository)
                .save(any(WorkflowExecution.class));

        verify(userRepository)
                .findByEmail(USER_A);

        verify(notificationService)
                .createNotification(
                        eq("Workflow retry queued"),
                        eq("Test Workflow has been queued for retry."),
                        eq(NotificationType.WORKFLOW_RETRIED),
                        eq(NotificationPriority.INFO),
                        eq(user),
                        eq(workflow)
                );
    }


    // =========================================================
    // 21. RETRY - WRONG OWNER
    // =========================================================

    @Test
    void retryExecution_withWrongOwner_shouldThrowException() {

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                executionService.retryExecution(
                                        1L,
                                        USER_B
                                )
                );

        assertEquals(
                "Execution not found",
                exception.getMessage()
        );

        verify(
                executionRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                userRepository,
                notificationService
        );
    }


    // =========================================================
    // 22. RETRY - NOT FAILED
    // =========================================================

    @Test
    void retryExecution_whenExecutionIsNotFailed_shouldThrowException() {

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                executionService.retryExecution(
                                        1L,
                                        USER_A
                                )
                );

        assertEquals(
                "Only failed executions can be retried",
                exception.getMessage()
        );

        verify(
                executionRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                userRepository,
                notificationService
        );
    }


    // =========================================================
    // 23. RETRY - NEW EXECUTION IS DIFFERENT OBJECT
    // =========================================================

    @Test
    void retryExecution_shouldCreateDifferentExecutionObject() {

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        WorkflowExecution result =
                executionService.retryExecution(
                        1L,
                        USER_A
                );

        assertNotSame(
                execution,
                result
        );

        assertEquals(
                workflow,
                result.getWorkflow()
        );

        assertEquals(
                USER_A,
                result.getCreatedBy()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                result.getStatus()
        );
    }


    // =========================================================
    // 24. USER NOT FOUND DURING START
    // =========================================================

    @Test
    void startExecution_whenUserNotFound_shouldThrowException() {

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        executionService.startExecution(
                                1L,
                                USER_A
                        )
        );

        verify(executionRepository)
                .save(execution);

        verify(
                notificationService,
                never()
        ).createNotification(
                anyString(),
                anyString(),
                any(NotificationType.class),
                any(NotificationPriority.class),
                any(User.class),
                any(Workflow.class)
        );
    }


    // =========================================================
    // 25. NOTIFICATION VERIFICATION - START
    // =========================================================

    @Test
    void startExecution_shouldCreateCorrectNotification() {

        when(
                executionRepository.findById(1L)
        ).thenReturn(
                Optional.of(execution)
        );

        when(
                executionRepository.save(
                        any(WorkflowExecution.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                userRepository.findByEmail(USER_A)
        ).thenReturn(
                Optional.of(user)
        );

        executionService.startExecution(
                1L,
                USER_A
        );

        ArgumentCaptor<String> titleCaptor =
                ArgumentCaptor.forClass(
                        String.class
                );

        verify(notificationService)
                .createNotification(
                        titleCaptor.capture(),
                        eq("Test Workflow was started successfully."),
                        eq(NotificationType.WORKFLOW_STARTED),
                        eq(NotificationPriority.SUCCESS),
                        eq(user),
                        eq(workflow)
                );

        assertEquals(
                "Workflow started",
                titleCaptor.getValue()
        );
    }
}