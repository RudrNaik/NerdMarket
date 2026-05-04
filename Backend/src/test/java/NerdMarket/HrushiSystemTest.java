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

    // Helper method: builds a JSON HttpEntity for POST/PUT requests
    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    //Test 1: Full CRUD lifecycle on Market endpoints.
    @Test
    public void marketCrudLifecycleTest() throws Exception {
        // 1. POST: create a new card
        String cardJson = "{"
                + "\"cardType\":\"POKEMON\","
                + "\"cardName\":\"TEST_HRUSHI_Charizard\","
                + "\"cardSet\":\"Test Set\","
                + "\"cardRarity\":\"Holo\","
                + "\"price\":100.00,"
                + "\"imageUrl\":\"http://example.com/charizard.png\""
                + "}";

        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(cardJson), String.class);

        assertEquals(200, createResponse.getStatusCode().value());
        assertTrue(createResponse.getBody().contains("success"), "POST response should contain 'success'");

        // 2. GET all cards, find the one we just created
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

        // 3. GET by ID, verify the field values were stored correctly
        ResponseEntity<String> getByIdResponse = restTemplate.getForEntity(baseUrl + "/api/cards/" + cardId, String.class);
        assertEquals(200, getByIdResponse.getStatusCode().value());
        assertNotNull(getByIdResponse.getBody(), "Expected a card body");
        JSONObject fetchedCard = new JSONObject(getByIdResponse.getBody());
        assertEquals("POKEMON", fetchedCard.getString("cardType"));
        assertEquals("Holo", fetchedCard.getString("cardRarity"));
        assertEquals(100.00, fetchedCard.getDouble("price"), 0.001);

        // 4. DELETE the card and verify success
        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/api/cards/" + cardId, HttpMethod.DELETE, null, String.class);

        assertEquals(200, deleteResponse.getStatusCode().value());
        assertTrue(deleteResponse.getBody().contains("success"), "DELETE response should contain 'success'");
    }

    //Test 2: Verify filtering cards by type returns only cards of that type.
    @Test
    public void cardTypeFilteringTest() throws Exception {
        // 1. POST 2 POKEMON cards
        String pokemon1 = "{"
                + "\"cardType\":\"POKEMON\","
                + "\"cardName\":\"TEST_HRUSHI_Pikachu\","
                + "\"cardSet\":\"Test Set\","
                + "\"cardRarity\":\"Common\","
                + "\"price\":5.00"
                + "}";
        String pokemon2 = "{"
                + "\"cardType\":\"POKEMON\","
                + "\"cardName\":\"TEST_HRUSHI_Mewtwo\","
                + "\"cardSet\":\"Test Set\","
                + "\"cardRarity\":\"Holo\","
                + "\"price\":50.00"
                + "}";
        // 2. POST 1 MTG card
        String mtg1 = "{"
                + "\"cardType\":\"MTG\","
                + "\"cardName\":\"TEST_HRUSHI_BlackLotus\","
                + "\"cardSet\":\"Alpha\","
                + "\"cardRarity\":\"Rare\","
                + "\"price\":10000.00"
                + "}";

        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(pokemon1), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(pokemon2), String.class).getStatusCode().value());
        assertEquals(200, restTemplate.postForEntity(baseUrl + "/api/cards", jsonEntity(mtg1), String.class).getStatusCode().value());

        // 3. GET POKEMON cards: should contain both Pikachu and Mewtwo, but NOT Black Lotus
        ResponseEntity<String> pokemonResponse = restTemplate.getForEntity(baseUrl + "/api/cards/type/POKEMON", String.class);
        assertEquals(200, pokemonResponse.getStatusCode().value());
        JSONArray pokemonCards = new JSONArray(pokemonResponse.getBody());
        assertTrue(pokemonCards.length() >= 2, "Expected at least 2 POKEMON cards, got " + pokemonCards.length());
        boolean foundPikachu = false;
        boolean foundMewtwo = false;
        boolean foundBlackLotus = false;
        for (int i = 0; i < pokemonCards.length(); i++) {
            JSONObject c = pokemonCards.getJSONObject(i);
            // Every result must actually be a POKEMON card
            assertEquals("POKEMON", c.getString("cardType"), "POKEMON filter returned a non-POKEMON card: " + c.getString("cardName"));
            String name = c.getString("cardName");
            if ("TEST_HRUSHI_Pikachu".equals(name)) foundPikachu = true;
            if ("TEST_HRUSHI_Mewtwo".equals(name)) foundMewtwo = true;
            if ("TEST_HRUSHI_BlackLotus".equals(name)) foundBlackLotus = true;
        }
        assertTrue(foundPikachu, "Pikachu should appear in POKEMON results");
        assertTrue(foundMewtwo, "Mewtwo should appear in POKEMON results");
        assertTrue(!foundBlackLotus, "Black Lotus (MTG) should NOT appear in POKEMON results");

        // 4. GET MTG cards: should contain Black Lotus, but NOT the POKEMON cards
        ResponseEntity<String> mtgResponse = restTemplate.getForEntity(baseUrl + "/api/cards/type/MTG", String.class);
        assertEquals(200, mtgResponse.getStatusCode().value());
        JSONArray mtgCards = new JSONArray(mtgResponse.getBody());
        assertTrue(mtgCards.length() >= 1, "Expected at least 1 MTG card, got " + mtgCards.length());
        boolean foundBlackLotusInMtg = false;
        for (int i = 0; i < mtgCards.length(); i++) {
            JSONObject c = mtgCards.getJSONObject(i);
            // Every result must actually be MTG
            assertEquals("MTG", c.getString("cardType"), "MTG filter returned a non-MTG card: " + c.getString("cardName"));
            String name = c.getString("cardName");
            if ("TEST_HRUSHI_BlackLotus".equals(name)) foundBlackLotusInMtg = true;
            // Sanity check: POKEMON cards must not appear in MTG results
            assertTrue(!"TEST_HRUSHI_Pikachu".equals(name), "Pikachu (POKEMON) should NOT appear in MTG results");
            assertTrue(!"TEST_HRUSHI_Mewtwo".equals(name), "Mewtwo (POKEMON) should NOT appear in MTG results");
        }
        assertTrue(foundBlackLotusInMtg, "Black Lotus should appear in MTG results");
    }
}