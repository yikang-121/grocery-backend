package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.entity.AdminNotification;
import com.grocery.grocerybackend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get all notifications, newest first.
     */
    @GetMapping
    public List<AdminNotification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    /**
     * Get the count of unread notifications (for badge display).
     */
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount() {
        return Map.of("count", notificationService.getUnreadCount());
    }

    /**
     * Mark a single notification as read.
     */
    @PutMapping("/{id}/read")
    public Map<String, String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Map.of("status", "ok");
    }

    /**
     * Mark all notifications as read.
     */
    @PutMapping("/read-all")
    public Map<String, String> markAllAsRead() {
        notificationService.markAllAsRead();
        return Map.of("status", "ok");
    }
}
