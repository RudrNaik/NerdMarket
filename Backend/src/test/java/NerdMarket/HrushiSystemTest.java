package NerdMarket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class HrushiSystemTest {

    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate restTemplate;
    String baseUrl;
    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    //TEST 1: Full CRUD lifecycle on Market endpoints.
    @Test
    public void marketCrudLifecycleTest() throws Exception {
        //POST: create a new card
        String cardJson = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Charizard\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Holo\"," + "\"price\":100.00," + "\"imageUrl\":\"http://example.com/charizard.png\"" + "}";
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(cardJson), String.class);
        assertEquals(200, createResponse.getStatusCode().value());
        assertTrue(createResponse.getBody().contains("success"), "POST response should contain 'success'");

        //GET all cards, find the one we just created
        ResponseEntity<String> getAllResponse = restTemplate.getForEntity(baseUrl + "/api/cards", String.class);
        assertEquals(200, getAllResponse.getStatusCode().value());
        JSONArray allCards = new JSONArray(getAllResponse.getBody());
        assertTrue(allCards.length() > 0, "Expected at least one card after POST");

        long cardId = -1;
        for (int i = 0; i < allCards.length(); i++) {
            JSONObject c = allCards.getJSONObject(i);
            if ("TEST_HRUSHI_Charizard".equals(c.getString("cardName"))) {
                cardId = c.getLong("id");
                break;
            }
        }
        assertTrue(cardId != -1, "Could not find the card we just created");

        //GET by ID, verify the field values were stored correctly
        ResponseEntity<String> getByIdResponse = restTemplate.getForEntity(baseUrl + "/api/cards/" + cardId, String.class);
        assertEquals(200, getByIdResponse.getStatusCode().value());
        assertNotNull(getByIdResponse.getBody(), "Expected a card body");
        JSONObject fetchedCard = new JSONObject(getByIdResponse.getBody());
        assertEquals("POKEMON", fetchedCard.getString("cardType"));
        assertEquals("Holo", fetchedCard.getString("cardRarity"));
        assertEquals(100.00, fetchedCard.getDouble("price"), 0.001);

        //DELETE the card and verify success
        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/api/cards/" + cardId, HttpMethod.DELETE, null, String.class);
        assertEquals(200, deleteResponse.getStatusCode().value());
        assertTrue(deleteResponse.getBody().contains("success"), "DELETE response should contain 'success'");
    }

    //TEST 2: Verify filtering cards by type returns only cards of that type.
    @Test
    public void cardTypeFilteringTest() throws Exception {
        //POST 2 POKEMON cards
        String pokemon1 = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Pikachu\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Common\"," + "\"price\":5.00" + "}";
        String pokemon2 = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Mewtwo\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Holo\"," + "\"price\":50.00" + "}";
        //POST 1 MTG card
        String mtg1 = "{" + "\"cardType\":\"MTG\"," + "\"cardName\":\"TEST_HRUSHI_BlackLotus\"," + "\"cardSet\":\"Alpha\"," + "\"cardRarity\":\"Rare\"," + "\"price\":10000.00" + "}";

        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(pokemon1), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(pokemon2), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(mtg1), String.class).getStatusCode().value());

        //GET POKEMON cards: should contain both Pikachu and Mewtwo, but NOT Black Lotus
        ResponseEntity<String> pokemonResponse = restTemplate.getForEntity(baseUrl + "/api/cards/type/POKEMON", String.class);
        assertEquals(200, pokemonResponse.getStatusCode().value());
        JSONArray pokemonCards = new JSONArray(pokemonResponse.getBody());
        assertTrue(pokemonCards.length() >= 2, "Expected at least 2 POKEMON cards, got " + pokemonCards.length());
        boolean foundPikachu = false;
        boolean foundMewtwo = false;
        boolean foundBlackLotus = false;
        for (int i = 0; i < pokemonCards.length(); i++) {
            JSONObject c = pokemonCards.getJSONObject(i);
            //Every result must actually be a POKEMON card
            assertEquals("POKEMON", c.getString("cardType"), "POKEMON filter returned a non-POKEMON card: " + c.getString("cardName"));
            String name = c.getString("cardName");
            if ("TEST_HRUSHI_Pikachu".equals(name)) foundPikachu = true;
            if ("TEST_HRUSHI_Mewtwo".equals(name)) foundMewtwo = true;
            if ("TEST_HRUSHI_BlackLotus".equals(name)) foundBlackLotus = true;
        }
        assertTrue(foundPikachu, "Pikachu should appear in POKEMON results");
        assertTrue(foundMewtwo, "Mewtwo should appear in POKEMON results");
        assertTrue(!foundBlackLotus, "Black Lotus (MTG) should NOT appear in POKEMON results");

        //GET MTG cards: should contain Black Lotus, but NOT the POKEMON cards
        ResponseEntity<String> mtgResponse = restTemplate.getForEntity(baseUrl + "/api/cards/type/MTG", String.class);
        assertEquals(200, mtgResponse.getStatusCode().value());
        JSONArray mtgCards = new JSONArray(mtgResponse.getBody());
        assertTrue(mtgCards.length() >= 1, "Expected at least 1 MTG card, got " + mtgCards.length());
        boolean foundBlackLotusInMtg = false;
        for (int i = 0; i < mtgCards.length(); i++) {
            JSONObject c = mtgCards.getJSONObject(i);
            //Every result must actually be MTG
            assertEquals("MTG", c.getString("cardType"), "MTG filter returned a non-MTG card: " + c.getString("cardName"));
            String name = c.getString("cardName");
            if ("TEST_HRUSHI_BlackLotus".equals(name)) foundBlackLotusInMtg = true;
            //Check: POKEMON cards must not appear in MTG results
            assertTrue(!"TEST_HRUSHI_Pikachu".equals(name), "Pikachu (POKEMON) should NOT appear in MTG results");
            assertTrue(!"TEST_HRUSHI_Mewtwo".equals(name), "Mewtwo (POKEMON) should NOT appear in MTG results");
        }
        assertTrue(foundBlackLotusInMtg, "Black Lotus should appear in MTG results");
    }

    //TEST 3: Verify the top 10 most expensive cards endpoint returns
    @Test
    public void top10MostExpensiveSortingTest() throws Exception {
        //POST 3 cards with distinctive prices that should land in the top 10 (using very high values to make sure they're not pushed out by other test data)
        String cheap = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Cheap\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Common\"," + "\"price\":50000.00" + "}";
        String medium = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Medium\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Holo\"," + "\"price\":75000.00" + "}";
        String expensive = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Expensive\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Illustration Rare\"," + "\"price\":99999.00" + "}";

        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(cheap), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(medium), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(expensive), String.class).getStatusCode().value());

        //GET top 10
        ResponseEntity<String> top10Response = restTemplate.getForEntity(baseUrl + "/api/cards/top10", String.class);
        assertEquals(200, top10Response.getStatusCode().value());
        JSONArray top10 = new JSONArray(top10Response.getBody());
        assertTrue(top10.length() > 0, "Expected at least one card in top 10");
        assertTrue(top10.length() <= 10, "Top 10 should return at most 10 cards");

        //Verify global descending order: each card's price >= the next card's price
        for (int i = 0; i < top10.length() - 1; i++) {
            double current = top10.getJSONObject(i).getDouble("price");
            double next = top10.getJSONObject(i + 1).getDouble("price");
            assertTrue(current >= next, "Top 10 should be sorted by price descending. Position " + i + " price=" + current + " is less than position " + (i + 1) + " price=" + next);
        }

        //Find the 3 test cards in the top 10 and verify their relative order
        int posCheap = -1;
        int posMedium = -1;
        int posExpensive = -1;
        for (int i = 0; i < top10.length(); i++) {
            String name = top10.getJSONObject(i).getString("cardName");
            if ("TEST_HRUSHI_Cheap".equals(name)) posCheap = i;
            if ("TEST_HRUSHI_Medium".equals(name)) posMedium = i;
            if ("TEST_HRUSHI_Expensive".equals(name)) posExpensive = i;
        }

        assertTrue(posExpensive != -1, "Expensive card should be in top 10");
        assertTrue(posMedium != -1, "Medium card should be in top 10");
        assertTrue(posCheap != -1, "Cheap card should be in top 10");

        // Expensive ($99,999) must come before Medium ($75,000), which must come before Cheap ($50,000)
        assertTrue(posExpensive < posMedium, "Expensive ($99,999) should appear before Medium ($75,000). " + "posExpensive=" + posExpensive + " posMedium=" + posMedium);
        assertTrue(posMedium < posCheap, "Medium ($75,000) should appear before Cheap ($50,000). " + "posMedium=" + posMedium + " posCheap=" + posCheap);
    }

    //TEST 4: Cross-feature flow covering Market + Prices.
    @Test
    public void priceTrackingBiggestMoversTest() throws Exception {
        //Create a card to track price history
        String cardJson = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_Mover\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Holo\"," + "\"price\":150.00" + "}";
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(cardJson), String.class);
        assertEquals(200, createResponse.getStatusCode().value());

        //Look up the auto-generated id for the card we just made
        ResponseEntity<String> getAllResponse = restTemplate.getForEntity(baseUrl + "/api/cards", String.class);
        JSONArray allCards = new JSONArray(getAllResponse.getBody());
        long cardId = -1;
        for (int i = 0; i < allCards.length(); i++) {
            JSONObject c = allCards.getJSONObject(i);
            if ("TEST_HRUSHI_Mover".equals(c.getString("cardName"))) {
                cardId = c.getLong("id");
                break;
            }
        }
        assertTrue(cardId != -1, "Could not find the card we just created");

        //POST an old, low price record (10 days ago, $100)
        String oldPriceJson = "{" + "\"cardId\":" + cardId + "," + "\"price\":100.00," + "\"recordedAt\":\"" + java.time.LocalDateTime.now().minusDays(10) + "\"" + "}";
        ResponseEntity<String> oldPriceResponse = restTemplate.postForEntity(baseUrl + "/api/prices", jsonEntity(oldPriceJson), String.class);
        assertEquals(200, oldPriceResponse.getStatusCode().value());
        assertTrue(oldPriceResponse.getBody().contains("success"));

        //POST a recent, high price record (today, $200) —> that's a 100% gain
        String newPriceJson = "{" + "\"cardId\":" + cardId + "," + "\"price\":200.00," + "\"recordedAt\":\"" + java.time.LocalDateTime.now() + "\"" + "}";
        ResponseEntity<String> newPriceResponse = restTemplate.postForEntity(baseUrl + "/api/prices", jsonEntity(newPriceJson), String.class);
        assertEquals(200, newPriceResponse.getStatusCode().value());
        assertTrue(newPriceResponse.getBody().contains("success"));

        //Confirm both price records were stored by querying the price history for the card
        ResponseEntity<String> historyResponse = restTemplate.getForEntity(baseUrl + "/api/prices/card/" + cardId, String.class);
        assertEquals(200, historyResponse.getStatusCode().value());
        JSONArray history = new JSONArray(historyResponse.getBody());
        assertEquals(2, history.length(), "Expected exactly 2 price records for our test card");

        //GET biggest movers and verify the card shows up with the correct change %
        ResponseEntity<String> moversResponse = restTemplate.getForEntity(baseUrl + "/api/prices/biggest-movers", String.class);
        assertEquals(200, moversResponse.getStatusCode().value());
        JSONArray movers = new JSONArray(moversResponse.getBody());
        assertTrue(movers.length() > 0, "Expected at least one mover");
        boolean foundOurCard = false;
        for (int i = 0; i < movers.length(); i++) {
            JSONObject mover = movers.getJSONObject(i);
            // Each mover record must include the analytics fields
            assertTrue(mover.has("cardId"), "Mover should have cardId field");
            assertTrue(mover.has("oldPrice"), "Mover should have oldPrice field");
            assertTrue(mover.has("newPrice"), "Mover should have newPrice field");
            assertTrue(mover.has("changePercent"), "Mover should have changePercent field");

            if (mover.getLong("cardId") == cardId) {
                foundOurCard = true;
                assertEquals(100.00, mover.getDouble("oldPrice"), 0.001, "Old price should be 100.00");
                assertEquals(200.00, mover.getDouble("newPrice"), 0.001, "New price should be 200.00");
                assertEquals(100.00, mover.getDouble("changePercent"), 0.001, "Change percent should be exactly 100% for $100 -> $200");
            }
        }
        assertTrue(foundOurCard, "Our test card should appear in biggest movers results");
    }
}
