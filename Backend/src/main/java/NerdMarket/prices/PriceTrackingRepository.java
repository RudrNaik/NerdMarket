package NerdMarket.prices;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceTrackingRepository extends JpaRepository<PriceTracking, Long> {

    List<PriceTracking> findByCardId(Long cardId);

    List<PriceTracking> findByCardIdOrderByRecordedAtDesc(Long cardId);

    PriceTracking findFirstByCardIdOrderByRecordedAtDesc(Long cardId);
}