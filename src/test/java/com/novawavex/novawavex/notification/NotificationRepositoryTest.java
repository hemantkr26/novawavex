
package com.novawavex.novawavex.notification;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.workflow.Workflow;
import com.novawavex.novawavex.workflow.WorkflowStatus;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.novawavex.novawavex.repository.UserRepository userRepository;

    @Autowired
    private com.novawavex.novawavex.workflow.WorkflowRepository workflowRepository;


    // =========================================================
    // 1. SAVE NOTIFICATION
    // =========================================================

    @Test
    void saveNotification_shouldPersistNotification() {

        User user = new User(
                "Notification Repository User",
                "notification-repo-save@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Workflow workflow = new Workflow(
                "Notification Repository Workflow",
                "Repository workflow test",
                WorkflowStatus.ACTIVE,
                savedUser.getEmail()
        );

        Workflow savedWorkflow =
                workflowRepository.save(workflow);

        Notification notification =
                new Notification(
                        "Test Notification",
                        "Test notification message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.SUCCESS,
                        savedUser,
                        savedWorkflow
                );

        Notification savedNotification =
                notificationRepository.save(notification);

        assertNotNull(
                savedNotification.getId()
        );

        assertEquals(
                "Test Notification",
                savedNotification.getTitle()
        );

        assertEquals(
                "Test notification message",
                savedNotification.getMessage()
        );

        assertEquals(
                NotificationType.WORKFLOW_STARTED,
                savedNotification.getType()
        );

        assertEquals(
                NotificationPriority.SUCCESS,
                savedNotification.getPriority()
        );

        assertFalse(
                savedNotification.isRead()
        );

        assertEquals(
                savedUser.getId(),
                savedNotification.getUser().getId()
        );

        assertEquals(
                savedWorkflow.getId(),
                savedNotification.getWorkflow().getId()
        );
    }


    // =========================================================
    // 2. FIND BY USER
    // =========================================================

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnUserNotifications() {

        User user = new User(
                "Notification User Query",
                "notification-repo-user@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Notification notification1 =
                new Notification(
                        "Notification One",
                        "Message One",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        null
                );

        Notification notification2 =
                new Notification(
                        "Notification Two",
                        "Message Two",
                        NotificationType.WORKFLOW_COMPLETED,
                        NotificationPriority.SUCCESS,
                        savedUser,
                        null
                );

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        List<Notification> result =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                savedUser.getId()
                        );

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream()
                        .allMatch(
                                notification ->
                                        notification.getUser()
                                                .getId()
                                                .equals(
                                                        savedUser.getId()
                                                )
                        )
        );
    }


    // =========================================================
    // 3. FIND BY USER - OTHER USER IS NOT RETURNED
    // =========================================================

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnOnlyRequestedUsersNotifications() {

        User userA = new User(
                "Notification User A",
                "notification-repo-user-a@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User userB = new User(
                "Notification User B",
                "notification-repo-user-b@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUserA =
                userRepository.save(userA);

        User savedUserB =
                userRepository.save(userB);

        notificationRepository.save(
                new Notification(
                        "User A Notification",
                        "User A message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUserA,
                        null
                )
        );

        notificationRepository.save(
                new Notification(
                        "User B Notification",
                        "User B message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUserB,
                        null
                )
        );

        List<Notification> result =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                savedUserA.getId()
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                savedUserA.getId(),
                result.get(0)
                        .getUser()
                        .getId()
        );

        assertEquals(
                "User A Notification",
                result.get(0).getTitle()
        );
    }


    // =========================================================
    // 4. FIND UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void findByUserIdAndReadFalseOrderByCreatedAtDesc_shouldReturnUnreadNotifications() {

        User user = new User(
                "Unread Notification User",
                "notification-repo-unread@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Notification unreadNotification =
                new Notification(
                        "Unread Notification",
                        "Unread message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        null
                );

        Notification readNotification =
                new Notification(
                        "Read Notification",
                        "Read message",
                        NotificationType.WORKFLOW_COMPLETED,
                        NotificationPriority.SUCCESS,
                        savedUser,
                        null
                );

        readNotification.setRead(true);

        notificationRepository.save(
                unreadNotification
        );

        notificationRepository.save(
                readNotification
        );

        List<Notification> result =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                savedUser.getId()
                        );

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Unread Notification",
                result.get(0).getTitle()
        );

        assertFalse(
                result.get(0).isRead()
        );
    }


    // =========================================================
    // 5. FIND UNREAD - ALL READ
    // =========================================================

    @Test
    void findByUserIdAndReadFalseOrderByCreatedAtDesc_whenAllAreRead_shouldReturnEmptyList() {

        User user = new User(
                "All Read Notification User",
                "notification-repo-all-read@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Notification notification =
                new Notification(
                        "Already Read",
                        "Already read message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        null
                );

        notification.setRead(true);

        notificationRepository.save(notification);

        List<Notification> result =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                savedUser.getId()
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 6. COUNT UNREAD
    // =========================================================

    @Test
    void countByUserIdAndReadFalse_shouldReturnUnreadCount() {

        User user = new User(
                "Unread Count User",
                "notification-repo-count@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        notificationRepository.save(
                new Notification(
                        "Unread One",
                        "Message One",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        null
                )
        );

        notificationRepository.save(
                new Notification(
                        "Unread Two",
                        "Message Two",
                        NotificationType.WORKFLOW_COMPLETED,
                        NotificationPriority.SUCCESS,
                        savedUser,
                        null
                )
        );

        Notification readNotification =
                new Notification(
                        "Read One",
                        "Read message",
                        NotificationType.WORKFLOW_FAILED,
                        NotificationPriority.ERROR,
                        savedUser,
                        null
                );

        readNotification.setRead(true);

        notificationRepository.save(
                readNotification
        );

        long count =
                notificationRepository
                        .countByUserIdAndReadFalse(
                                savedUser.getId()
                        );

        assertEquals(
                2L,
                count
        );
    }


    // =========================================================
    // 7. COUNT UNREAD - AFTER MARKING READ
    // =========================================================

    @Test
    void countByUserIdAndReadFalse_shouldDecreaseWhenNotificationIsMarkedRead() {

        User user = new User(
                "Unread Decrease User",
                "notification-repo-decrease@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Notification notification1 =
                notificationRepository.save(
                        new Notification(
                                "Unread One",
                                "Message One",
                                NotificationType.WORKFLOW_STARTED,
                                NotificationPriority.INFO,
                                savedUser,
                                null
                        )
                );

        notificationRepository.save(
                new Notification(
                        "Unread Two",
                        "Message Two",
                        NotificationType.WORKFLOW_COMPLETED,
                        NotificationPriority.SUCCESS,
                        savedUser,
                        null
                )
        );

        long initialCount =
                notificationRepository
                        .countByUserIdAndReadFalse(
                                savedUser.getId()
                        );

        assertEquals(
                2L,
                initialCount
        );

        notification1.setRead(true);

        notificationRepository.save(
                notification1
        );

        long updatedCount =
                notificationRepository
                        .countByUserIdAndReadFalse(
                                savedUser.getId()
                        );

        assertEquals(
                1L,
                updatedCount
        );
    }


    // =========================================================
    // 8. FIND BY WORKFLOW
    // =========================================================

    @Test
    void findByWorkflowId_shouldReturnWorkflowNotifications() {

        User user = new User(
                "Workflow Notification User",
                "notification-repo-workflow@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Workflow workflow = new Workflow(
                "Notification Workflow",
                "Workflow notification test",
                WorkflowStatus.ACTIVE,
                savedUser.getEmail()
        );

        Workflow savedWorkflow =
                workflowRepository.save(workflow);

        notificationRepository.save(
                new Notification(
                        "Workflow Notification One",
                        "Message One",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        savedWorkflow
                )
        );

        notificationRepository.save(
                new Notification(
                        "Workflow Notification Two",
                        "Message Two",
                        NotificationType.WORKFLOW_COMPLETED,
                        NotificationPriority.SUCCESS,
                        savedUser,
                        savedWorkflow
                )
        );

        List<Notification> result =
                notificationRepository
                        .findByWorkflowId(
                                savedWorkflow.getId()
                        );

        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream()
                        .allMatch(
                                notification ->
                                        notification.getWorkflow()
                                                .getId()
                                                .equals(
                                                        savedWorkflow.getId()
                                                )
                        )
        );
    }


    // =========================================================
    // 9. FIND BY WORKFLOW - OTHER WORKFLOW NOT RETURNED
    // =========================================================

    @Test
    void findByWorkflowId_shouldReturnOnlyRequestedWorkflowNotifications() {

        User user = new User(
                "Workflow Filter User",
                "notification-repo-workflow-filter@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Workflow workflowA = new Workflow(
                "Workflow A",
                "Workflow A",
                WorkflowStatus.ACTIVE,
                savedUser.getEmail()
        );

        Workflow workflowB = new Workflow(
                "Workflow B",
                "Workflow B",
                WorkflowStatus.ACTIVE,
                savedUser.getEmail()
        );

        Workflow savedWorkflowA =
                workflowRepository.save(workflowA);

        Workflow savedWorkflowB =
                workflowRepository.save(workflowB);

        notificationRepository.save(
                new Notification(
                        "Workflow A Notification",
                        "Workflow A message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        savedWorkflowA
                )
        );

        notificationRepository.save(
                new Notification(
                        "Workflow B Notification",
                        "Workflow B message",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        savedUser,
                        savedWorkflowB
                )
        );

        List<Notification> result =
                notificationRepository
                        .findByWorkflowId(
                                savedWorkflowA.getId()
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Workflow A Notification",
                result.get(0).getTitle()
        );

        assertEquals(
                savedWorkflowA.getId(),
                result.get(0)
                        .getWorkflow()
                        .getId()
        );
    }


    // =========================================================
    // 10. FIND BY MISSING USER
    // =========================================================

    @Test
    void findByUserId_whenUserDoesNotExist_shouldReturnEmptyList() {

        List<Notification> result =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                999999L
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 11. FIND UNREAD BY MISSING USER
    // =========================================================

    @Test
    void findUnreadByUserId_whenUserDoesNotExist_shouldReturnEmptyList() {

        List<Notification> result =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                999999L
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 12. COUNT UNREAD BY MISSING USER
    // =========================================================

    @Test
    void countUnreadByUserId_whenUserDoesNotExist_shouldReturnZero() {

        long count =
                notificationRepository
                        .countByUserIdAndReadFalse(
                                999999L
                        );

        assertEquals(
                0L,
                count
        );
    }


    // =========================================================
    // 13. FIND BY MISSING WORKFLOW
    // =========================================================

    @Test
    void findByWorkflowId_whenWorkflowDoesNotExist_shouldReturnEmptyList() {

        List<Notification> result =
                notificationRepository
                        .findByWorkflowId(
                                999999L
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 14. FIND BY ID
    // =========================================================

    @Test
    void findById_whenNotificationExists_shouldReturnNotification() {

        User user = new User(
                "Find Notification User",
                "notification-repo-find@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Notification notification =
                notificationRepository.save(
                        new Notification(
                                "Find Test Notification",
                                "Find test message",
                                NotificationType.WORKFLOW_STARTED,
                                NotificationPriority.INFO,
                                savedUser,
                                null
                        )
                );

        Optional<Notification> result =
                notificationRepository.findById(
                        notification.getId()
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                notification.getId(),
                result.get().getId()
        );

        assertEquals(
                "Find Test Notification",
                result.get().getTitle()
        );
    }


    // =========================================================
    // 15. FIND BY ID - NOT FOUND
    // =========================================================

    @Test
    void findById_whenNotificationDoesNotExist_shouldReturnEmpty() {

        Optional<Notification> result =
                notificationRepository.findById(
                        999999L
                );

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 16. DELETE NOTIFICATION
    // =========================================================

    @Test
    void deleteById_shouldDeleteNotification() {

        User user = new User(
                "Delete Notification User",
                "notification-repo-delete@novawavex.com",
                "encodedPassword",
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        Notification notification =
                notificationRepository.save(
                        new Notification(
                                "Delete Notification",
                                "Delete test message",
                                NotificationType.WORKFLOW_STARTED,
                                NotificationPriority.INFO,
                                savedUser,
                                null
                        )
                );

        Long notificationId =
                notification.getId();

        assertTrue(
                notificationRepository
                        .existsById(notificationId)
        );

        notificationRepository.deleteById(
                notificationId
        );

        notificationRepository.flush();

        assertFalse(
                notificationRepository
                        .existsById(notificationId)
        );
    }
}
