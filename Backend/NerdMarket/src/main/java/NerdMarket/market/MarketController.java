//CRUD REST endpoints - will handle GET, POST, PUT, DELETE requests.

package NerdMarket.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
public class MarketController {

    @Autowired
    private MarketRepository marketRepository;

    private String success = "{\"message\":\"success\"}";
    private String error = "{\"message\":\"error\"}";

    // GET all cards
    @GetMapping(path = "/api/cards")
    List<Market> getAllCards() {
        return marketRepository.findAll();
    }

    // GET specific card by ID
    @GetMapping(path = "/api/cards/{id}")
    Market getCardById(@PathVariable Long id) {
        return marketRepository.findById(id).orElse(null);
    }

    // GET cards by type of trading card > Pokemon, MTG, Yu-Gi-Oh, Baseball, etc..
    @GetMapping(path = "/api/cards/type/{cardType}")
    List<Market> getCardsByCardType(@PathVariable String cardType) {
        return marketRepository.findByCardType(cardType);
    }

    // POST creating new card
    @PostMapping(path = "/api/cards")
    String createCard(@RequestBody Market card) {
        if (card == null) {
            return error;
        }
        marketRepository.save(card);
        return success;
    }

    // PUT update card by ID
    @PutMapping(path = "/api/cards/{id}")
    Market updateCard(@PathVariable Long id, @RequestBody Market request) {
        Market card = marketRepository.findById(id).orElse(null);
        if (card == null) {
            return null;
        }
        request.setId(id);
        marketRepository.save(request);
        return marketRepository.findById(id).orElse(null);
    }

    // DELETE card by ID
    @DeleteMapping(path = "/api/cards/{id}")
    String deleteCard(@PathVariable Long id) {
        if (!marketRepository.existsById(id)) {
            return error;
        }
        marketRepository.deleteById(id);
        return success;
    }

}
