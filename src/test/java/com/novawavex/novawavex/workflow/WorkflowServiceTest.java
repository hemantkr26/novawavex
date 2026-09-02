
package com.novawavex.novawavex.workflow;

import com.novawavex.novawavex.dto.WorkflowRequest;
import com.novawavex.novawavex.dto.WorkflowResponse;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.ResourceNotFoundException;
import com.novawavex.novawavex.notification.NotificationPriority;
import com.novawavex.novawavex.notification.NotificationService;
import com.novawavex.novawavex.notification.NotificationType;
import com.novawavex.novawavex.repository.UserRepository;

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
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkflowService workflowService;

    private Workflow workflow;

    private User user;

    private final String USER_EMAIL =
            "workflow-service@novawavex.com";

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        workflow =
                new Workflow(
                        "Test Workflow",
                        "Workflow service test",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL
                );

        workflow.setName("Test Workflow");

        user =
                new User(
                        "Workflow Service User",
                        USER_EMAIL,
                        "encodedPassword",
                        "USER"
                );
    }

    // =========================================================
    // HELPER
    // =========================================================

    private WorkflowRequest createWorkflowRequest(
            String name,
            String description,
            WorkflowStatus status) {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(name);
        request.setDescription(description);
        request.setStatus(status);

        return request;
    }

    // =========================================================
    // 1. CREATE WORKFLOW
    // =========================================================

    @Test
    void createWorkflow_shouldCreateWorkflowAndNotification() {

        WorkflowRequest request =
                createWorkflowRequest(
                        "New Workflow",
                        "New workflow description",
                        WorkflowStatus.ACTIVE
                );

        workflow =
                new Workflow(
                        "New Workflow",
                        "New workflow description",
                        WorkflowStatus.ACTIVE,
                        USER_EMAIL
                );

        workflow.setName("New Workflow");

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenAnswer(invocation -> {

            Workflow saved =
                    invocation.getArgument(0);

            return saved;
        });

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        WorkflowResponse response =
                workflowService.createWorkflow(
                        request,
                        USER_EMAIL
                );

        assertNotNull(response);

        assertEquals(
                "New Workflow",
                response.getName()
        );

        assertEquals(
                "New workflow description",
                response.getDescription()
        );

        assertEquals(
                WorkflowStatus.ACTIVE,
                response.getStatus()
        );

        assertEquals(
                USER_EMAIL,
                response.getCreatedBy()
        );

        verify(workflowRepository)
                .save(any(Workflow.class));

        verify(userRepository)
                .findByEmail(USER_EMAIL);

        verify(notificationService)
                .createNotification(
                        eq("Workflow created"),
                        eq("New Workflow was created successfully."),
                        eq(NotificationType.WORKFLOW_CREATED),
                        eq(NotificationPriority.SUCCESS),
                        eq(user),
                        any(Workflow.class)
                );
    }

    // =========================================================
    // 2. CREATE WORKFLOW - USER NOT FOUND
    // =========================================================

    @Test
    void createWorkflow_whenUserDoesNotExist_shouldThrowException() {

        WorkflowRequest request =
                createWorkflowRequest(
                        "New Workflow",
                        "Description",
                        WorkflowStatus.DRAFT
                );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenReturn(workflow);

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.createWorkflow(
                                request,
                                USER_EMAIL
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(workflowRepository)
                .save(any(Workflow.class));

        verify(userRepository)
                .findByEmail(USER_EMAIL);

        verify(notificationService, never())
                .createNotification(
                        anyString(),
                        anyString(),
                        any(NotificationType.class),
                        any(NotificationPriority.class),
                        any(User.class),
                        any(Workflow.class)
                );
    }

    // =========================================================
    // 3. CREATE WORKFLOW - REQUEST DATA
    // =========================================================

    @Test
    void createWorkflow_shouldSaveCorrectWorkflowData() {

        WorkflowRequest request =
                createWorkflowRequest(
                        "Correct Workflow",
                        "Correct Description",
                        WorkflowStatus.COMPLETED
                );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenAnswer(invocation -> {

            Workflow saved =
                    invocation.getArgument(0);

            saved.setName("Correct Workflow");

            return saved;
        });

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        workflowService.createWorkflow(
                request,
                USER_EMAIL
        );

        ArgumentCaptor<Workflow> captor =
                ArgumentCaptor.forClass(
                        Workflow.class
                );

        verify(workflowRepository)
                .save(captor.capture());

        Workflow savedWorkflow =
                captor.getValue();

        assertEquals(
                "Correct Workflow",
                savedWorkflow.getName()
        );

        assertEquals(
                "Correct Description",
                savedWorkflow.getDescription()
        );

        assertEquals(
                WorkflowStatus.COMPLETED,
                savedWorkflow.getStatus()
        );

        assertEquals(
                USER_EMAIL,
                savedWorkflow.getCreatedBy()
        );
    }

    // =========================================================
    // 4. GET ALL WORKFLOWS
    // =========================================================

    @Test
    void getAllWorkflows_shouldReturnUserWorkflows() {

        Workflow workflow1 =
                new Workflow(
                        "Workflow One",
                        "Description One",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL
                );

        Workflow workflow2 =
                new Workflow(
                        "Workflow Two",
                        "Description Two",
                        WorkflowStatus.ACTIVE,
                        USER_EMAIL
                );

        workflow1.setName("Workflow One");
        workflow2.setName("Workflow Two");

        when(workflowRepository.findByCreatedBy(
                USER_EMAIL
        )).thenReturn(
                List.of(
                        workflow1,
                        workflow2
                )
        );

        List<WorkflowResponse> result =
                workflowService.getAllWorkflows(
                        USER_EMAIL
                );

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Workflow One",
                result.get(0).getName()
        );

        assertEquals(
                WorkflowStatus.DRAFT,
                result.get(0).getStatus()
        );

        assertEquals(
                "Workflow Two",
                result.get(1).getName()
        );

        assertEquals(
                WorkflowStatus.ACTIVE,
                result.get(1).getStatus()
        );

        verify(workflowRepository)
                .findByCreatedBy(USER_EMAIL);
    }

    // =========================================================
    // 5. GET ALL WORKFLOWS - EMPTY
    // =========================================================

    @Test
    void getAllWorkflows_whenNoWorkflowsExist_shouldReturnEmptyList() {

        when(workflowRepository.findByCreatedBy(
                USER_EMAIL
        )).thenReturn(
                List.of()
        );

        List<WorkflowResponse> result =
                workflowService.getAllWorkflows(
                        USER_EMAIL
                );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );

        verify(workflowRepository)
                .findByCreatedBy(USER_EMAIL);
    }

    // =========================================================
    // 6. GET WORKFLOW BY ID
    // =========================================================

    @Test
    void getWorkflowById_whenOwnerMatches_shouldReturnWorkflow() {

        when(workflowRepository.findByIdAndCreatedBy(
                1L,
                USER_EMAIL
        )).thenReturn(
                Optional.of(workflow)
        );

        WorkflowResponse response =
                workflowService.getWorkflowById(
                        1L,
                        USER_EMAIL
                );

        assertNotNull(response);

        assertEquals(
                "Test Workflow",
                response.getName()
        );

        assertEquals(
                WorkflowStatus.DRAFT,
                response.getStatus()
        );

        assertEquals(
                USER_EMAIL,
                response.getCreatedBy()
        );

        verify(workflowRepository)
                .findByIdAndCreatedBy(
                        1L,
                        USER_EMAIL
                );
    }

    // =========================================================
    // 7. GET WORKFLOW BY ID - NOT FOUND
    // =========================================================

    @Test
    void getWorkflowById_whenWorkflowDoesNotExist_shouldThrowException() {

        when(workflowRepository.findByIdAndCreatedBy(
                999L,
                USER_EMAIL
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.getWorkflowById(
                                999L,
                                USER_EMAIL
                        )
                );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );

        verify(workflowRepository)
                .findByIdAndCreatedBy(
                        999L,
                        USER_EMAIL
                );
    }

    // =========================================================
    // 8. GET WORKFLOW BY ID - WRONG USER
    // =========================================================

    @Test
    void getWorkflowById_whenUserDoesNotOwnWorkflow_shouldThrowException() {

        when(workflowRepository.findByIdAndCreatedBy(
                1L,
                "different@novawavex.com"
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.getWorkflowById(
                                1L,
                                "different@novawavex.com"
                        )
                );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );
    }

    // =========================================================
    // 9. UPDATE WORKFLOW
    // =========================================================

    @Test
    void updateWorkflow_shouldUpdateWorkflowAndNotification() {

        workflow =
                new Workflow(
                        "Old Workflow",
                        "Old Description",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL
                );

        WorkflowRequest request =
                createWorkflowRequest(
                        "Updated Workflow",
                        "Updated Description",
                        WorkflowStatus.ACTIVE
                );

        when(workflowRepository.findByIdAndCreatedBy(
                1L,
                USER_EMAIL
        )).thenReturn(
                Optional.of(workflow)
        );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        WorkflowResponse response =
                workflowService.updateWorkflow(
                        1L,
                        request,
                        USER_EMAIL
                );

        assertNotNull(response);

        assertEquals(
                "Updated Workflow",
                response.getName()
        );

        assertEquals(
                "Updated Description",
                response.getDescription()
        );

        assertEquals(
                WorkflowStatus.ACTIVE,
                response.getStatus()
        );

        assertEquals(
                USER_EMAIL,
                response.getCreatedBy()
        );

        verify(workflowRepository)
                .findByIdAndCreatedBy(
                        1L,
                        USER_EMAIL
                );

        verify(workflowRepository)
                .save(workflow);

        verify(notificationService)
                .createNotification(
                        eq("Workflow updated"),
                        eq("Updated Workflow was updated successfully."),
                        eq(NotificationType.WORKFLOW_UPDATED),
                        eq(NotificationPriority.SUCCESS),
                        eq(user),
                        eq(workflow)
                );
    }

    // =========================================================
    // 10. UPDATE WORKFLOW - NOT FOUND
    // =========================================================

    @Test
    void updateWorkflow_whenWorkflowDoesNotExist_shouldThrowException() {

        WorkflowRequest request =
                createWorkflowRequest(
                        "Updated Workflow",
                        "Description",
                        WorkflowStatus.ACTIVE
                );

        when(workflowRepository.findByIdAndCreatedBy(
                999L,
                USER_EMAIL
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.updateWorkflow(
                                999L,
                                request,
                                USER_EMAIL
                        )
                );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );

        verify(workflowRepository, never())
                .save(any(Workflow.class));

        verify(notificationService, never())
                .createNotification(
                        anyString(),
                        anyString(),
                        any(NotificationType.class),
                        any(NotificationPriority.class),
                        any(User.class),
                        any()
                );
    }

    // =========================================================
    // 11. UPDATE WORKFLOW - USER NOT FOUND
    // =========================================================

    @Test
    void updateWorkflow_whenUserDoesNotExist_shouldThrowException() {

        WorkflowRequest request =
                createWorkflowRequest(
                        "Updated Workflow",
                        "Description",
                        WorkflowStatus.ACTIVE
                );

        when(workflowRepository.findByIdAndCreatedBy(
                1L,
                USER_EMAIL
        )).thenReturn(
                Optional.of(workflow)
        );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenReturn(workflow);

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.updateWorkflow(
                                1L,
                                request,
                                USER_EMAIL
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(workflowRepository)
                .save(workflow);

        verify(notificationService, never())
                .createNotification(
                        anyString(),
                        anyString(),
                        any(NotificationType.class),
                        any(NotificationPriority.class),
                        any(User.class),
                        any()
                );
    }

    // =========================================================
    // 12. DELETE WORKFLOW
    // =========================================================

    @Test
    void deleteWorkflow_shouldDetachNotificationsCreateNotificationAndDeleteWorkflow() {

        workflow =
                new Workflow(
                        "Delete Workflow",
                        "Workflow to delete",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL
                );

        when(workflowRepository.findByIdAndCreatedBy(
                5L,
                USER_EMAIL
        )).thenReturn(
                Optional.of(workflow)
        );

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        workflowService.deleteWorkflow(
                5L,
                USER_EMAIL
        );

        verify(workflowRepository)
                .findByIdAndCreatedBy(
                        5L,
                        USER_EMAIL
                );

        verify(userRepository)
                .findByEmail(USER_EMAIL);

        verify(notificationService)
                .detachNotificationsFromWorkflow(5L);

        verify(notificationService)
                .createNotification(
                        eq("Workflow deleted"),
                        eq("Delete Workflow was deleted successfully."),
                        eq(NotificationType.WORKFLOW_DELETED),
                        eq(NotificationPriority.SUCCESS),
                        eq(user),
                        isNull()
                );

        verify(workflowRepository)
                .delete(workflow);
    }

    // =========================================================
    // 13. DELETE WORKFLOW - NOT FOUND
    // =========================================================

    @Test
    void deleteWorkflow_whenWorkflowDoesNotExist_shouldThrowException() {

        when(workflowRepository.findByIdAndCreatedBy(
                999L,
                USER_EMAIL
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.deleteWorkflow(
                                999L,
                                USER_EMAIL
                        )
                );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findByEmail(anyString());

        verify(notificationService, never())
                .detachNotificationsFromWorkflow(anyLong());

        verify(workflowRepository, never())
                .delete(any(Workflow.class));
    }

    // =========================================================
    // 14. DELETE WORKFLOW - USER NOT FOUND
    // =========================================================

    @Test
    void deleteWorkflow_whenUserDoesNotExist_shouldThrowException() {

        when(workflowRepository.findByIdAndCreatedBy(
                1L,
                USER_EMAIL
        )).thenReturn(
                Optional.of(workflow)
        );

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.deleteWorkflow(
                                1L,
                                USER_EMAIL
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(notificationService, never())
                .detachNotificationsFromWorkflow(anyLong());

        verify(notificationService, never())
                .createNotification(
                        anyString(),
                        anyString(),
                        any(NotificationType.class),
                        any(NotificationPriority.class),
                        any(User.class),
                        any()
                );

        verify(workflowRepository, never())
                .delete(any(Workflow.class));
    }

    // =========================================================
    // 15. DELETE WORKFLOW - OWNERSHIP
    // =========================================================

    @Test
    void deleteWorkflow_whenDifferentUserRequestsDelete_shouldThrowException() {

        String differentUser =
                "different@novawavex.com";

        when(workflowRepository.findByIdAndCreatedBy(
                1L,
                differentUser
        )).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> workflowService.deleteWorkflow(
                                1L,
                                differentUser
                        )
                );

        assertEquals(
                "Workflow not found",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findByEmail(anyString());

        verify(notificationService, never())
                .detachNotificationsFromWorkflow(anyLong());

        verify(workflowRepository, never())
                .delete(any(Workflow.class));
    }

    // =========================================================
    // 16. UPDATE WORKFLOW - REQUEST VALUES
    // =========================================================

    @Test
    void updateWorkflow_shouldApplyAllRequestValues() {

        workflow =
                new Workflow(
                        "Original Name",
                        "Original Description",
                        WorkflowStatus.DRAFT,
                        USER_EMAIL
                );

        WorkflowRequest request =
                createWorkflowRequest(
                        "Changed Name",
                        "Changed Description",
                        WorkflowStatus.COMPLETED
                );

        when(workflowRepository.findByIdAndCreatedBy(
                10L,
                USER_EMAIL
        )).thenReturn(
                Optional.of(workflow)
        );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        workflowService.updateWorkflow(
                10L,
                request,
                USER_EMAIL
        );

        assertEquals(
                "Changed Name",
                workflow.getName()
        );

        assertEquals(
                "Changed Description",
                workflow.getDescription()
        );

        assertEquals(
                WorkflowStatus.COMPLETED,
                workflow.getStatus()
        );
    }

    // =========================================================
    // 17. CREATE WORKFLOW - CREATED BY
    // =========================================================

    @Test
    void createWorkflow_shouldSetCreatedByFromAuthenticatedUser() {

        WorkflowRequest request =
                createWorkflowRequest(
                        "Ownership Workflow",
                        "Ownership test",
                        WorkflowStatus.DRAFT
                );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        workflowService.createWorkflow(
                request,
                USER_EMAIL
        );

        verify(workflowRepository)
                .save(argThat(savedWorkflow ->
                        USER_EMAIL.equals(
                                savedWorkflow.getCreatedBy()
                        )
                ));
    }

    // =========================================================
    // 18. GET ALL WORKFLOWS - USER ISOLATION
    // =========================================================

    @Test
    void getAllWorkflows_shouldQueryOnlyAuthenticatedUser() {

        when(workflowRepository.findByCreatedBy(
                USER_EMAIL
        )).thenReturn(
                List.of()
        );

        workflowService.getAllWorkflows(
                USER_EMAIL
        );

        verify(workflowRepository)
                .findByCreatedBy(USER_EMAIL);

        verify(workflowRepository, never())
                .findAll();
    }

    // =========================================================
    // 19. CREATE NOTIFICATION CONTENT
    // =========================================================

    @Test
    void createWorkflow_shouldCreateSuccessNotificationWithWorkflowReference() {

        workflow =
                new Workflow(
                        "Notification Workflow",
                        "Notification test",
                        WorkflowStatus.ACTIVE,
                        USER_EMAIL
                );

        when(workflowRepository.save(
                any(Workflow.class)
        )).thenReturn(workflow);

        when(userRepository.findByEmail(
                USER_EMAIL
        )).thenReturn(
                Optional.of(user)
        );

        workflowService.createWorkflow(
                createWorkflowRequest(
                        "Notification Workflow",
                        "Notification test",
                        WorkflowStatus.ACTIVE
                ),
                USER_EMAIL
        );

        verify(notificationService)
                .createNotification(
                        "Workflow created",
                        "Notification Workflow was created successfully.",
                        NotificationType.WORKFLOW_CREATED,
                        NotificationPriority.SUCCESS,
                        user,
                        workflow
                );
    }
}
