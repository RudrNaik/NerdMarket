package NerdMarket.prices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import NerdMarket.market.Market;
import NerdMarket.market.MarketRepository;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Price Tracking", description = "Price history, biggest movers, and price analytics endpoints")
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


    @Operation(summary = "Get all price records")
    @GetMapping(path = "/api/prices")
    List<PriceTracking> getAllPriceRecords() {
        return priceTrackingRepository.findAll();
    }

    @Operation(summary = "Get price history for one specific card")
    @GetMapping(path = "/api/prices/card/{cardId}")
    List<PriceTracking> getPriceHistory(@PathVariable Long cardId) {
        return priceTrackingRepository.findByCardIdOrderByRecordedAtDesc(cardId);
    }

    @Operation(summary = "Get the most recent price record for a card")
    @GetMapping(path = "/api/prices/card/{cardId}/latest")
    PriceTracking getLatestPriceByCard(@PathVariable Long cardId) {
        return priceTrackingRepository.findFirstByCardIdOrderByRecordedAtDesc(cardId);
    }

    @Operation(summary = "Populate price tracking table with top 100 most valuable cards")
    @GetMapping(path = "/api/prices/populate")
    String populatePriceData() {
        return priceTrackingService.populatePriceData();
    }

    @Operation(summary = "Get top 10 biggest movers for all card types")
    @GetMapping(path = "/api/prices/biggest-movers")
    List<Map<String, Object>> getBiggestMovers() {
        return priceTrackingService.getBiggestMovers();
    }

    @Operation(summary = "Get top 10 biggest movers for all card types in the last 2 days")
    @GetMapping(path = "/api/prices/biggest-movers/2days")
    List<Map<String, Object>> getBiggestMoversLast2Days() {
        return priceTrackingService.getBiggestMoversLast2Days();
    }

    @Operation(summary = "Get top 10 biggest movers for all card types in the last 7 days (1 week)")
    @GetMapping(path = "/api/prices/biggest-movers/7days")
    List<Map<String, Object>> getBiggestMoversLast7Days() {
        return priceTrackingService.getBiggestMoversLast7Days();
    }

    @Operation(summary = "Get top 10 biggest movers for all card types in the last 21 days (3 weeks)")
    @GetMapping(path = "/api/prices/biggest-movers/21days")
    List<Map<String, Object>> getBiggestMoversLast21Days() {
        return priceTrackingService.getBiggestMoversLast21Days();
    }

    @Operation(summary = "Get top 10 biggest movers for a specific card type (POKEMON, MTG, YU-GI-OH) all time")
    @GetMapping(path = "/api/prices/biggest-movers/type/{cardType}")
    List<Map<String, Object>> getBiggestMoversByCardType(@PathVariable String cardType) {
        return priceTrackingService.getBiggestMoversByCardType(cardType);
    }

    @Operation(summary = "Get top 10 biggest movers for a specific card type (POKEMON, MTG, YU-GI-OH) in the last 2 days")
    @GetMapping(path = "/api/prices/biggest-movers/type/{cardType}/2days")
    List<Map<String, Object>> getBiggestMoversByCardTypeLast2Days(@PathVariable String cardType) {
        return priceTrackingService.getBiggestMoversByCardTypeLast2Days(cardType);
    }

    @Operation(summary = "Get top 10 biggest movers for a specific card type (POKEMON, MTG, YU-GI-OH) in the last 7 days (1 week)")
    @GetMapping(path = "/api/prices/biggest-movers/type/{cardType}/7days")
    List<Map<String, Object>> getBiggestMoversByCardTypeLast7Days(@PathVariable String cardType) {
        return priceTrackingService.getBiggestMoversByCardTypeLast7Days(cardType);
    }

    @Operation(summary = "Get top 10 biggest movers for a specific card type (POKEMON, MTG, YU-GI-OH) in the last 21 days (3 weeks)")
    @GetMapping(path = "/api/prices/biggest-movers/type/{cardType}/21days")
    List<Map<String, Object>> getBiggestMoversByCardTypeLast21Days(@PathVariable String cardType) {
        return priceTrackingService.getBiggestMoversByCardTypeLast21Days(cardType);
    }

    @Operation(summary = "Get biggest gainers only (positive price changes)")
    @GetMapping(path = "/api/prices/biggest-movers/gainers")
    List<Map<String, Object>> getBiggestGainers() {
        return priceTrackingService.getBiggestGainers();
    }

    @Operation(summary = "Get biggest losers only (negative price changes)")
    @GetMapping(path = "/api/prices/biggest-movers/losers")
    List<Map<String, Object>> getBiggestLosers() {
        return priceTrackingService.getBiggestLosers();
    }

    @Operation(summary = "Create a new price record in price tracking table")
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

    @Operation(summary = "Update price record for a single card in price tracking table")
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

    @Operation(summary = "Delete a single price record by table Id in price tracking table")
    @DeleteMapping(path = "/api/prices/{id}")
    public ResponseEntity<?> deletePriceRecord(@PathVariable Long id) {
        if (!priceTrackingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        priceTrackingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete all price records for a card in price tracking table by cardID")
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