package NerdMarket.notifications;

import NerdMarket.users.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_notification")
@Data
@NoArgsConstructor
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "notification_id", insertable = false, updatable = false)
    private Long notificationId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private boolean isRead = false;
    private LocalDateTime readAt;

    public String getNotificationType() {
        return notification != null ? notification.getType() : null;
    }

    public String getNotificationTitle() {
        return notification != null ? notification.getTitle() : null;
    }

    public String getNotificationMessage() {
        return notification != null ? notification.getMessage() : null;
    }

    public Long getNotificationSenderId() {
        return notification != null ? notification.getSenderId() : null;
    }

    public LocalDateTime getNotificationCreatedAt() {
        return notification != null ? notification.getCreatedAt() : null;
    }
}

