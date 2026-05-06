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

    //TEST 5: Market search, update, top 10 by type, and delete by type.
    @Test
    public void marketSearchAndDeleteByTypeTest() throws Exception {
        //POST 2 cards we can search for
        String card1 = "{" + "\"cardType\":\"YU-Gi-Oh\"," + "\"cardName\":\"TEST_HRUSHI_BlueEyes\"," + "\"cardSet\":\"LOB\"," + "\"cardRarity\":\"Ultra\"," + "\"price\":250.00" + "}";
        String card2 = "{" + "\"cardType\":\"YU-Gi-Oh\"," + "\"cardName\":\"TEST_HRUSHI_DarkMagician\"," + "\"cardSet\":\"LOB\"," + "\"cardRarity\":\"Ultra\"," + "\"price\":150.00" + "}";
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(card1), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(card2), String.class).getStatusCode().value());

        //Search by name (case-insensitive)
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(baseUrl + "/api/cards/search/blueeyes", String.class);
        assertEquals(200, searchResponse.getStatusCode().value());
        JSONArray searchResults = new JSONArray(searchResponse.getBody());
        boolean foundBlueEyes = false;
        for (int i = 0; i < searchResults.length(); i++) {
            if ("TEST_HRUSHI_BlueEyes".equals(searchResults.getJSONObject(i).getString("cardName"))) {
                foundBlueEyes = true;
                break;
            }
        }
        assertTrue(foundBlueEyes, "Search should find BlueEyes case-insensitively");

        //Get top 10 by card type
        ResponseEntity<String> top10TypeResponse = restTemplate.getForEntity(baseUrl + "/api/cards/top10/YU-Gi-Oh", String.class);
        assertEquals(200, top10TypeResponse.getStatusCode().value());
        JSONArray top10Type = new JSONArray(top10TypeResponse.getBody());
        assertTrue(top10Type.length() <= 10, "Top 10 by type should return at most 10 cards");

        //Find BlueEyes id, then PUT to update it
        long blueEyesId = -1;
        JSONArray allCards = new JSONArray(restTemplate.getForEntity(baseUrl + "/api/cards", String.class).getBody());
        for (int i = 0; i < allCards.length(); i++) {
            if ("TEST_HRUSHI_BlueEyes".equals(allCards.getJSONObject(i).getString("cardName"))) {
                blueEyesId = allCards.getJSONObject(i).getLong("id");
                break;
            }
        }
        assertTrue(blueEyesId != -1);
        String updateJson = "{" + "\"cardType\":\"YU-Gi-Oh\"," + "\"cardName\":\"TEST_HRUSHI_BlueEyes\"," + "\"cardSet\":\"LOB\"," + "\"cardRarity\":\"Secret\"," + "\"price\":500.00" + "}";
        ResponseEntity<String> updateResponse = restTemplate.exchange(baseUrl + "/api/cards/" + blueEyesId, HttpMethod.PUT, jsonEntity(updateJson), String.class);
        assertEquals(200, updateResponse.getStatusCode().value());
        //Verify the update worked
        JSONObject updatedCard = new JSONObject(restTemplate.getForEntity(baseUrl + "/api/cards/" + blueEyesId, String.class).getBody());
        assertEquals(500.00, updatedCard.getDouble("price"), 0.001);
        assertEquals("Secret", updatedCard.getString("cardRarity"));
        //Delete all cards of this type, verify endpoint responds successfully
        ResponseEntity<String> deleteByTypeResponse = restTemplate.exchange(baseUrl + "/api/cards/type/YU-Gi-Oh", HttpMethod.DELETE, null, String.class);
        assertEquals(200, deleteByTypeResponse.getStatusCode().value());
    }

    //TEST 6: Price tracking CRUD and analytics endpoints.
    @Test
    public void priceCrudAndAnalyticsTest() throws Exception {
        //Create a card to attach price records to
        String cardJson = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_PriceCard\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Holo\"," + "\"price\":80.00" + "}";
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(cardJson), String.class).getStatusCode().value());

        //Look up the cardId
        JSONArray allCards = new JSONArray(restTemplate.getForEntity(baseUrl + "/api/cards", String.class).getBody());
        long cardId = -1;
        for (int i = 0; i < allCards.length(); i++) {
            if ("TEST_HRUSHI_PriceCard".equals(allCards.getJSONObject(i).getString("cardName"))) {
                cardId = allCards.getJSONObject(i).getLong("id");
                break;
            }
        }
        assertTrue(cardId != -1);

        //POST 2 price records, old (5 days ago, $80) and new (today, $40) - that's a 50% drop
        String oldPrice = "{\"cardId\":" + cardId + ",\"price\":80.00,\"recordedAt\":\"" + java.time.LocalDateTime.now().minusDays(5) + "\"}";
        String newPrice = "{\"cardId\":" + cardId + ",\"price\":40.00,\"recordedAt\":\"" + java.time.LocalDateTime.now() + "\"}";
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/prices", jsonEntity(oldPrice), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/prices", jsonEntity(newPrice), String.class).getStatusCode().value());

        //GET all price records and latest price for this card
        ResponseEntity<String> allPricesResponse = restTemplate.getForEntity(baseUrl + "/api/prices", String.class);
        assertEquals(200, allPricesResponse.getStatusCode().value());
        assertTrue(new JSONArray(allPricesResponse.getBody()).length() >= 2);

        ResponseEntity<String> latestResponse = restTemplate.getForEntity(baseUrl + "/api/prices/card/" + cardId + "/latest", String.class);
        assertEquals(200, latestResponse.getStatusCode().value());
        assertEquals(40.00, new JSONObject(latestResponse.getBody()).getDouble("price"), 0.001);

        //Find the latest price record id, then PUT to update it
        long priceRecordId = new JSONObject(latestResponse.getBody()).getLong("id");
        String updateJson = "{\"price\":35.00}";
        ResponseEntity<String> updatePriceResponse = restTemplate.exchange(baseUrl + "/api/prices/" + priceRecordId, HttpMethod.PUT, jsonEntity(updateJson), String.class);
        assertEquals(200, updatePriceResponse.getStatusCode().value());

        //Hit each biggest-movers variant - just verify they respond 200 and return JSON arrays
        String[] moverEndpoints = {"/api/prices/biggest-movers/2days", "/api/prices/biggest-movers/7days", "/api/prices/biggest-movers/21days", "/api/prices/biggest-movers/type/POKEMON", "/api/prices/biggest-movers/type/POKEMON/2days", "/api/prices/biggest-movers/type/POKEMON/7days", "/api/prices/biggest-movers/type/POKEMON/21days", "/api/prices/biggest-movers/gainers", "/api/prices/biggest-movers/losers"};
        for (String endpoint : moverEndpoints) {
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + endpoint, String.class);
            assertEquals(200, response.getStatusCode().value(), "Endpoint should return 200: " + endpoint);
            assertNotNull(new JSONArray(response.getBody()), "Endpoint should return a JSON array: " + endpoint);
        }

        //POST one price record, then DELETE it via the single-record endpoint
        String tempPrice = "{\"cardId\":" + cardId + ",\"price\":99.00}";
        restTemplate.postForEntity(baseUrl + "/api/prices", jsonEntity(tempPrice), String.class);
        long latestId = new JSONObject(restTemplate.getForEntity(baseUrl + "/api/prices/card/" + cardId + "/latest", String.class).getBody()).getLong("id");
        ResponseEntity<String> deleteSingleResponse = restTemplate.exchange(baseUrl + "/api/prices/" + latestId, HttpMethod.DELETE, null, String.class);
        assertEquals(200, deleteSingleResponse.getStatusCode().value());

        //DELETE all price records for this card
        ResponseEntity<String> deleteByCardResponse = restTemplate.exchange(baseUrl + "/api/prices/card/" + cardId, HttpMethod.DELETE, null, String.class);
        assertEquals(200, deleteByCardResponse.getStatusCode().value());
    }

    //TEST 7: Notification retrieval endpoints for an existing user.
    @Test
    public void notificationRetrievalTest() throws Exception {
        //Use existing admin user (hbhatt10, id=3)
        long adminId = 3;

        //Get all notifications for the user - should return 200 even if list is empty
        ResponseEntity<String> userNotifsResponse = restTemplate.getForEntity(baseUrl + "/notifications/user/" + adminId, String.class);
        assertEquals(200, userNotifsResponse.getStatusCode().value());
        JSONArray userNotifs = new JSONArray(userNotifsResponse.getBody());
        assertNotNull(userNotifs, "User notifications list should not be null");

        //Get unread count for the user - should return a non-negative number
        ResponseEntity<String> unreadResponse = restTemplate.getForEntity(baseUrl + "/notifications/unread/" + adminId, String.class);
        assertEquals(200, unreadResponse.getStatusCode().value());
        long unreadCount = Long.parseLong(unreadResponse.getBody());
        assertTrue(unreadCount >= 0, "Unread count should be non-negative");

        //Verify the unread count never exceeds the total count
        assertTrue(unreadCount <= userNotifs.length(), "Unread count should not exceed total notifications");
    }

    //TEST 8: Price tracking populate, gainers, and losers endpoints.
    @Test
    public void priceTrackingPopulateAndDirectionTest() throws Exception {
        //Trigger the populate endpoint - copies all cards >$3 into price_tracking
        ResponseEntity<String> populateResponse = restTemplate.getForEntity(baseUrl + "/api/prices/populate", String.class);
        assertEquals(200, populateResponse.getStatusCode().value());
        assertNotNull(populateResponse.getBody(), "Populate should return a response message");

        //Get all price records - should not be empty after populate
        ResponseEntity<String> allPricesResponse = restTemplate.getForEntity(baseUrl + "/api/prices", String.class);
        assertEquals(200, allPricesResponse.getStatusCode().value());
        JSONArray allPrices = new JSONArray(allPricesResponse.getBody());
        assertTrue(allPrices.length() > 0, "Price records should exist after populate");

        //Hit gainers endpoint - exercises the gainers branch in calculateBiggestMovers
        ResponseEntity<String> gainersResponse = restTemplate.getForEntity(baseUrl + "/api/prices/biggest-movers/gainers", String.class);
        assertEquals(200, gainersResponse.getStatusCode().value());
        JSONArray gainers = new JSONArray(gainersResponse.getBody());
        assertNotNull(gainers, "Gainers list should not be null");

        //Verify gainers all have positive change percent (the filtering rule)
        for (int i = 0; i < gainers.length(); i++) {
            double changePercent = gainers.getJSONObject(i).getDouble("changePercent");
            assertTrue(changePercent > 0, "Gainers should have positive change percent");
        }

        //Hit losers endpoint - exercises the losers branch in calculateBiggestMovers
        ResponseEntity<String> losersResponse = restTemplate.getForEntity(baseUrl + "/api/prices/biggest-movers/losers", String.class);
        assertEquals(200, losersResponse.getStatusCode().value());
        JSONArray losers = new JSONArray(losersResponse.getBody());
        assertNotNull(losers, "Losers list should not be null");

        //Verify losers all have negative change percent (the filtering rule)
        for (int i = 0; i < losers.length(); i++) {
            double changePercent = losers.getJSONObject(i).getDouble("changePercent");
            assertTrue(changePercent < 0, "Losers should have negative change percent");
        }
    }

    //TEST 9: Smoke test - hit notification and chat read endpoints to exercise serialization paths.
    @Test
    public void notificationAndChatSmokeTest() throws Exception {
        long adminId = 3;

        //Notification endpoints - tolerant assertions
        int[] acceptableStatuses = {200, 400, 404};

        //Get notifications by user - exercises NotificationController.getUserNotifications and the full service/repo/entity stack
        ResponseEntity<String> userNotifs = restTemplate.getForEntity(baseUrl + "/notifications/user/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, userNotifs.getStatusCode().value()), "User notifications endpoint should respond");

        //Get unread count - exercises NotificationController.getUnreadCount
        ResponseEntity<String> unread = restTemplate.getForEntity(baseUrl + "/notifications/unread/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, unread.getStatusCode().value()), "Unread count endpoint should respond");

        //Try error path on notification endpoints - non-existent user
        ResponseEntity<String> badNotifs = restTemplate.getForEntity(baseUrl + "/notifications/user/999999", String.class);
        assertTrue(contains(acceptableStatuses, badNotifs.getStatusCode().value()), "Bad user notifications endpoint should respond");

        //Chat endpoints - just exercise the serialization stack
        ResponseEntity<String> chatRooms = restTemplate.getForEntity(baseUrl + "/chat/rooms/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, chatRooms.getStatusCode().value()), "Chat rooms endpoint should respond");

        //Iterate all rooms to exercise ChatRoom getters during JSON serialization
        if (chatRooms.getStatusCode().value() == 200) {
            JSONArray rooms = new JSONArray(chatRooms.getBody());
            for (int i = 0; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);
                //Just touch the fields - this confirms Jackson deserialized them, which means getters were called
                room.optString("name");
                room.optString("type");
                room.optString("cardType");
                room.optLong("id");
            }
        }

        //Try chat rooms for non-existent user (covers the user-not-found error path)
        ResponseEntity<String> badChat = restTemplate.getForEntity(baseUrl + "/chat/rooms/999999", String.class);
        assertTrue(contains(acceptableStatuses, badChat.getStatusCode().value()), "Bad user chat endpoint should respond");
    }

    //Helper to check if a status code is in the acceptable list
    private boolean contains(int[] arr, int value) {
        for (int v : arr) {
            if (v == value) return true;
        }
        return false;
    }
}
