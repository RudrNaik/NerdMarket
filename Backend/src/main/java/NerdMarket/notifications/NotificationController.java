package NerdMarket.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    //Admin creates notification - they can set a scheduled time to send in future
    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Map<String, Object> body) {
        try {
            Long senderId = Long.valueOf(body.get("senderId").toString());
            String type = (String) body.get("type");
            String title = (String) body.get("title");
            String message = (String) body.get("message");

            LocalDateTime scheduledAt = null;
            if (body.containsKey("scheduledAt") && body.get("scheduledAt") != null) {
                scheduledAt = LocalDateTime.parse(body.get("scheduledAt").toString());
            }
            Notification notification = notificationService.createNotification(senderId, type, title, message, scheduledAt);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Get all notification for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotifications(@PathVariable Long userId) {
        try {
            List<UserNotification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Get unread notification count for a user
    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Mark a notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            UserNotification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Delete a notification for user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteUserNotification(id);
            return ResponseEntity.ok("Notification deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
