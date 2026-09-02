package com.novawavex.novawavex.notification;

import com.novawavex.novawavex.notification.dto.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    // =========================================
    // GET ALL NOTIFICATIONS
    // =========================================

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.getNotifications(userId)
        );
    }

    // =========================================
    // GET UNREAD NOTIFICATIONS
    // =========================================

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    // =========================================
    // GET UNREAD COUNT
    // =========================================

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId)
        );
    }

    // =========================================
    // MARK NOTIFICATION AS READ
    // =========================================

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        notificationId
                )
        );
    }

    // =========================================
    // MARK ALL AS READ
    // =========================================

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam Long userId) {

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }

    // =========================================
    // DELETE NOTIFICATION
    // =========================================

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        notificationService.deleteNotification(
                notificationId
        );

        return ResponseEntity.noContent().build();
    }
}