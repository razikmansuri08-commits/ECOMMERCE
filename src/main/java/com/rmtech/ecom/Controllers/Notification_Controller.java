package com.rmtech.ecom.Controllers;

import com.rmtech.ecom.DTOS.NotificationDto;
import com.rmtech.ecom.Service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/notifications")
public class Notification_Controller {

    private final NotificationService notificationService;

    public Notification_Controller(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get user ID from username - you might need to add a method in User_Service or User_Repo
        // For now, we'll need to get it differently
        Page<NotificationDto> notifications = notificationService
                .getUserNotifications(getUserIdByUsername(username), pageable);
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<NotificationDto> notifications = notificationService
                .getUnreadNotifications(getUserIdByUsername(username));
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Long count = notificationService.getUnreadCount(getUserIdByUsername(username));
        
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        int count = notificationService.markAllAsRead(getUserIdByUsername(username));
        return ResponseEntity.ok("Marked " + count + " notifications as read");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdByUsername(String username) {
        // This is a helper method - you might want to add this to User_Service
        // or create a dedicated method in User_Repo
        throw new RuntimeException("getUserById method not implemented. Add to User_Service or use SecurityContext user details.");
    }
}
