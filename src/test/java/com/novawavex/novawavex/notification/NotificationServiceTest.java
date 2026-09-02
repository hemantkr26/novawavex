
package com.novawavex.novawavex.notification;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.notification.dto.NotificationResponse;
import com.novawavex.novawavex.workflow.Workflow;
import com.novawavex.novawavex.workflow.WorkflowStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Workflow workflow;
    private Notification notification;

    private final Long USER_ID = 1L;
    private final Long NOTIFICATION_ID = 10L;
    private final Long WORKFLOW_ID = 20L;

    @BeforeEach
    void setUp() {

        user = new User();

        workflow = new Workflow(
                "Test Workflow",
                "Test workflow description",
                WorkflowStatus.DRAFT,
                "user@example.com"
        );

        notification = new Notification(
                "Test Notification",
                "Test notification message",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.SUCCESS,
                user,
                workflow
        );
    }


    // =========================================================
    // 1. CREATE NOTIFICATION
    // =========================================================

    @Test
    void createNotification_shouldCreateAndReturnNotification() {

        when(notificationRepository.save(
                any(Notification.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationResponse result =
                notificationService.createNotification(
                        "Test Notification",
                        "Test notification message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.SUCCESS,
                        user,
                        workflow
                );

        assertNotNull(result);

        assertEquals(
                "Test Notification",
                result.getTitle()
        );

        assertEquals(
                "Test notification message",
                result.getMessage()
        );

        assertEquals(
                NotificationType.WORKFLOW_STARTED,
                result.getType()
        );

        assertEquals(
                NotificationPriority.SUCCESS,
                result.getPriority()
        );

        assertFalse(result.isRead());

        assertNotNull(result.getCreatedAt());

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // 2. CREATE NOTIFICATION - WORKFLOW
    // =========================================================

    @Test
    void createNotification_shouldIncludeWorkflowInformation() {

        when(notificationRepository.save(
                any(Notification.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationResponse result =
                notificationService.createNotification(
                        "Workflow Started",
                        "Workflow started successfully.",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.SUCCESS,
                        user,
                        workflow
                );

        assertNotNull(result);

        assertEquals(
                workflow.getId(),
                result.getWorkflowId()
        );

        assertEquals(
                workflow.getName(),
                result.getWorkflowName()
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // 3. CREATE NOTIFICATION - NULL WORKFLOW
    // =========================================================

    @Test
    void createNotification_withNullWorkflow_shouldWork() {

        when(notificationRepository.save(
                any(Notification.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationResponse result =
                notificationService.createNotification(
                        "System Notification",
                        "System message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        user,
                        null
                );

        assertNotNull(result);

        assertNull(
                result.getWorkflowId()
        );

        assertNull(
                result.getWorkflowName()
        );

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    // =========================================================
    // 4. DETACH NOTIFICATIONS FROM WORKFLOW
    // =========================================================

    @Test
    void detachNotificationsFromWorkflow_shouldDetachWorkflow() {

        when(notificationRepository.findByWorkflowId(
                WORKFLOW_ID
        )).thenReturn(
                List.of(notification)
        );

        notificationService.detachNotificationsFromWorkflow(
                WORKFLOW_ID
        );

        assertNull(
                notification.getWorkflow()
        );

        verify(notificationRepository)
                .findByWorkflowId(WORKFLOW_ID);

        verify(notificationRepository)
                .saveAll(List.of(notification));
    }


    // =========================================================
    // 5. DETACH - NO NOTIFICATIONS
    // =========================================================

    @Test
    void detachNotificationsFromWorkflow_whenNoneExist_shouldDoNothing() {

        when(notificationRepository.findByWorkflowId(
                WORKFLOW_ID
        )).thenReturn(
                List.of()
        );

        notificationService.detachNotificationsFromWorkflow(
                WORKFLOW_ID
        );

        verify(notificationRepository)
                .findByWorkflowId(WORKFLOW_ID);

        verify(notificationRepository)
                .saveAll(List.of());
    }


    // =========================================================
    // 6. GET ALL NOTIFICATIONS
    // =========================================================

    @Test
    void getNotifications_shouldReturnUserNotifications() {

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(
                        List.of(notification)
                );

        List<NotificationResponse> result =
                notificationService.getNotifications(
                        USER_ID
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                notification.getTitle(),
                result.get(0).getTitle()
        );

        assertEquals(
                notification.getMessage(),
                result.get(0).getMessage()
        );

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(USER_ID);
    }


    // =========================================================
    // 7. GET ALL NOTIFICATIONS - EMPTY
    // =========================================================

    @Test
    void getNotifications_whenNoneExist_shouldReturnEmptyList() {

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(
                        List.of()
                );

        List<NotificationResponse> result =
                notificationService.getNotifications(
                        USER_ID
                );

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(USER_ID);
    }


    // =========================================================
    // 8. GET UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void getUnreadNotifications_shouldReturnUnreadNotifications() {

        notification.setRead(false);

        when(notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                ))
                .thenReturn(
                        List.of(notification)
                );

        List<NotificationResponse> result =
                notificationService.getUnreadNotifications(
                        USER_ID
                );

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertFalse(
                result.get(0).isRead()
        );

        verify(notificationRepository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                );
    }


    // =========================================================
    // 9. GET UNREAD NOTIFICATIONS - EMPTY
    // =========================================================

    @Test
    void getUnreadNotifications_whenNoneExist_shouldReturnEmptyList() {

        when(notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                ))
                .thenReturn(
                        List.of()
                );

        List<NotificationResponse> result =
                notificationService.getUnreadNotifications(
                        USER_ID
                );

        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(notificationRepository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                );
    }


    // =========================================================
    // 10. GET UNREAD COUNT
    // =========================================================

    @Test
    void getUnreadCount_shouldReturnCount() {

        when(notificationRepository
                .countByUserIdAndReadFalse(USER_ID))
                .thenReturn(3L);

        long result =
                notificationService.getUnreadCount(
                        USER_ID
                );

        assertEquals(
                3L,
                result
        );

        verify(notificationRepository)
                .countByUserIdAndReadFalse(USER_ID);
    }


    // =========================================================
    // 11. MARK AS READ
    // =========================================================

    @Test
    void markAsRead_shouldMarkNotificationAsRead() {

        notification.setRead(false);

        when(notificationRepository.findById(
                NOTIFICATION_ID
        )).thenReturn(
                Optional.of(notification)
        );

        when(notificationRepository.save(
                any(Notification.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationResponse result =
                notificationService.markAsRead(
                        NOTIFICATION_ID
                );

        assertNotNull(result);

        assertTrue(
                notification.isRead()
        );

        assertTrue(
                result.isRead()
        );

        verify(notificationRepository)
                .findById(NOTIFICATION_ID);

        verify(notificationRepository)
                .save(notification);
    }


    // =========================================================
    // 12. MARK AS READ - NOT FOUND
    // =========================================================

    @Test
    void markAsRead_whenNotFound_shouldThrowException() {

        when(notificationRepository.findById(
                NOTIFICATION_ID
        )).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> notificationService.markAsRead(
                                NOTIFICATION_ID
                        )
                );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationRepository)
                .findById(NOTIFICATION_ID);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    // =========================================================
    // 13. MARK ALL AS READ
    // =========================================================

    @Test
    void markAllAsRead_shouldMarkAllUnreadNotificationsAsRead() {

        Notification notification2 =
                new Notification(
                        "Notification 2",
                        "Message 2",
                        NotificationType.WORKFLOW_COMPLETED,
                        NotificationPriority.SUCCESS,
                        user,
                        workflow
                );

        notification.setRead(false);
        notification2.setRead(false);

        List<Notification> notifications =
                List.of(
                        notification,
                        notification2
                );

        when(notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                ))
                .thenReturn(notifications);

        notificationService.markAllAsRead(
                USER_ID
        );

        assertTrue(
                notification.isRead()
        );

        assertTrue(
                notification2.isRead()
        );

        verify(notificationRepository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                );

        verify(notificationRepository)
                .saveAll(notifications);
    }


    // =========================================================
    // 14. MARK ALL AS READ - NO NOTIFICATIONS
    // =========================================================

    @Test
    void markAllAsRead_whenNoneExist_shouldSaveEmptyList() {

        when(notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                ))
                .thenReturn(
                        List.of()
                );

        notificationService.markAllAsRead(
                USER_ID
        );

        verify(notificationRepository)
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        USER_ID
                );

        verify(notificationRepository)
                .saveAll(List.of());
    }


    // =========================================================
    // 15. DELETE NOTIFICATION
    // =========================================================

    @Test
    void deleteNotification_shouldDeleteExistingNotification() {

        when(notificationRepository.existsById(
                NOTIFICATION_ID
        )).thenReturn(true);

        notificationService.deleteNotification(
                NOTIFICATION_ID
        );

        verify(notificationRepository)
                .existsById(NOTIFICATION_ID);

        verify(notificationRepository)
                .deleteById(NOTIFICATION_ID);
    }


    // =========================================================
    // 16. DELETE NOTIFICATION - NOT FOUND
    // =========================================================

    @Test
    void deleteNotification_whenNotFound_shouldThrowException() {

        when(notificationRepository.existsById(
                NOTIFICATION_ID
        )).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> notificationService.deleteNotification(
                                NOTIFICATION_ID
                        )
                );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationRepository)
                .existsById(NOTIFICATION_ID);

        verify(notificationRepository, never())
                .deleteById(anyLong());
    }


    // =========================================================
    // 17. NOTIFICATION RESPONSE - READ STATE
    // =========================================================

    @Test
    void createNotification_shouldInitiallyBeUnread() {

        when(notificationRepository.save(
                any(Notification.class)
        )).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        NotificationResponse result =
                notificationService.createNotification(
                        "Unread Test",
                        "Unread notification",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        user,
                        workflow
                );

        assertFalse(
                result.isRead()
        );
    }


    // =========================================================
    // 18. GET NOTIFICATIONS - WORKFLOW DATA
    // =========================================================

    @Test
    void getNotifications_shouldMapWorkflowData() {

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(
                        List.of(notification)
                );

        List<NotificationResponse> result =
                notificationService.getNotifications(
                        USER_ID
                );

        assertEquals(
                workflow.getId(),
                result.get(0).getWorkflowId()
        );

        assertEquals(
                workflow.getName(),
                result.get(0).getWorkflowName()
        );
    }
}
