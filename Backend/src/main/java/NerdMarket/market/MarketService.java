//Contains logic to handle external API calls and save it to the database.
//Purpose of this file is for it to be called to populate the database.

package NerdMarket.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class MarketService {

    @Autowired
    private MarketRepository marketRepository;
    private final RestTemplate restTemplate;

    public MarketService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    private Market findOrCreate(String name, String set, String type) {
        Market existing = marketRepository.findByCardNameAndCardSetAndCardType(name, set, type);
        if (existing != null) return existing;
        Market card = new Market();
        card.setCardName(name);
        card.setCardSet(set);
        card.setCardType(type);
        return card;
    }

    //Fetch all pokemon cards from TCGDex
    public String fetchAllPokemonCards() {
        try {
            String setsUrl = "https://api.tcgdex.net/v2/en/sets";
            List<Map> sets = restTemplate.getForObject(setsUrl, List.class);

            if (sets == null || sets.isEmpty()) {
                return "No sets found";
            }

            int totalCards = 0;
            int totalSets = sets.size();
            int currentSet = 0;

            // Looping through each set
            for (Map setInfo : sets) {
                String setId = (String) setInfo.get("id");
                currentSet++;
                System.out.println("Fetching set " + currentSet + "/" + totalSets + ": " + setId);
                try {
                    // Get full set details with card list
                    String setUrl = "https://api.tcgdex.net/v2/en/sets/" + setId;
                    Map setResponse = restTemplate.getForObject(setUrl, Map.class);
                    if (setResponse == null) continue;

                    List<Map> cards = (List<Map>) setResponse.get("cards");
                    if (cards == null) continue;
                    // Fetch each card in the set
                    for (Map cardInfo : cards) {
                        String cardId = (String) cardInfo.get("id");
                        try {
                            String cardUrl = "https://api.tcgdex.net/v2/en/cards/" + cardId;
                            Map cardResponse = restTemplate.getForObject(cardUrl, Map.class);
                            if (cardResponse != null) {
                                String name = (String) cardResponse.get("name");
                                String rarity = (String) cardResponse.get("rarity");
                                Map set = (Map) cardResponse.get("set");
                                String setName = (set != null) ? (String) set.get("name") : "Unknown";
                                Market marketCard = findOrCreate(name, setName, "POKEMON");
                                marketCard.setCardRarity(rarity);
                                // Get price from TCGPlayer which now supports all updated tags/types
                                double existingPrice = marketCard.getPrice();
                                boolean foundPrice = false;
                                String matchedVariant = null;
                                Object pricingObj = cardResponse.get("pricing");
                                if (pricingObj instanceof Map<?, ?> pricingMap) {
                                    Object tcgplayerObj = pricingMap.get("tcgplayer");
                                    if (tcgplayerObj instanceof Map<?, ?> tcgplayerMap) {
                                        String[] variantKeys = {"holofoil", "normal", "reverse-holofoil", "reverseHolofoil", "reverse", "1st-edition-holofoil", "1st-edition", "unlimited-holofoil", "unlimited"};
                                        for (String key : variantKeys) {
                                            Object variantObj = tcgplayerMap.get(key);
                                            if (!(variantObj instanceof Map<?, ?> variantMap)) {
                                                continue;
                                            }
                                            Object marketPriceObj = variantMap.get("marketPrice");
                                            if (marketPriceObj instanceof Number num) {
                                                marketCard.setPrice(num.doubleValue());
                                                foundPrice = true;
                                                matchedVariant = key;
                                                break;
                                            }
                                            if (marketPriceObj instanceof String s) {
                                                try {
                                                    marketCard.setPrice(Double.parseDouble(s));
                                                    foundPrice = true;
                                                    matchedVariant = key;
                                                    break;
                                                } catch (NumberFormatException ignored) {
                                                }
                                            }
                                        }
                                    }
                                }
                                // If no price found, keep existing price and don't overwrite with 0
                                System.out.println("Pokemon card: " + name + " | set=" + setName + " | matchedVariant=" + matchedVariant + " | finalPrice=" + marketCard.getPrice());
                                // Get image of the card from API
                                String imageUrl = (String) cardResponse.get("image");
                                if (imageUrl != null) {
                                    marketCard.setImageUrl(imageUrl + "/high.webp");
                                }
                                marketRepository.save(marketCard);
                                totalCards++;
                                if (totalCards % 1000 == 0 && totalCards > 0) {
                                    System.out.println("Pokemon progress: " + totalCards + " cards processed (set " + currentSet + "/" + totalSets + ")");
                                }
                            }
                            // Small delay to avoid skipping cards
                            Thread.sleep(50);
                        } catch (Exception e) {
                            System.out.println("Failed to fetch card: " + cardId + " | " + e.getClass().getSimpleName() + " | " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    // Delay between each set that gets imported.
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.out.println("Failed to fetch set: " + setId +  " | " + e.getClass().getSimpleName() + " | " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return "Successfully added " + totalCards + " Pokemon cards from " + totalSets + " sets";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    //Fetch all MTG cards from Scryfall
    public String fetchAllMTGCards() {
        try {
            //Gets all sets for MTG rom Scryfall API
            String setsUrl = "https://api.scryfall.com/sets";
            Map setsResponse = restTemplate.getForObject(setsUrl, Map.class);

            if (setsResponse == null){
                return "No sets found";
            }
            List<Map> sets = (List<Map>) setsResponse.get("data");
            if (sets == null || sets.isEmpty()) {
                return "No sets found";
            }

            int totalCards = 0;
            int totalSets = sets.size();
            int currentSet = 0;

            //Looping through every set in API
            for (Map setInfo : sets) {
                String setName = (String) setInfo.get("name");
                String setCode = (String) setInfo.get("code");
                currentSet++;
                System.out.println("Fetching MTG set " + currentSet + "/" + totalSets + ": " + setName);

                try {
                    //Fetch all cards in the Set that it is currently in
                    String searchUrl = "https://api.scryfall.com/cards/search?q=set:" + setCode;
                    String nextPage = searchUrl;

                    while (nextPage != null) {
                        Map pageResponse = restTemplate.getForObject(nextPage, Map.class);
                        if (pageResponse == null) break;

                        List<Map> cards = (List<Map>) pageResponse.get("data");
                        if (cards != null) {
                            for (Map card : cards) {
                                try {
                                    Market marketCard = findOrCreate((String) card.get("name"), (String) card.get("set_name"), "MTG");
                                    marketCard.setCardRarity((String) card.get("rarity"));
                                    //Get price of card
                                    Map prices = (Map) card.get("prices");
                                    if (prices != null && prices.get("usd") != null) {
                                        marketCard.setPrice(Double.parseDouble((String) prices.get("usd")));
                                    }

                                    //Get image URL of card
                                    Map imageUris = (Map) card.get("image_uris");
                                    if (imageUris != null && imageUris.get("normal") != null) {
                                        marketCard.setImageUrl((String) imageUris.get("normal"));
                                    }
                                    marketRepository.save(marketCard);
                                    totalCards++;
                                } catch (Exception e) {
                                    System.out.println("Failed to fetch MTG card: " + card.get("name") + " | " + e.getClass().getSimpleName() + " | " + e.getMessage());
                                }
                            }
                        }
                        //Checks next page in the API
                        Boolean hasMore = (Boolean) pageResponse.get("has_more");
                        if (hasMore != null && hasMore) {
                            nextPage = (String) pageResponse.get("next_page");
                            Thread.sleep(150);
                        } else {
                            nextPage = null;
                        }
                    }
                    Thread.sleep(200);
                } catch (Exception e) {
                    System.out.println("Failed: " + setName);
                }
            }
            return "Successfully added " + totalCards + "MTG cards from " + totalSets + " sets";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    //Fetch all cards from YGOPROdeck
    public String fetchAllYugiohCards() {
        try {
            //YGOPRODeck has no sets so all cards can be returnedin one request.
            String url = "https://db.ygoprodeck.com/api/v7/cardinfo.php";
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null){
                return "No cards found";
            }
            List<Map> cards = (List<Map>) response.get("data");
            if (cards == null || cards.isEmpty()) {
                return "no cards found";
            }

            int totalCards = 0;
            int total = cards.size();
            System.out.println("Fetching " + total + " Yu-Gi-Oh cards");

            for (Map card : cards) {
                try {
                    String name = (String) card.get("name");

                    //Grab Set and Rarity if Available in the API.
                    String setName;
                    String rarity;
                    List<Map> cardSets = (List<Map>) card.get("card_sets");
                    if (cardSets != null && !cardSets.isEmpty()) {
                        Map firstSet = cardSets.get(0);
                        setName = (String) firstSet.get("set_name");
                        rarity = (String) firstSet.get("set_rarity");
                    } else {
                        setName = "unknown";
                        rarity = "unknown";
                    }

                    Market marketCard = findOrCreate(name, setName, "YU-Gi-Oh");
                    marketCard.setCardRarity(rarity != null ? rarity : "Unknown");
                    //Grab prices from api.
                    List<Map> cardPrices = (List<Map>) card.get("card_prices");
                    if (cardPrices != null && !cardPrices.isEmpty()) {
                        Map priceInfo = cardPrices.get(0);
                        String tcgPrice = (String) priceInfo.get("tcgplayer_price");
                        if (tcgPrice != null && !tcgPrice.isEmpty()) {
                            marketCard.setPrice(Double.parseDouble(tcgPrice));
                        }
                    }

                    //Get image of the card.
                    List<Map> cardImages = (List<Map>) card.get("card_images");
                    if (cardImages != null && !cardImages.isEmpty()) {
                        Map imageInfo = cardImages.get(0);
                        marketCard.setImageUrl((String) imageInfo.get("image_url"));
                    }
                    marketRepository.save(marketCard);
                    totalCards++;

                    //Tell me the progress after every 1000 cards.
                    if (totalCards % 1000 == 0) {
                        System.out.println("Progress: " + totalCards + "/" + total);
                    }
                } catch (Exception e) {
                    System.out.println("Failed to fetch Yu-Gi-Oh card: " + card.get("name") + " | " + e.getClass().getSimpleName() + " | " + e.getMessage());
                }
            }
            return "Successfully added " + totalCards + "Yu-Gi-Oh cards";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

}