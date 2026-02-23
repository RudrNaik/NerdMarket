//CRUD REST endpoints - will handle GET, POST, PUT, DELETE requests.

package NerdMarket.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;


@RestController
public class MarketController {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private MarketService marketService;

    private final String success = "{\"message\":\"success\"}";
    private final String error = "{\"message\":\"error\"}";

    // GET all cards
    @GetMapping(path = "/api/cards")
    List<Market> getAllCards() {
        return marketRepository.findAll();
    }

    // GET specific card by ID
    @GetMapping(path = "/api/cards/{id}")
    Market getCardById(@PathVariable Long id) {
        return marketRepository.findCardById(id);
    }

    // GET cards by type of trading card > Pokemon, MTG, Yu-Gi-Oh, Baseball, etc..
    @GetMapping(path = "/api/cards/type/{cardType}")
    List<Market> getCardsByCardType(@PathVariable String cardType) {
        return marketRepository.findByCardType(cardType);
    }

    //GET top 10 most expensive cards in the entire DB
    @GetMapping(path ="/api/cards/top10")
    List<Market> getTop10MostExpensiveCards() {
        return marketRepository.findTop10ByOrderByPriceDesc();
    }

    //GET top 10 most expensive cards by card type
    @GetMapping(path = "/api/cards/top10/{cardType}")
    List<Market> getTop10MostExpensiveCardsByCardType(@PathVariable String cardType) {
        return marketRepository.findTop10ByCardTypeOrderByPriceDesc(cardType);
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
        Market card = marketRepository.findCardById(id);
        if (card == null) {
            return null;
        }
        request.setId(id);
        marketRepository.save(request);
        return marketRepository.findCardById(id);
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

    //DELETE by Card Type
    @DeleteMapping(path = "/api/cards/type/{cardType}")
    String deleteCardsByCardType(@PathVariable String cardType) {
        List<Market> cards = marketRepository.findByCardType(cardType);
        if (cards.isEmpty()) {
            return "{\"message\":\"No cards found with type: " + cardType + "\"}";
        }
        marketRepository.deleteAll(cards);
        return success;
    }

    //FETCH ALL pokemon cards from API tcgdex
    @GetMapping(path = "/api/cards/pokemon/fetch-all")
    String fetchAllPokemonCards() {
        return marketService.fetchAllPokemonCards();
    }

    //FETCH ALL MTG cards from API scryfall
    @GetMapping(path = "/api/cards/mtg/fetch-all")
    String fetchAllMtgCards() {
        return marketService.fetchAllMTGCards();
    }

    //FETCH all Yugioh cards
    @GetMapping(path = "/api/cards/yugioh/fetch-all")
    String fetchAllYugiohCards() {
        return marketService.fetchAllYugiohCards();
    }
}
