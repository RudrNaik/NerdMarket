package NerdMarket.binders;

import NerdMarket.market.Market;
import NerdMarket.users.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "UserBinders",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}),
    indexes = {
        @Index(name = "idx_user_binder_user_id", columnList = "user_id"),
        @Index(name = "idx_user_binder_card_id", columnList = "card_id")
    }
)
@Data
@NoArgsConstructor
public class Binders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Market card;

    @Column(name = "card_id", insertable = false, updatable = false)
    private Long cardId;

    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}
