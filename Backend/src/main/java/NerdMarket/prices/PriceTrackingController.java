package NerdMarket.prices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import NerdMarket.market.Market;
import NerdMarket.market.MarketRepository;
import org.springframework.http.ResponseEntity;

@RestController
public class PriceTrackingController {

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    @Autowired
    private PriceTrackingService priceTrackingService;

    @Autowired
    private MarketRepository marketRepository;

    private final String success = "{\"message\":\"success\"}";
    private final String error = "{\"message\":\"error\"}";


    // GET all price records
    @GetMapping(path = "/api/prices")
    List<PriceTracking> getAllPriceRecords() {
        return priceTrackingRepository.findAll();
    }

    // GET price history for one specific card
    @GetMapping(path = "/api/prices/card/{cardId}")
    List<PriceTracking> getPriceHistory(@PathVariable Long cardId) {
        return priceTrackingRepository.findByCardIdOrderByRecordedAtDesc(cardId);
    }

    // GET most recent price of a specific card
    @GetMapping(path = "/api/prices/card/{cardId}/latest")
    PriceTracking getLatestPriceByCard(@PathVariable Long cardId) {
        return priceTrackingRepository.findFirstByCardIdOrderByRecordedAtDesc(cardId);
    }

    //GET to populate price tracking table with top 100 most valued cards.
    @GetMapping(path = "/api/prices/populate")
    String populatePriceData() {
        return priceTrackingService.populatePriceData();
    }

    //GET top 10 biggest Movers all
    @GetMapping(path = "/api/prices/biggest-movers")
    List<Map<String, Object>> getBiggestMovers() {
        return priceTrackingService.getBiggestMovers();
    }

    //GET top 10 biggest movers for last 1 day
    @GetMapping(path = "/api/prices/biggest-movers/1day")
    List<Map<String, Object>> getBiggestMoversLastDay() {
        return priceTrackingService.getBiggestMoversLastDay();
    }

    //GET top 10 biggest movers last 3 days
    @GetMapping(path = "/api/prices/biggest-movers/3days")
    List<Map<String, Object>> getBiggestMoversLast3Days() {
        return priceTrackingService.getBiggestMoversLast3Days();
    }

    //GET top 10 biggest movers last 7 days (last week)
    @GetMapping(path = "/api/prices/biggest-movers/7days")
    List<Map<String, Object>> getBiggestMoversLastWeek() {
        return priceTrackingService.getBiggestMoversLastWeek();
    }

    // POST to create a new price record
    @PostMapping(path = "/api/prices")
    String createPriceRecord(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("cardId") || !request.containsKey("price")) {
            return error;
        }
        Long cardId = ((Number) request.get("cardId")).longValue();
        Market card = marketRepository.findCardById(cardId);
        if (card == null) {
            return "card not found";
        }
        PriceTracking priceRecord = new PriceTracking();
        priceRecord.setCard(card);
        priceRecord.setPrice(((Number) request.get("price")).doubleValue());
        if (request.containsKey("recordedAt")) {
            String dateStr = (String) request.get("recordedAt");
            priceRecord.setRecordedAt(java.time.LocalDateTime.parse(dateStr));
        } else {
            priceRecord.setRecordedAt(java.time.LocalDateTime.now());
        }
        priceTrackingRepository.save(priceRecord);
        return success;
    }

    // PUT to update price record by ID
    @PutMapping(path = "/api/prices/{id}")
    PriceTracking updatePriceRecord(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        PriceTracking priceRecord = priceTrackingRepository.findById(id).orElse(null);
        if (priceRecord == null) {
            return null;
        }
        if (request.containsKey("price")) {
            priceRecord.setPrice(((Number) request.get("price")).doubleValue());
        }
        if (request.containsKey("recordedAt")) {
            String dateStr =  (String) request.get("recordedAt");
            priceRecord.setRecordedAt(java.time.LocalDateTime.parse(dateStr));
        }
        priceTrackingRepository.save(priceRecord);
        return priceTrackingRepository.findById(id).orElse(null);
    }

    // DELETE price record by table index for one specific entry.
    @DeleteMapping(path = "/api/prices/{id}")
    public ResponseEntity<?> deletePriceRecord(@PathVariable Long id) {
        if (!priceTrackingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        priceTrackingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    //DELETE by cardID so all 3 records for one card in price table.
    @DeleteMapping(path = "/api/prices/card/{cardId}")
    String deletePriceRecordsByCardId(@PathVariable Long cardId) {
        List<PriceTracking> records = priceTrackingRepository.findByCardId(cardId);
        if (records.isEmpty()) {
            return "No records found for card";
        }
        priceTrackingRepository.deleteAll(records);
        return success;
    }
}