package com.sujal.itsm.core.notification.controller;

import com.sujal.itsm.core.notification.model.Notification;
import com.sujal.itsm.core.notification.service.NotificationService;
import com.sujal.itsm.core.security.CurrentUserService;
import com.sujal.itsm.core.user.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // ✅ CHANGED from @RestController
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller // ✅ MUST BE @Controller TO RENDER HTML TEMPLATES
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @ResponseBody // ✅ Keep this for JSON endpoints
    public ResponseEntity<List<Notification>> getRecentNotifications() {
        AppUser user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(notificationService.getRecentNotifications(user.getId()));
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount() {
        AppUser user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        AppUser user = currentUserService.getCurrentUser();
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Void> markAllAsRead() {
        AppUser user = currentUserService.getCurrentUser();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }

    // ✅ THIS NOW RETURNS THE HTML VIEW
    @GetMapping("/history")
    public String notificationHistory(Model model) {
        AppUser user = currentUserService.getCurrentUser();
        List<Notification> notifications = notificationService.getAllNotifications(user.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("pageTitle", "Notification History");
        return "notifications/history";
    }
}