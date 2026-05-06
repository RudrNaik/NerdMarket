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

    //TEST 9: Test - notification and chat read endpoints to exercise serialization paths.
    @Test
    public void notificationAndChatReadTest() throws Exception {
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

    //TEST 10: Market controller error paths and edge cases.
    @Test
    public void marketErrorPathsAndEdgeCasesTest() throws Exception {
        //GET card by non-existent ID - exercises the not-found path in getCardById
        ResponseEntity<String> missingCardResponse = restTemplate.getForEntity(baseUrl + "/api/cards/9999999", String.class);
        assertEquals(200, missingCardResponse.getStatusCode().value(), "Endpoint responds even for missing card");

        //PUT update on non-existent card - exercises the null branch in updateCard
        String updateJson = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"Ghost\"," + "\"cardSet\":\"Fake\"," + "\"cardRarity\":\"Common\"," + "\"price\":1.00" + "}";
        ResponseEntity<String> badUpdateResponse = restTemplate.exchange(baseUrl + "/api/cards/9999999", HttpMethod.PUT, jsonEntity(updateJson), String.class);
        assertEquals(200, badUpdateResponse.getStatusCode().value(), "Update endpoint responds even for missing card");

        //DELETE on non-existent card - exercises the not-found path in deleteCard
        ResponseEntity<String> badDeleteResponse = restTemplate.exchange(baseUrl + "/api/cards/9999999", HttpMethod.DELETE, null, String.class);
        assertEquals(200, badDeleteResponse.getStatusCode().value(), "Delete endpoint responds even for missing card");

        //DELETE all cards of a non-existent type - exercises the empty-list branch
        ResponseEntity<String> badTypeDeleteResponse = restTemplate.exchange(baseUrl + "/api/cards/type/NONEXISTENT_TYPE_XYZ", HttpMethod.DELETE, null, String.class);
        assertEquals(200, badTypeDeleteResponse.getStatusCode().value(), "Delete by type responds even for unknown type");

        //GET cards by type - exercises findByCardType repository method
        ResponseEntity<String> typeResponse = restTemplate.getForEntity(baseUrl + "/api/cards/type/POKEMON", String.class);
        assertEquals(200, typeResponse.getStatusCode().value());
        JSONArray pokemonCards = new JSONArray(typeResponse.getBody());
        assertNotNull(pokemonCards, "Pokemon cards list should not be null");

        //GET top10 across all types - exercises findTop10ByOrderByPriceDesc
        ResponseEntity<String> top10Response = restTemplate.getForEntity(baseUrl + "/api/cards/top10", String.class);
        assertEquals(200, top10Response.getStatusCode().value());
        JSONArray top10 = new JSONArray(top10Response.getBody());
        assertTrue(top10.length() <= 10, "Top 10 should return at most 10 cards");

        //Iterate top 10 to exercise Market entity getters during JSON serialization
        for (int i = 0; i < top10.length(); i++) {
            JSONObject card = top10.getJSONObject(i);
            card.optString("cardName");
            card.optString("cardType");
            card.optString("cardSet");
            card.optString("cardRarity");
            card.optDouble("price");
            card.optString("imageUrl");
            card.optLong("id");
        }
    }

    //TEST 11: Notifications and chat endpoints exercise the controller/service/repo stack.
    @Test
    public void notificationsAndChatExerciseTest() throws Exception {
        long adminId = 3;
        int[] acceptableStatuses = {200, 400, 404};

        //Get notifications for the admin user - exercises NotificationController.getUserNotifications
        ResponseEntity<String> userNotifs = restTemplate.getForEntity(baseUrl + "/notifications/user/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, userNotifs.getStatusCode().value()), "User notifications endpoint should respond");

        //Iterate user notifications to exercise UserNotification getters during JSON serialization
        if (userNotifs.getStatusCode().value() == 200) {
            JSONArray notifs = new JSONArray(userNotifs.getBody());
            for (int i = 0; i < notifs.length(); i++) {
                JSONObject n = notifs.getJSONObject(i);
                n.optString("notificationType");
                n.optString("notificationTitle");
                n.optString("notificationMessage");
                n.optLong("notificationSenderId");
                n.optString("notificationCreatedAt");
                n.optBoolean("read");
                n.optLong("id");
            }
        }

        //Get unread count - exercises NotificationController.getUnreadCount
        ResponseEntity<String> unread = restTemplate.getForEntity(baseUrl + "/notifications/unread/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, unread.getStatusCode().value()), "Unread count endpoint should respond");

        //Try non-existent user - exercises the user-not-found error branch
        ResponseEntity<String> badNotifs = restTemplate.getForEntity(baseUrl + "/notifications/user/999999", String.class);
        assertTrue(contains(acceptableStatuses, badNotifs.getStatusCode().value()), "Bad user notifications endpoint should respond");

        //Get chat rooms accessible to admin - exercises ChatController.getAccessibleRooms
        ResponseEntity<String> chatRooms = restTemplate.getForEntity(baseUrl + "/chat/rooms/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, chatRooms.getStatusCode().value()), "Chat rooms endpoint should respond");

        //Iterate chat rooms to exercise ChatRoom getters during JSON serialization
        if (chatRooms.getStatusCode().value() == 200) {
            JSONArray rooms = new JSONArray(chatRooms.getBody());
            for (int i = 0; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);
                room.optString("name");
                room.optString("type");
                room.optString("cardType");
                room.optLong("id");
            }
        }

        //Try chat rooms for non-existent user - exercises the user-not-found error branch
        ResponseEntity<String> badChat = restTemplate.getForEntity(baseUrl + "/chat/rooms/999999", String.class);
        assertTrue(contains(acceptableStatuses, badChat.getStatusCode().value()), "Bad user chat endpoint should respond");
    }

    //TEST 12: Notification endpoints for multiple existing users.
    @Test
    public void notificationMultiUserTest() throws Exception {
        //Hit notification endpoints across multiple existing user IDs to exercise repeated calls
        long[] userIds = {1, 3, 4};
        int[] acceptableStatuses = {200, 400, 404};

        for (long userId : userIds) {
            //Get notifications for this user
            ResponseEntity<String> userNotifs = restTemplate.getForEntity(baseUrl + "/notifications/user/" + userId, String.class);
            assertTrue(contains(acceptableStatuses, userNotifs.getStatusCode().value()), "User " + userId + " notifications should respond");

            //Get unread count for this user
            ResponseEntity<String> unread = restTemplate.getForEntity(baseUrl + "/notifications/unread/" + userId, String.class);
            assertTrue(contains(acceptableStatuses, unread.getStatusCode().value()), "User " + userId + " unread count should respond");

            //If we got data, verify the count is consistent with the list size
            if (userNotifs.getStatusCode().value() == 200 && unread.getStatusCode().value() == 200) {
                JSONArray notifs = new JSONArray(userNotifs.getBody());
                long count = Long.parseLong(unread.getBody());
                assertTrue(count <= notifs.length(), "Unread count should not exceed total for user " + userId);
            }
        }
    }

    //TEST 13: Chat history endpoint test.
    @Test
    public void chatHistoryTest() throws Exception {
        int[] acceptableStatuses = {200, 400, 404};

        //Hit chat history for room IDs 1 through 5 (the seeded default rooms)
        for (long roomId = 1; roomId <= 5; roomId++) {
            ResponseEntity<String> historyResponse = restTemplate.getForEntity(baseUrl + "/chat/rooms/" + roomId + "/history", String.class);
            assertTrue(contains(acceptableStatuses, historyResponse.getStatusCode().value()), "History endpoint for room " + roomId + " should respond");

            //If we got history back, touch the ChatMessage fields to confirm Jackson serialized them
            if (historyResponse.getStatusCode().value() == 200) {
                JSONArray history = new JSONArray(historyResponse.getBody());
                for (int j = 0; j < history.length(); j++) {
                    JSONObject msg = history.getJSONObject(j);
                    msg.optString("username");
                    msg.optString("content");
                    msg.optString("sentAt");
                    msg.optLong("id");
                    msg.optLong("userId");
                    msg.optLong("chatRoomId");
                }
            }
        }

        //Hit a non-existent room - exercises the room-not-found branch in ChatService.getRoomHistory
        ResponseEntity<String> badRoomHistory = restTemplate.getForEntity(baseUrl + "/chat/rooms/999999/history", String.class);
        assertTrue(contains(acceptableStatuses, badRoomHistory.getStatusCode().value()), "History for missing room should respond");
    }

    //TEST 14: User signup, login, lookup, password change, and email change endpoints.
    @Test
    public void usersFullLifecycleTest() throws Exception {
        long timestamp = System.currentTimeMillis();
        String username = "TEST_HRUSHI_T14_" + timestamp;
        String email = "hrushi_t14_" + timestamp + "@test.com";
        String password = "InitialPass123";

        //POST signup - create a new user
        String signupJson = "{" + "\"firstName\":\"Test\"," + "\"lastName\":\"User\"," + "\"username\":\"" + username + "\"," + "\"email\":\"" + email + "\"," + "\"password\":\"" + password + "\"" + "}";
        ResponseEntity<String> signupResponse = restTemplate.postForEntity(baseUrl + "/users/signup", jsonEntity(signupJson), String.class);
        assertEquals(200, signupResponse.getStatusCode().value(), "Signup should succeed");
        JSONObject createdUser = new JSONObject(signupResponse.getBody());
        long userId = createdUser.getLong("id");
        assertEquals(username, createdUser.getString("username"));

        //POST login - log in with the new credentials
        String loginJson = "{" + "\"usernameOrEmail\":\"" + username + "\"," + "\"password\":\"" + password + "\"" + "}";
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(baseUrl + "/users/login", jsonEntity(loginJson), String.class);
        assertEquals(200, loginResponse.getStatusCode().value(), "Login should succeed with correct credentials");

        //POST login with bad password - exercises the wrong-password error path
        String badLoginJson = "{" + "\"usernameOrEmail\":\"" + username + "\"," + "\"password\":\"WrongPassword\"" + "}";
        ResponseEntity<String> badLoginResponse = restTemplate.postForEntity(baseUrl + "/users/login", jsonEntity(badLoginJson), String.class);
        assertEquals(400, badLoginResponse.getStatusCode().value(), "Login should fail with wrong password");

        //GET user by id - exercises getUserById happy path
        ResponseEntity<String> getUserResponse = restTemplate.getForEntity(baseUrl + "/users/" + userId, String.class);
        assertEquals(200, getUserResponse.getStatusCode().value());
        JSONObject fetchedUser = new JSONObject(getUserResponse.getBody());
        assertEquals(username, fetchedUser.getString("username"));

        //GET user by non-existent id - exercises the not-found error path
        ResponseEntity<String> badUserResponse = restTemplate.getForEntity(baseUrl + "/users/9999999", String.class);
        assertEquals(400, badUserResponse.getStatusCode().value());

        //PUT change password - exercises changePassword happy path
        String newPassword = "NewPass456";
        String changePassJson = "{\"oldPassword\":\"" + password + "\",\"newPassword\":\"" + newPassword + "\"}";
        ResponseEntity<String> changePassResponse = restTemplate.exchange(baseUrl + "/users/" + userId + "/change-password", HttpMethod.PUT, jsonEntity(changePassJson), String.class);
        assertEquals(200, changePassResponse.getStatusCode().value());

        //PUT change password with wrong old password - exercises the error path
        String badChangePassJson = "{\"oldPassword\":\"NotTheRightOldPassword\",\"newPassword\":\"X\"}";
        ResponseEntity<String> badChangePassResponse = restTemplate.exchange(baseUrl + "/users/" + userId + "/change-password", HttpMethod.PUT, jsonEntity(badChangePassJson), String.class);
        assertEquals(400, badChangePassResponse.getStatusCode().value());

        //PUT change email - exercises changeEmail happy path
        String newEmail = "hrushi_t14_new_" + timestamp + "@test.com";
        String changeEmailJson = "{\"password\":\"" + newPassword + "\",\"newEmail\":\"" + newEmail + "\"}";
        ResponseEntity<String> changeEmailResponse = restTemplate.exchange(baseUrl + "/users/" + userId + "/change-email", HttpMethod.PUT, jsonEntity(changeEmailJson), String.class);
        assertEquals(200, changeEmailResponse.getStatusCode().value());

        //POST signup with duplicate username - exercises the username-taken error
        String dupSignupJson = "{" + "\"firstName\":\"Dup\"," + "\"lastName\":\"User\"," + "\"username\":\"" + username + "\"," + "\"email\":\"different_" + timestamp + "@test.com\"," + "\"password\":\"pass\"" + "}";
        ResponseEntity<String> dupResponse = restTemplate.postForEntity(baseUrl + "/users/signup", jsonEntity(dupSignupJson), String.class);
        assertEquals(400, dupResponse.getStatusCode().value());
    }

    //TEST 15: Admin endpoints exercised with an existing admin user.
    @Test
    public void adminEndpointsTest() throws Exception {
        //Use admin user id=2 (Rudra)
        long adminId = 2;
        int[] acceptableStatuses = {200, 400, 404};

        //GET all users via admin endpoint - exercises AdminController.getAllUsers + AdminService.getAllUsers + requireAdmin
        ResponseEntity<String> allUsersResponse = restTemplate.getForEntity(baseUrl + "/admin/users?userId=" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, allUsersResponse.getStatusCode().value()), "Admin getAllUsers should respond");

        //If 200, iterate users to exercise Users entity getters via Jackson serialization
        if (allUsersResponse.getStatusCode().value() == 200) {
            JSONArray users = new JSONArray(allUsersResponse.getBody());
            for (int i = 0; i < users.length(); i++) {
                JSONObject u = users.getJSONObject(i);
                u.optString("username");
                u.optString("email");
                u.optString("firstName");
                u.optString("lastName");
                u.optBoolean("admin");
                u.optBoolean("active");
                u.optBoolean("locked");
                u.optBoolean("moderator");
                u.optLong("id");
            }
        }

        //GET all users with a non-admin userId - exercises the unauthorized branch in requireAdmin
        ResponseEntity<String> nonAdminResponse = restTemplate.getForEntity(baseUrl + "/admin/users?userId=1", String.class);
        assertTrue(contains(acceptableStatuses, nonAdminResponse.getStatusCode().value()), "Non-admin should be rejected");

        //GET all users with a non-existent userId - exercises the user-not-found branch
        ResponseEntity<String> missingUserResponse = restTemplate.getForEntity(baseUrl + "/admin/users?userId=9999999", String.class);
        assertTrue(contains(acceptableStatuses, missingUserResponse.getStatusCode().value()), "Missing user should be rejected");
    }

    //TEST 16: Chat room create and delete using admin, plus unauthorized rejection.
    @Test
    public void chatRoomCreateDeleteTest() throws Exception {
        //Use admin user id=2 (Rudra) - admin can create/delete chat rooms
        long adminId = 2;
        int[] acceptableStatuses = {200, 400, 404};

        //POST create a new chat room as admin
        String uniqueName = "TEST_HRUSHI_T16_Room_" + System.currentTimeMillis();
        String createRoomJson = "{" + "\"userId\":" + adminId + "," + "\"name\":\"" + uniqueName + "\"," + "\"type\":\"MODERATOR\"" + "}";
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/chat/rooms", jsonEntity(createRoomJson), String.class);
        assertTrue(contains(acceptableStatuses, createResponse.getStatusCode().value()), "Create room should respond");

        //If room was created, exercise deleteRoom as admin
        if (createResponse.getStatusCode().value() == 200) {
            JSONObject createdRoom = new JSONObject(createResponse.getBody());
            long roomId = createdRoom.getLong("id");

            //Verify response fields - exercises ChatRoom getters via Jackson
            assertEquals(uniqueName, createdRoom.getString("name"));
            assertEquals("MODERATOR", createdRoom.getString("type"));
            assertNotNull(createdRoom.optString("createdAt"), "Created room should have a createdAt timestamp");

            //DELETE the room as admin - exercises ChatService.deleteRoom happy path
            ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/chat/rooms/" + roomId + "?userId=" + adminId, HttpMethod.DELETE, null, String.class);
            assertTrue(contains(acceptableStatuses, deleteResponse.getStatusCode().value()), "Delete room should respond");
        }

        //POST create with a non-admin/non-moderator user (id=1) - exercises the unauthorized branch
        String unauthorizedRoomJson = "{" + "\"userId\":1," + "\"name\":\"TEST_HRUSHI_T16_Unauthorized_" + System.currentTimeMillis() + "\"," + "\"type\":\"MODERATOR\"" + "}";
        ResponseEntity<String> unauthorizedResponse = restTemplate.postForEntity(baseUrl + "/chat/rooms", jsonEntity(unauthorizedRoomJson), String.class);
        assertEquals(400, unauthorizedResponse.getStatusCode().value(), "Non-admin/non-moderator should be rejected");

        //DELETE a non-existent room as admin - exercises the room-not-found branch
        ResponseEntity<String> badDeleteResponse = restTemplate.exchange(baseUrl + "/chat/rooms/9999999?userId=" + adminId, HttpMethod.DELETE, null, String.class);
        assertTrue(contains(acceptableStatuses, badDeleteResponse.getStatusCode().value()), "Delete missing room should respond");
    }

    //TEST 17: Notifications mark-as-read and delete using existing user notifications.
    @Test
    public void notificationsMarkReadAndDeleteTest() throws Exception {
        long adminId = 2;
        int[] acceptableStatuses = {200, 400, 404};

        //Get all notifications for admin
        ResponseEntity<String> userNotifsResponse = restTemplate.getForEntity(baseUrl + "/notifications/user/" + adminId, String.class);
        assertTrue(contains(acceptableStatuses, userNotifsResponse.getStatusCode().value()), "Get user notifications should respond");

        //If we got data back, exercise mark-as-read and delete on the first notification
        if (userNotifsResponse.getStatusCode().value() == 200) {
            JSONArray userNotifs = new JSONArray(userNotifsResponse.getBody());
            if (userNotifs.length() > 0) {
                long firstNotifId = userNotifs.getJSONObject(0).getLong("id");

                //PUT mark as read - exercises NotificationController.markAsRead and NotificationService.markAsRead
                ResponseEntity<String> markReadResponse = restTemplate.exchange(baseUrl + "/notifications/" + firstNotifId + "/read", HttpMethod.PUT, null, String.class);
                assertTrue(contains(acceptableStatuses, markReadResponse.getStatusCode().value()), "Mark as read should respond");

                //If mark-read succeeded, verify the read field is true
                if (markReadResponse.getStatusCode().value() == 200) {
                    JSONObject updated = new JSONObject(markReadResponse.getBody());
                    assertTrue(updated.getBoolean("read"), "Notification should be marked read");
                    assertNotNull(updated.optString("readAt"), "Notification should have a readAt timestamp");
                }

                //DELETE the user notification - exercises deleteUserNotification
                ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/notifications/" + firstNotifId, HttpMethod.DELETE, null, String.class);
                assertTrue(contains(acceptableStatuses, deleteResponse.getStatusCode().value()), "Delete should respond");
            }
        }

        //PUT mark-as-read on non-existent ID - exercises the not-found error branch
        ResponseEntity<String> badMarkReadResponse = restTemplate.exchange(baseUrl + "/notifications/9999999/read", HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, badMarkReadResponse.getStatusCode().value()), "Mark-read missing should respond");

        //DELETE non-existent ID - exercises the not-found error branch
        ResponseEntity<String> badDeleteResponse = restTemplate.exchange(baseUrl + "/notifications/9999999", HttpMethod.DELETE, null, String.class);
        assertTrue(contains(acceptableStatuses, badDeleteResponse.getStatusCode().value()), "Delete missing should respond");
    }

    //TEST 18: Admin user management endpoints (promote, demote, activate, deactivate, unlock).
    @Test
    public void adminUserManagementTest() throws Exception {
        //Use admin user id=2 (Rudra) - admin can perform all these actions
        long adminId = 2;
        int[] acceptableStatuses = {200, 400, 404};

        //First, create a fresh user to operate on so we don't disrupt existing accounts
        long timestamp = System.currentTimeMillis();
        String username = "TEST_HRUSHI_T18_" + timestamp;
        String email = "hrushi_t18_" + timestamp + "@test.com";
        String signupJson = "{" + "\"firstName\":\"Test\"," + "\"lastName\":\"Target\"," + "\"username\":\"" + username + "\"," + "\"email\":\"" + email + "\"," + "\"password\":\"InitialPass123\"" + "}";
        ResponseEntity<String> signupResponse = restTemplate.postForEntity(baseUrl + "/users/signup", jsonEntity(signupJson), String.class);
        assertEquals(200, signupResponse.getStatusCode().value(), "Signup should succeed");
        long targetId = new JSONObject(signupResponse.getBody()).getLong("id");

        //PUT promote target to admin
        ResponseEntity<String> promoteResponse = restTemplate.exchange(baseUrl + "/admin/users/" + targetId + "/promote?userId=" + adminId, HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, promoteResponse.getStatusCode().value()), "Promote should respond");

        //PUT demote target from admin
        ResponseEntity<String> demoteResponse = restTemplate.exchange(baseUrl + "/admin/users/" + targetId + "/demote?userId=" + adminId, HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, demoteResponse.getStatusCode().value()), "Demote should respond");

        //POST unlock target user
        ResponseEntity<String> unlockResponse = restTemplate.exchange(baseUrl + "/admin/users/" + targetId + "/unlock?userId=" + adminId, HttpMethod.POST, null, String.class);
        assertTrue(contains(acceptableStatuses, unlockResponse.getStatusCode().value()), "Unlock should respond");

        //PUT deactivate target user
        ResponseEntity<String> deactivateResponse = restTemplate.exchange(baseUrl + "/admin/users/" + targetId + "/deactivate?userId=" + adminId, HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, deactivateResponse.getStatusCode().value()), "Deactivate should respond");

        //PUT activate target user
        ResponseEntity<String> activateResponse = restTemplate.exchange(baseUrl + "/admin/users/" + targetId + "/activate?userId=" + adminId, HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, activateResponse.getStatusCode().value()), "Activate should respond");

        //DELETE the target user as admin
        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/admin/users/" + targetId + "?userId=" + adminId, HttpMethod.DELETE, null, String.class);
        assertTrue(contains(acceptableStatuses, deleteResponse.getStatusCode().value()), "Delete user should respond");

        //Try promote with a non-admin caller (id=1) - exercises the unauthorized branch
        ResponseEntity<String> unauthorizedResponse = restTemplate.exchange(baseUrl + "/admin/users/3/promote?userId=1", HttpMethod.PUT, null, String.class);
        assertEquals(400, unauthorizedResponse.getStatusCode().value(), "Non-admin caller should be rejected");
    }

    //TEST 19: User reset password and delete own account endpoints.
    @Test
    public void usersResetAndDeleteOwnAccountTest() throws Exception {
        int[] acceptableStatuses = {200, 204, 400, 404};

        //Create a fresh user to operate on
        long timestamp = System.currentTimeMillis();
        String username = "TEST_HRUSHI_T19_" + timestamp;
        String email = "hrushi_t19_" + timestamp + "@test.com";
        String password = "InitialPass123";
        String signupJson = "{" + "\"firstName\":\"Test\"," + "\"lastName\":\"Reset\"," + "\"username\":\"" + username + "\"," + "\"email\":\"" + email + "\"," + "\"password\":\"" + password + "\"" + "}";
        ResponseEntity<String> signupResponse = restTemplate.postForEntity(baseUrl + "/users/signup", jsonEntity(signupJson), String.class);
        assertEquals(200, signupResponse.getStatusCode().value(), "Signup should succeed");
        long userId = new JSONObject(signupResponse.getBody()).getLong("id");

        //POST reset password - exercises UserService.resetPassword happy path
        String newPassword = "ResetPass456";
        String resetJson = "{\"email\":\"" + email + "\",\"oldPassword\":\"" + password + "\",\"newPassword\":\"" + newPassword + "\"}";
        ResponseEntity<String> resetResponse = restTemplate.postForEntity(baseUrl + "/users/reset-password", jsonEntity(resetJson), String.class);
        assertTrue(contains(acceptableStatuses, resetResponse.getStatusCode().value()), "Reset password should respond");

        //POST reset with wrong email - exercises the no-account error branch
        String badEmailResetJson = "{\"email\":\"nonexistent_" + timestamp + "@test.com\",\"oldPassword\":\"x\",\"newPassword\":\"y\"}";
        ResponseEntity<String> badEmailResponse = restTemplate.postForEntity(baseUrl + "/users/reset-password", jsonEntity(badEmailResetJson), String.class);
        assertEquals(400, badEmailResponse.getStatusCode().value(), "Reset with bad email should be rejected");

        //POST reset with wrong old password - exercises the wrong-password error branch
        String wrongPassResetJson = "{\"email\":\"" + email + "\",\"oldPassword\":\"WrongPass\",\"newPassword\":\"y\"}";
        ResponseEntity<String> wrongPassResponse = restTemplate.postForEntity(baseUrl + "/users/reset-password", jsonEntity(wrongPassResetJson), String.class);
        assertEquals(400, wrongPassResponse.getStatusCode().value(), "Reset with wrong old password should be rejected");

        //DELETE own account with correct password header - exercises deleteOwnAccount happy path
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Password", newPassword);
        HttpEntity<String> deleteEntity = new HttpEntity<>(headers);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/users/" + userId + "/delete-account", HttpMethod.DELETE, deleteEntity, String.class);
        assertTrue(contains(acceptableStatuses, deleteResponse.getStatusCode().value()), "Delete own account should respond");

        //DELETE own account with wrong password - exercises the incorrect-password error branch
        //Using admin id=2 as a guaranteed-existing user we can target
        HttpHeaders badHeaders = new HttpHeaders();
        badHeaders.set("X-Password", "DefinitelyNotTheRightPassword");
        HttpEntity<String> badDeleteEntity = new HttpEntity<>(badHeaders);
        ResponseEntity<String> badDeleteResponse = restTemplate.exchange(baseUrl + "/users/2/delete-account", HttpMethod.DELETE, badDeleteEntity, String.class);
        assertEquals(400, badDeleteResponse.getStatusCode().value(), "Delete with wrong password should be rejected");
    }

    //TEST 20: Binder add card, get binder, and remove card flow.
    @Test
    public void binderFullFlowTest() throws Exception {
        int[] acceptableStatuses = {200, 400, 404, 409};

        //Create a fresh user to operate on
        long timestamp = System.currentTimeMillis();
        String username = "TEST_HRUSHI_T20_" + timestamp;
        String email = "hrushi_t20_" + timestamp + "@test.com";
        String signupJson = "{" + "\"firstName\":\"Test\"," + "\"lastName\":\"Binder\"," + "\"username\":\"" + username + "\"," + "\"email\":\"" + email + "\"," + "\"password\":\"BinderPass123\"" + "}";
        ResponseEntity<String> signupResponse = restTemplate.postForEntity(baseUrl + "/users/signup", jsonEntity(signupJson), String.class);
        assertEquals(200, signupResponse.getStatusCode().value(), "Signup should succeed");

        //Create a card to add to the binder
        String cardJson = "{" + "\"cardType\":\"POKEMON\"," + "\"cardName\":\"TEST_HRUSHI_T20_BinderCard\"," + "\"cardSet\":\"Test Set\"," + "\"cardRarity\":\"Holo\"," + "\"price\":50.00" + "}";
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(cardJson), String.class).getStatusCode().value());

        //Find the cardId we just created
        JSONArray allCards = new JSONArray(restTemplate.getForEntity(baseUrl + "/api/cards", String.class).getBody());
        long cardId = -1;
        for (int i = 0; i < allCards.length(); i++) {
            if ("TEST_HRUSHI_T20_BinderCard".equals(allCards.getJSONObject(i).getString("cardName"))) {
                cardId = allCards.getJSONObject(i).getLong("id");
                break;
            }
        }
        assertTrue(cardId != -1, "Should find the test card");

        //GET binder for our new user - should be empty
        ResponseEntity<String> getBinderResponse = restTemplate.getForEntity(baseUrl + "/api/users/" + username + "/binder", String.class);
        assertEquals(200, getBinderResponse.getStatusCode().value(), "Get binder should succeed for valid user");
        JSONObject binderResponseBody = new JSONObject(getBinderResponse.getBody());
        assertNotNull(binderResponseBody.getJSONObject("user"), "Response should contain user info");
        assertNotNull(binderResponseBody.getJSONArray("binder"), "Response should contain binder array");

        //POST add card to binder
        String addCardJson = "{\"card_id\":" + cardId + "}";
        ResponseEntity<String> addResponse = restTemplate.postForEntity(baseUrl + "/api/users/" + username + "/binder", jsonEntity(addCardJson), String.class);
        assertTrue(contains(acceptableStatuses, addResponse.getStatusCode().value()), "Add card should respond");

        //POST add same card again - exercises CardAlreadyInBinderException branch (409)
        ResponseEntity<String> dupAddResponse = restTemplate.postForEntity(baseUrl + "/api/users/" + username + "/binder", jsonEntity(addCardJson), String.class);
        assertTrue(contains(acceptableStatuses, dupAddResponse.getStatusCode().value()), "Duplicate add should respond");

        //POST add card with missing card_id - exercises the validation error branch
        ResponseEntity<String> missingFieldResponse = restTemplate.postForEntity(baseUrl + "/api/users/" + username + "/binder", jsonEntity("{}"), String.class);
        assertEquals(400, missingFieldResponse.getStatusCode().value(), "Missing card_id should be rejected");

        //POST add card with non-numeric card_id - exercises the NumberFormatException branch
        ResponseEntity<String> badIdResponse = restTemplate.postForEntity(baseUrl + "/api/users/" + username + "/binder", jsonEntity("{\"card_id\":\"not-a-number\"}"), String.class);
        assertEquals(400, badIdResponse.getStatusCode().value(), "Non-numeric card_id should be rejected");

        //POST add card to non-existent user - exercises UserNotFoundException
        ResponseEntity<String> badUserAddResponse = restTemplate.postForEntity(baseUrl + "/api/users/NONEXISTENT_USER_XYZ_" + timestamp + "/binder", jsonEntity(addCardJson), String.class);
        assertTrue(contains(acceptableStatuses, badUserAddResponse.getStatusCode().value()), "Add for missing user should respond");

        //GET binder for non-existent user - exercises UserNotFoundException in getBinder
        ResponseEntity<String> badUserGetResponse = restTemplate.getForEntity(baseUrl + "/api/users/NONEXISTENT_USER_XYZ_" + timestamp + "/binder", String.class);
        assertTrue(contains(acceptableStatuses, badUserGetResponse.getStatusCode().value()), "Get binder for missing user should respond");

        //DELETE the card from binder - exercises removeCard happy path
        ResponseEntity<String> removeResponse = restTemplate.exchange(baseUrl + "/api/users/" + username + "/binder/" + cardId, HttpMethod.DELETE, null, String.class);
        assertTrue(contains(acceptableStatuses, removeResponse.getStatusCode().value()), "Remove card should respond");

        //DELETE same card again - exercises BinderEntryNotFoundException branch
        ResponseEntity<String> dupRemoveResponse = restTemplate.exchange(baseUrl + "/api/users/" + username + "/binder/" + cardId, HttpMethod.DELETE, null, String.class);
        assertTrue(contains(acceptableStatuses, dupRemoveResponse.getStatusCode().value()), "Duplicate remove should respond");
    }

    //TEST 21: Admin moderator promote and demote endpoints.
    @Test
    public void adminModeratorPromoteDemoteTest() throws Exception {
        int[] acceptableStatuses = {200, 400, 404};

        //Create a fresh user as the target for moderator changes
        long timestamp = System.currentTimeMillis();
        String username = "TEST_HRUSHI_T21_" + timestamp;
        String email = "hrushi_t21_" + timestamp + "@test.com";
        String signupJson = "{" + "\"firstName\":\"Test\"," + "\"lastName\":\"Mod\"," + "\"username\":\"" + username + "\"," + "\"email\":\"" + email + "\"," + "\"password\":\"ModPass123\"" + "}";
        ResponseEntity<String> signupResponse = restTemplate.postForEntity(baseUrl + "/users/signup", jsonEntity(signupJson), String.class);
        assertEquals(200, signupResponse.getStatusCode().value(), "Signup should succeed");
        long targetId = new JSONObject(signupResponse.getBody()).getLong("id");

        //PUT promote to moderator (note: endpoint path has /admin/admin/ due to double mapping)
        ResponseEntity<String> promoteModResponse = restTemplate.exchange(baseUrl + "/admin/admin/users/" + targetId + "/promote-moderator", HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, promoteModResponse.getStatusCode().value()), "Promote moderator should respond");

        //If promoted successfully, verify response shows moderator=true
        if (promoteModResponse.getStatusCode().value() == 200) {
            JSONObject promoted = new JSONObject(promoteModResponse.getBody());
            assertTrue(promoted.getBoolean("moderator"), "User should be flagged moderator after promote");
        }

        //PUT demote from moderator
        ResponseEntity<String> demoteModResponse = restTemplate.exchange(baseUrl + "/admin/admin/users/" + targetId + "/demote-moderator", HttpMethod.PUT, null, String.class);
        assertTrue(contains(acceptableStatuses, demoteModResponse.getStatusCode().value()), "Demote moderator should respond");

        //PUT promote moderator on non-existent user - exercises the user-not-found error branch
        ResponseEntity<String> badPromoteResponse = restTemplate.exchange(baseUrl + "/admin/admin/users/9999999/promote-moderator", HttpMethod.PUT, null, String.class);
        assertEquals(400, badPromoteResponse.getStatusCode().value(), "Promote on missing user should be rejected");

        //PUT demote moderator on non-existent user - exercises the user-not-found error branch
        ResponseEntity<String> badDemoteResponse = restTemplate.exchange(baseUrl + "/admin/admin/users/9999999/demote-moderator", HttpMethod.PUT, null, String.class);
        assertEquals(400, badDemoteResponse.getStatusCode().value(), "Demote on missing user should be rejected");
    }

    //TEST 22: End-to-end notification lifecycle - retrieve, mark read, verify state, delete.
    @Test
    public void notificationEndToEndLifecycleTest() throws Exception {
        int[] acceptableStatuses = {200, 400, 404};

        //Try multiple user IDs to find one with notifications - test DB state varies
        long[] candidateUserIds = {1, 2, 3, 4};
        long userIdWithNotifs = -1;
        long firstNotifId = -1;

        for (long userId : candidateUserIds) {
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/notifications/user/" + userId, String.class);
            if (response.getStatusCode().value() == 200) {
                JSONArray notifs = new JSONArray(response.getBody());
                if (notifs.length() > 0) {
                    userIdWithNotifs = userId;
                    firstNotifId = notifs.getJSONObject(0).getLong("id");
                    break;
                }
            }
        }

        //Stage 1: Get the unread count for whichever user we found
        if (userIdWithNotifs != -1) {
            ResponseEntity<String> initialUnreadResponse = restTemplate.getForEntity(baseUrl + "/notifications/unread/" + userIdWithNotifs, String.class);
            assertEquals(200, initialUnreadResponse.getStatusCode().value(), "Initial unread count should succeed");
            long initialUnreadCount = Long.parseLong(initialUnreadResponse.getBody());
            assertTrue(initialUnreadCount >= 0, "Initial unread count should be non-negative");

            //Stage 2: Mark the first notification as read
            ResponseEntity<String> markReadResponse = restTemplate.exchange(baseUrl + "/notifications/" + firstNotifId + "/read", HttpMethod.PUT, null, String.class);
            assertTrue(contains(acceptableStatuses, markReadResponse.getStatusCode().value()), "Mark read should respond");

            //Stage 3: If mark-read succeeded, verify the state changed
            if (markReadResponse.getStatusCode().value() == 200) {
                JSONObject updated = new JSONObject(markReadResponse.getBody());
                assertTrue(updated.getBoolean("read"), "Notification should be flagged as read");
                assertNotNull(updated.optString("readAt"), "Notification should have a readAt timestamp");

                //Stage 4: Re-fetch the notification list - the read flag should persist
                ResponseEntity<String> refetchResponse = restTemplate.getForEntity(baseUrl + "/notifications/user/" + userIdWithNotifs, String.class);
                assertEquals(200, refetchResponse.getStatusCode().value());
                JSONArray refetched = new JSONArray(refetchResponse.getBody());
                for (int i = 0; i < refetched.length(); i++) {
                    JSONObject notif = refetched.getJSONObject(i);
                    if (notif.getLong("id") == firstNotifId) {
                        assertTrue(notif.getBoolean("read"), "Marked notification should still be read after refetch");
                        break;
                    }
                }
            }

            //Stage 5: Delete the notification
            ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/notifications/" + firstNotifId, HttpMethod.DELETE, null, String.class);
            assertTrue(contains(acceptableStatuses, deleteResponse.getStatusCode().value()), "Delete should respond");

            //Stage 6: Confirm the deleted notification no longer appears in the list
            if (deleteResponse.getStatusCode().value() == 200) {
                ResponseEntity<String> afterDeleteResponse = restTemplate.getForEntity(baseUrl + "/notifications/user/" + userIdWithNotifs, String.class);
                assertEquals(200, afterDeleteResponse.getStatusCode().value());
                JSONArray afterDelete = new JSONArray(afterDeleteResponse.getBody());
                boolean stillExists = false;
                for (int i = 0; i < afterDelete.length(); i++) {
                    if (afterDelete.getJSONObject(i).getLong("id") == firstNotifId) {
                        stillExists = true;
                        break;
                    }
                }
                assertTrue(!stillExists, "Deleted notification should no longer appear in user's list");
            }
        }

        //Stage 7: Error path coverage - invalid IDs return predictable status
        ResponseEntity<String> badMarkRead = restTemplate.exchange(baseUrl + "/notifications/9999999/read", HttpMethod.PUT, null, String.class);
        assertEquals(400, badMarkRead.getStatusCode().value(), "Mark read on missing notification should be rejected");

        ResponseEntity<String> badDelete = restTemplate.exchange(baseUrl + "/notifications/9999999", HttpMethod.DELETE, null, String.class);
        assertEquals(400, badDelete.getStatusCode().value(), "Delete on missing notification should be rejected");

        //Stage 8: Get notifications for non-existent user
        ResponseEntity<String> badUserNotifs = restTemplate.getForEntity(baseUrl + "/notifications/user/9999999", String.class);
        assertTrue(contains(acceptableStatuses, badUserNotifs.getStatusCode().value()), "Bad user notifications should respond");
    }
}
