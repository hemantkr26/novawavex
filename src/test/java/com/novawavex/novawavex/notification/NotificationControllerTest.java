
package com.novawavex.novawavex.notification;

import com.novawavex.novawavex.notification.dto.NotificationResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse notification;

    @BeforeEach
    void setUp() {

        notification = new NotificationResponse(
                1L,
                "Workflow started",
                "Test Workflow was started successfully.",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.SUCCESS,
                false,
                LocalDateTime.now(),
                10L,
                "Test Workflow"
        );
    }


    // =========================================================
    // 1. GET ALL NOTIFICATIONS
    // =========================================================

    @Test
    void getNotifications_shouldReturnNotifications() {

        Long userId = 1L;

        when(notificationService.getNotifications(userId))
                .thenReturn(List.of(notification));

        ResponseEntity<List<NotificationResponse>> response =
                notificationController.getNotifications(userId);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                1,
                response.getBody().size()
        );

        assertEquals(
                notification,
                response.getBody().get(0)
        );

        verify(notificationService)
                .getNotifications(userId);
    }


    // =========================================================
    // 2. GET ALL NOTIFICATIONS - EMPTY
    // =========================================================

    @Test
    void getNotifications_whenEmpty_shouldReturnEmptyList() {

        Long userId = 1L;

        when(notificationService.getNotifications(userId))
                .thenReturn(List.of());

        ResponseEntity<List<NotificationResponse>> response =
                notificationController.getNotifications(userId);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().isEmpty()
        );

        verify(notificationService)
                .getNotifications(userId);
    }


    // =========================================================
    // 3. GET UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void getUnreadNotifications_shouldReturnUnreadNotifications() {

        Long userId = 1L;

        when(
                notificationService.getUnreadNotifications(userId)
        ).thenReturn(List.of(notification));

        ResponseEntity<List<NotificationResponse>> response =
                notificationController
                        .getUnreadNotifications(userId);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                1,
                response.getBody().size()
        );

        assertEquals(
                notification,
                response.getBody().get(0)
        );

        verify(notificationService)
                .getUnreadNotifications(userId);
    }


    // =========================================================
    // 4. GET UNREAD NOTIFICATIONS - EMPTY
    // =========================================================

    @Test
    void getUnreadNotifications_whenEmpty_shouldReturnEmptyList() {

        Long userId = 1L;

        when(
                notificationService.getUnreadNotifications(userId)
        ).thenReturn(List.of());

        ResponseEntity<List<NotificationResponse>> response =
                notificationController
                        .getUnreadNotifications(userId);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().isEmpty()
        );

        verify(notificationService)
                .getUnreadNotifications(userId);
    }


    // =========================================================
    // 5. GET UNREAD COUNT
    // =========================================================

    @Test
    void getUnreadCount_shouldReturnUnreadCount() {

        Long userId = 1L;

        when(
                notificationService.getUnreadCount(userId)
        ).thenReturn(5L);

        ResponseEntity<Long> response =
                notificationController.getUnreadCount(userId);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                5L,
                response.getBody()
        );

        verify(notificationService)
                .getUnreadCount(userId);
    }


    // =========================================================
    // 6. GET UNREAD COUNT - ZERO
    // =========================================================

    @Test
    void getUnreadCount_whenNoneUnread_shouldReturnZero() {

        Long userId = 1L;

        when(
                notificationService.getUnreadCount(userId)
        ).thenReturn(0L);

        ResponseEntity<Long> response =
                notificationController.getUnreadCount(userId);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                0L,
                response.getBody()
        );

        verify(notificationService)
                .getUnreadCount(userId);
    }


    // =========================================================
    // 7. MARK NOTIFICATION AS READ
    // =========================================================

    @Test
    void markAsRead_shouldReturnUpdatedNotification() {

        Long notificationId = 1L;

        NotificationResponse readNotification =
                new NotificationResponse(
                        1L,
                        "Workflow started",
                        "Test Workflow was started successfully.",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.SUCCESS,
                        true,
                        notification.getCreatedAt(),
                        10L,
                        "Test Workflow"
                );

        when(
                notificationService.markAsRead(notificationId)
        ).thenReturn(readNotification);

        ResponseEntity<NotificationResponse> response =
                notificationController.markAsRead(
                        notificationId
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        assertEquals(
                readNotification,
                response.getBody()
        );

        assertTrue(
                response.getBody().isRead()
        );

        verify(notificationService)
                .markAsRead(notificationId);
    }


    // =========================================================
    // 8. MARK NOTIFICATION AS READ - NOT FOUND
    // =========================================================

    @Test
    void markAsRead_whenNotificationNotFound_shouldPropagateException() {

        Long notificationId = 999L;

        when(
                notificationService.markAsRead(notificationId)
        ).thenThrow(
                new IllegalArgumentException(
                        "Notification not found"
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> notificationController.markAsRead(
                                notificationId
                        )
                );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationService)
                .markAsRead(notificationId);
    }


    // =========================================================
    // 9. MARK ALL AS READ
    // =========================================================

    @Test
    void markAllAsRead_shouldReturnNoContent() {

        Long userId = 1L;

        doNothing()
                .when(notificationService)
                .markAllAsRead(userId);

        ResponseEntity<Void> response =
                notificationController.markAllAsRead(userId);

        assertEquals(
                204,
                response.getStatusCode().value()
        );

        assertNull(
                response.getBody()
        );

        verify(notificationService)
                .markAllAsRead(userId);
    }


    // =========================================================
    // 10. DELETE NOTIFICATION
    // =========================================================

    @Test
    void deleteNotification_shouldReturnNoContent() {

        Long notificationId = 1L;

        doNothing()
                .when(notificationService)
                .deleteNotification(notificationId);

        ResponseEntity<Void> response =
                notificationController.deleteNotification(
                        notificationId
                );

        assertEquals(
                204,
                response.getStatusCode().value()
        );

        assertNull(
                response.getBody()
        );

        verify(notificationService)
                .deleteNotification(notificationId);
    }


    // =========================================================
    // 11. DELETE NOTIFICATION - NOT FOUND
    // =========================================================

    @Test
    void deleteNotification_whenNotificationNotFound_shouldPropagateException() {

        Long notificationId = 999L;

        doThrow(
                new IllegalArgumentException(
                        "Notification not found"
                )
        ).when(notificationService)
                .deleteNotification(notificationId);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> notificationController
                                .deleteNotification(
                                        notificationId
                                )
                );

        assertEquals(
                "Notification not found",
                exception.getMessage()
        );

        verify(notificationService)
                .deleteNotification(notificationId);
    }


    // =========================================================
    // 12. SERVICE INTERACTION VERIFICATION
    // =========================================================

    @Test
    void controllerMethods_shouldDelegateToNotificationService() {

        Long userId = 1L;
        Long notificationId = 1L;

        when(
                notificationService.getNotifications(userId)
        ).thenReturn(List.of());

        when(
                notificationService.getUnreadNotifications(userId)
        ).thenReturn(List.of());

        when(
                notificationService.getUnreadCount(userId)
        ).thenReturn(0L);

        when(
                notificationService.markAsRead(notificationId)
        ).thenReturn(notification);

        doNothing()
                .when(notificationService)
                .markAllAsRead(userId);

        doNothing()
                .when(notificationService)
                .deleteNotification(notificationId);

        notificationController.getNotifications(userId);

        notificationController.getUnreadNotifications(userId);

        notificationController.getUnreadCount(userId);

        notificationController.markAsRead(notificationId);

        notificationController.markAllAsRead(userId);

        notificationController.deleteNotification(notificationId);

        verify(notificationService)
                .getNotifications(userId);

        verify(notificationService)
                .getUnreadNotifications(userId);

        verify(notificationService)
                .getUnreadCount(userId);

        verify(notificationService)
                .markAsRead(notificationId);

        verify(notificationService)
                .markAllAsRead(userId);

        verify(notificationService)
                .deleteNotification(notificationId);
    }
}
