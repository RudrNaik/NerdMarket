package NerdMarket.notifications;

import NerdMarket.users.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Users sender;

    @Column(name = "sender_id", insertable = false, updatable = false)
    private Long senderId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private Users recipient;

    @Column(name = "recipient_id", insertable = false, updatable = false)
    private Long recipientId;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
