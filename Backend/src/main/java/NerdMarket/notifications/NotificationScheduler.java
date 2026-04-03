package NerdMarket.notifications;

import NerdMarket.binders.Binders;
import NerdMarket.binders.BinderRepository;
import NerdMarket.prices.PriceTracking;
import NerdMarket.prices.PriceTrackingRepository;
import NerdMarket.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class NotificationScheduler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private BinderRepository binderRepository;

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    private final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    //Checks every minute for scheduled notifications that are ready to send.
    @Scheduled(fixedRate = 60000)
    public void processScheduledNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findByScheduledAtBeforeAndSentAtIsNull(LocalDateTime.now());
        for (Notification notification : pendingNotifications) {
            logger.info("[Scheduler] Sending scheduled notification: " + notification.getTitle());
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            notificationService.deliverToAllUsers(notification);
            NotificationSocket.broadcast("[" + notification.getType() + "] " + notification.getTitle() + ": " + notification.getMessage());
        }
    }

    //Runs daily at 8:00 AM - sends card price update notifications to users based on cards in their binder.
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyCardUpdates() {
        logger.info("[Scheduler] Starting daily card update notifications");

        List<Binders> allBinderEntries = binderRepository.findAll();

        for (Binders binder : allBinderEntries) {
            Users user = binder.getUser();
            Long cardId = binder.getCardId();
            String cardName = binder.getCard().getCardName();

            // Get the two most recent price records for this card
            List<PriceTracking> recentPrices = priceTrackingRepository.findTop2ByCardIdOrderByRecordedAtDesc(cardId);

            if (recentPrices.size() < 2) {
                continue;
            }

            double currentPrice = recentPrices.get(0).getPrice();
            double previousPrice = recentPrices.get(1).getPrice();

            if (previousPrice == 0) {
                continue;
            }

            double changePercent = ((currentPrice - previousPrice) / previousPrice) * 100;

            // Only notify if there's a meaningful change (more than 1%)
            if (Math.abs(changePercent) > 1.0) {
                String direction = changePercent > 0 ? "up" : "down";
                String message = String.format("Card %s in your binder went %s by %.2f%%", cardName, direction, Math.abs(changePercent));
                notificationService.createSystemNotification(user.getId(), "Card Price Update", message);
                logger.info("[Scheduler] Sent card update to " + user.getUsername() + ": " + message);
            }
        }
        logger.info("[Scheduler] Daily card update notifications complete");
    }
}