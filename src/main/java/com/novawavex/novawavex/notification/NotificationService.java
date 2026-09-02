package com.novawavex.novawavex.notification;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.notification.dto.NotificationResponse;
import com.novawavex.novawavex.workflow.Workflow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {

        this.notificationRepository = notificationRepository;
    }

    // =========================================
    // CREATE NOTIFICATION
    // =========================================

    public NotificationResponse createNotification(
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority,
            User user,
            Workflow workflow) {

        Notification notification = new Notification(
                title,
                message,
                type,
                priority,
                user,
                workflow
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        return NotificationResponse.fromEntity(
                savedNotification
        );
    }

    // =========================================
    // DETACH NOTIFICATIONS FROM WORKFLOW
    // =========================================

    public void detachNotificationsFromWorkflow(
            Long workflowId) {

        List<Notification> notifications =
                notificationRepository
                        .findByWorkflowId(workflowId);

        notifications.forEach(
                notification ->
                        notification.setWorkflow(null)
        );

        notificationRepository.saveAll(notifications);
    }

    // =========================================
    // GET ALL NOTIFICATIONS
    // =========================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    // =========================================
    // GET UNREAD NOTIFICATIONS
    // =========================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(
            Long userId) {

        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    // =========================================
    // GET UNREAD COUNT
    // =========================================

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }

    // =========================================
    // MARK NOTIFICATION AS READ
    // =========================================

    public NotificationResponse markAsRead(
            Long notificationId) {

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        notification.setRead(true);

        Notification savedNotification =
                notificationRepository.save(notification);

        return NotificationResponse.fromEntity(
                savedNotification
        );
    }

    // =========================================
    // MARK ALL AS READ
    // =========================================

    public void markAllAsRead(Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                userId
                        );

        notifications.forEach(
                notification ->
                        notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }

    // =========================================
    // DELETE NOTIFICATION
    // =========================================

    public void deleteNotification(
            Long notificationId) {

        if (!notificationRepository.existsById(
                notificationId)) {

            throw new IllegalArgumentException(
                    "Notification not found"
            );
        }

        notificationRepository.deleteById(
                notificationId
        );
    }
}