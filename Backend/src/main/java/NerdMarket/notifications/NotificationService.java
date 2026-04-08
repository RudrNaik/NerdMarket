package NerdMarket.notifications;

import NerdMarket.users.UserRepository;
import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //Admin creates notification (broadcasted to all users)
    //If scheduledAt is set, it will be sent later by the schedular.
    //If scheduledAt is null then it gets sent right away.
    public Notification createNotification(Long senderId, String type, String title, String message, LocalDateTime scheduledAt) {
        Users sender = userRepository.findById(senderId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!sender.isAdmin()) {
            throw new RuntimeException("Only admins can create notifications");
        }

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setScheduledAt(scheduledAt);

        if(scheduledAt == null) {
            notification.setSentAt(LocalDateTime.now());
        }

        notificationRepository.save(notification);

        //If no scheduled time, deliver immediately to all users
        if (scheduledAt == null) {
            deliverToAllUsers(notification);
            //push to all connected users via WebSocket
            messagingTemplate.convertAndSend("/topic/notifications", "[" + type + "] " + title + ": " + message);
        }

        return notification;
    }

    //Targeted notification for specific user
    public Notification createSystemNotification(Long recipientId, String title, String message) {
        Users recipient = userRepository.findById(recipientId).orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setType("CARD_UPDATE");
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);

        //Create the user specific delivery record
        UserNotification userNotification = new UserNotification();
        userNotification.setNotification(notification);
        userNotification.setUser(recipient);
        userNotificationRepository.save(userNotification);

        //Push to the specific user via WebSocket if they're connected
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getUsername(), "[CARD_UPDATE] " + title + ": " + message);

        return notification;
    }

    //Push a scheduled notification to all connected users via WebSocket
    public void broadcastScheduledNotification(Notification notification) {
        messagingTemplate.convertAndSend("/topic/notifications",
                "[" + notification.getType() + "] " + notification.getTitle() + ": " + notification.getMessage());
    }

    //Deliver a notification to all active users
    public void deliverToAllUsers(Notification notification) {
        List<Users> allUsers = userRepository.findAll();

        for (Users user : allUsers) {
            if (user.isActive()) {
                UserNotification userNotification = new UserNotification();
                userNotification.setNotification(notification);
                userNotification.setUser(user);
                userNotificationRepository.save(userNotification);
            }
        }
    }

    //get all notifications for a user
    public List<UserNotification> getUserNotifications(Long userId) {
        return userNotificationRepository.findByUserIdOrderByIdDesc(userId);
    }

    //Get unread notifications for user
    public List<UserNotification> getUnreadNotifications(Long userId) {
        return userNotificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    //Get unread count for a user
    public long getUnreadCount(Long userId) {
        return userNotificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    //Mark notification as read for a user
    public UserNotification markAsRead(Long userNotificationId) {
        UserNotification userNotification = userNotificationRepository.findById(userNotificationId).orElseThrow(() -> new RuntimeException("User not found"));
        userNotification.setRead(true);
        userNotification.setReadAt(LocalDateTime.now());
        return userNotificationRepository.save(userNotification);
    }

    //Delete notification for user
    public void deleteUserNotification(Long userNotificationId) {
        if (!userNotificationRepository.existsById(userNotificationId)) {
            throw new RuntimeException("Notification not found");
        }
        userNotificationRepository.deleteById(userNotificationId);
    }

}
