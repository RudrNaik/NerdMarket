package NerdMarket.prices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class PriceTrackingController {

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

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

    // POST to create a new price record
    @PostMapping(path = "/api/prices")
    String createPriceRecord(@RequestBody PriceTracking priceRecord) {
        if (priceRecord == null) {
            return error;
        }
        priceTrackingRepository.save(priceRecord);
        return success;
    }

    // PUT to update price record by ID
    @PutMapping(path = "/api/prices/{id}")
    PriceTracking updatePriceRecord(@PathVariable Long id, @RequestBody PriceTracking request) {
        PriceTracking priceRecord = priceTrackingRepository.findById(id).orElse(null);
        if (priceRecord == null) {
            return null;
        }
        request.setId(id);
        priceTrackingRepository.save(request);
        return priceTrackingRepository.findById(id).orElse(null);
    }

    // DELETE price record by ID
    @DeleteMapping(path = "/api/prices/{id}")
    String deletePriceRecord(@PathVariable Long id) {
        if (!priceTrackingRepository.existsById(id)) {
            return error;
        }
        priceTrackingRepository.deleteById(id);
        return success;
    }
}