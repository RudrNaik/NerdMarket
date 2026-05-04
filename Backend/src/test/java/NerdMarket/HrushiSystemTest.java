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
}