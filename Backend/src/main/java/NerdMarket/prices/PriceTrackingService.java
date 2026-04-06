package NerdMarket.prices;

import NerdMarket.market.Market;
import NerdMarket.market.MarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
public class PriceTrackingService {

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    @Autowired
    private MarketRepository marketRepository;

    public String populatePriceData() {
        List<Market> cards = marketRepository.findByPriceGreaterThan(3.0);
        if (cards.isEmpty()) {
            return "No cards found above $3";
        }

        int count = 0;
        for (Market card : cards) {
            PriceTracking record = new PriceTracking();
            record.setCard(card);
            record.setPrice(card.getPrice());
            record.setRecordedAt(LocalDateTime.now());
            priceTrackingRepository.save(record);
            count++;
        }
        return "Added " + count + " price records";
    }

    public List<Map<String, Object>> getBiggestMovers() {
        return calculateBiggestMovers(priceTrackingRepository.findAll(), null);
    }

    public List<Map<String, Object>> getBiggestMoversLast2Days() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        return calculateBiggestMovers(priceTrackingRepository.findByRecordedAtAfter(twoDaysAgo), null);
    }

    public List<Map<String, Object>> getBiggestMoversLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return calculateBiggestMovers(priceTrackingRepository.findByRecordedAtAfter(sevenDaysAgo), null);
    }

    public List<Map<String, Object>> getBiggestMoversLast21Days() {
        LocalDateTime twentyOneDaysAgo = LocalDateTime.now().minusDays(21);
        return calculateBiggestMovers(priceTrackingRepository.findByRecordedAtAfter(twentyOneDaysAgo), null);
    }

    public List<Map<String, Object>> getBiggestMoversByCardType(String cardType) {
        return calculateBiggestMovers(priceTrackingRepository.findByCard_CardType(cardType), null);
    }

    public List<Map<String, Object>> getBiggestMoversByCardTypeLast2Days(String cardType) {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        return calculateBiggestMovers(priceTrackingRepository.findByCard_CardTypeAndRecordedAtAfter(cardType, twoDaysAgo), null);
    }

    public List<Map<String, Object>> getBiggestMoversByCardTypeLast7Days(String cardType) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return calculateBiggestMovers(priceTrackingRepository.findByCard_CardTypeAndRecordedAtAfter(cardType, sevenDaysAgo), null);
    }

    public List<Map<String, Object>> getBiggestMoversByCardTypeLast21Days(String cardType) {
        LocalDateTime twentyOneDaysAgo = LocalDateTime.now().minusDays(21);
        return calculateBiggestMovers(priceTrackingRepository.findByCard_CardTypeAndRecordedAtAfter(cardType, twentyOneDaysAgo), null);
    }

    public List<Map<String, Object>> getBiggestGainers() {
        return calculateBiggestMovers(priceTrackingRepository.findAll(), "gainers");
    }

    public List<Map<String, Object>> getBiggestLosers() {
        return calculateBiggestMovers(priceTrackingRepository.findAll(), "losers");
    }

    //Calculates biggest movers from a list of records
    private List<Map<String, Object>> calculateBiggestMovers(List<PriceTracking> records, String direction) {
        //Grouping by cardId
        Map<Long, List<PriceTracking>> cardPrices = new HashMap<>();
        for (PriceTracking record : records) {
            Long cardId = record.getCardId();
            if (!cardPrices.containsKey(cardId)) {
                cardPrices.put(cardId, new ArrayList<>());
            }
            cardPrices.get(cardId).add(record);
        }

        //Calculate price change for each individual card
        List<Map<String, Object>> movers = new ArrayList<>();
        for (Long cardId : cardPrices.keySet()) {
            List<PriceTracking> prices = cardPrices.get(cardId);
            if (prices.size() < 2) {
                continue;
            }

            //Sorts by date to get oldest and newest
            prices.sort(Comparator.comparing(PriceTracking::getRecordedAt));
            double oldPrice = prices.get(0).getPrice();
            double newPrice = prices.get(prices.size() - 1).getPrice();

            if (oldPrice == 0) {
                continue;
            }

            double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;
            changePercent = Math.round(changePercent * 100.0) / 100.0;
            Map<String, Object> mover = new HashMap<>();
            mover.put("cardId", cardId);
            mover.put("oldPrice", oldPrice);
            mover.put("newPrice", newPrice);
            mover.put("changePercent", changePercent);
            movers.add(mover);
        }

        if ("gainers".equals(direction)) {
            movers.removeIf(m -> (Double) m.get("changePercent") <= 0);
        } else if ("losers".equals(direction)) {
            movers.removeIf(m -> (Double) m.get("changePercent") >= 0);
        }

        //Sorts by change percentage to get the top 10 biggest movers
        movers.sort((a, b) -> Double.compare(Math.abs((Double) b.get("changePercent")), Math.abs((Double) a.get("changePercent"))));
        if (movers.size() > 10) {
            return movers.subList(0, 10);
        }
        return movers;
    }
}