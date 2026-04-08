package NerdMarket.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationWebSocketController {

    @Autowired
    private NotificationService notificationService;

    // Client sends to: /app/notifications/read/{id}
    @MessageMapping("/notifications/read/{id}")
    public void markNotificationRead(@DestinationVariable Long id) {
        notificationService.markAsRead(id);
    }
}
