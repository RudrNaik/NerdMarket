//CRUD REST endpoints - will handle GET, POST, PUT, DELETE requests.

package NerdMarket.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Tag(name = "Market", description = "Trading card CRUD and external API fetch endpoints")
@RestController
public class MarketController {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private MarketService marketService;

    private final String success = "{\"message\":\"success\"}";
    private final String error = "{\"message\":\"error\"}";

    @Operation(summary = "Get all cards")
    @GetMapping(path = "/api/cards")
    List<Market> getAllCards() {
        return marketRepository.findAll();
    }

    @Operation(summary = "Get a card by its ID")
    @GetMapping(path = "/api/cards/{id}")
    Market getCardById(@PathVariable Long id) {
        return marketRepository.findCardById(id);
    }

    @Operation(summary = "Get cards by type (POKEMON, MTG, Yu-GI-OH)")
    @GetMapping(path = "/api/cards/type/{cardType}")
    List<Market> getCardsByCardType(@PathVariable String cardType) {
        return marketRepository.findByCardType(cardType);
    }

    @Operation(summary = "Get top 10 most expensive cards across all three types")
    @GetMapping(path ="/api/cards/top10")
    List<Market> getTop10MostExpensiveCards() {
        return marketRepository.findTop10ByOrderByPriceDesc();
    }

    @Operation(summary = "Get top 10 most expensive cards for a specific card type")
    @GetMapping(path = "/api/cards/top10/{cardType}")
    List<Market> getTop10MostExpensiveCardsByCardType(@PathVariable String cardType) {
        return marketRepository.findTop10ByCardTypeOrderByPriceDesc(cardType);
    }

    @Operation(summary = "Search cards by name, case-insensitive")
    @GetMapping(path = "/api/cards/search/{cardName}")
    List<Market> getCardsByCardName(@PathVariable String cardName) {
        return marketRepository.findByCardNameContainingIgnoreCase(cardName);
    }

    @Operation(summary = "Create a new card")
    @PostMapping(path = "/api/cards")
    String createCard(@RequestBody Market card) {
        if (card == null) {
            return error;
        }
        marketRepository.save(card);
        return success;
    }

    @Operation(summary = "Update a card by its ID")
    @PutMapping(path = "/api/cards/{id}")
    Market updateCard(@PathVariable Long id, @RequestBody Market request) {
        Market card = marketRepository.findCardById(id);
        if (card == null) {
            return null;
        }
        request.setId(id);
        marketRepository.save(request);
        return marketRepository.findCardById(id);
    }

    @Operation(summary = "Delete card by its ID")
    @DeleteMapping(path = "/api/cards/{id}")
    String deleteCard(@PathVariable Long id) {
        if (!marketRepository.existsById(id)) {
            return error;
        }
        marketRepository.deleteById(id);
        return success;
    }

    @Operation(summary = "Delete all cards of a specific type")
    @DeleteMapping(path = "/api/cards/type/{cardType}")
    String deleteCardsByCardType(@PathVariable String cardType) {
        List<Market> cards = marketRepository.findByCardType(cardType);
        if (cards.isEmpty()) {
            return "{\"message\":\"No cards found with type: " + cardType + "\"}";
        }
        marketRepository.deleteAll(cards);
        return success;
    }

    @Operation(summary = "Fetch all POKEMON cards from the external API and store in Market database")
    @GetMapping(path = "/api/cards/pokemon/fetch-all")
    String fetchAllPokemonCards() {
        return marketService.fetchAllPokemonCards();
    }

    @Operation(summary = "Fetch all MTG cards from external API and store in Market database")
    @GetMapping(path = "/api/cards/mtg/fetch-all")
    String fetchAllMtgCards() {
        return marketService.fetchAllMTGCards();
    }

    @Operation(summary = "Fetch all YU-GI-OH cards from the external API and store in Market database")
    @GetMapping(path = "/api/cards/yugioh/fetch-all")
    String fetchAllYugiohCards() {
        return marketService.fetchAllYugiohCards();
    }
}
