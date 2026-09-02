package com.novawavex.novawavex.notification.dto;

import com.novawavex.novawavex.notification.Notification;
import com.novawavex.novawavex.notification.NotificationPriority;
import com.novawavex.novawavex.notification.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private NotificationType type;

    private NotificationPriority priority;

    private boolean read;

    private LocalDateTime createdAt;

    private Long workflowId;

    private String workflowName;

    public NotificationResponse() {
    }

    public NotificationResponse(
            Long id,
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority,
            boolean read,
            LocalDateTime createdAt,
            Long workflowId,
            String workflowName) {

        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.read = read;
        this.createdAt = createdAt;
        this.workflowId = workflowId;
        this.workflowName = workflowName;
    }

    public static NotificationResponse fromEntity(
            Notification notification) {

        Long workflowId = null;
        String workflowName = null;

        if (notification.getWorkflow() != null) {
            workflowId = notification.getWorkflow().getId();
            workflowName = notification.getWorkflow().getName();
        }

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getPriority(),
                notification.isRead(),
                notification.getCreatedAt(),
                workflowId,
                workflowName
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }
}