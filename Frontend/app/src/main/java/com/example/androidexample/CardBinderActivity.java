package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CardBinderActivity extends AppCompatActivity {
    /**
     * Search field (not used)
     */
    private EditText searchEditText;
    /**
     * Search button (not used)
     */
    private Button btnSearch;
    /**
     * Card view component
     */
    private CardView cardView;
    /**
     * Image for the card view component
     */
    private ImageView cardImage;
    /**
     * URL for the card
     */
    private String cardUrl;
    /**
     * Name of the card
     */
    private TextView cardName;
    /**
     * Type of card
     */
    private TextView cardType;
    /**
     * Set the card belongs to
     */
    private TextView cardSet;
    /**
     * The rarity of the card
     */
    private TextView cardRarity;
    /**
     * Price of the card
     */
    private TextView cardPrice;
    /**
     * Card name edit field.
     */
    private EditText cardNameEdit;
    /**
     * Card type edit field.
     */
    private EditText cardTypeEdit;
    /**
     * Card set edit field
     */
    private EditText cardSetEdit;
    /**
     * Card rarity edit field
     */
    private EditText cardRarityEdit;
    /**
     * Card price edit field.
     */
    private EditText cardPriceEdit;
    /**
     * Card edit UI open button
     */
    private Button cardEditBtn;
    /**
     * Card save button field
     */
    private Button cardSaveBtn;
    /**
     * Card list container
     */
    private LinearLayout cardListContainer;
    /**
     * JSON Array of cards (Not used)
     */
    private JSONArray cards;
    /**
     * Individual selected card (not used)
     */
    private  JSONObject card;
    /**
     * The current chosen card ID.
     */
    private String currentCardId;

    private ImageView returnToMain;
    private ImageButton cardDetailsButton;
    private ImageButton toChatsButton;

    private int id;
    /**
     * The username for the user.
     */
    private String username;
    /**
     * Check for if the user is an admin or not.
     */
    private boolean isAdmin;
    /**
     * The base URL for the backend.
     */
    private static final String BASE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/users/";

    /**
     * Runs on activity start
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardbinder);

        //Card information
        cardView            = findViewById(R.id.card_view);
        cardImage           = findViewById(R.id.card_image);
        cardName            = findViewById(R.id.card_name);
        cardType            = findViewById(R.id.card_type);
        cardSet             = findViewById(R.id.card_set);
        cardRarity          = findViewById(R.id.card_rarity);
        cardPrice           = findViewById(R.id.card_price);

        //cardListcontainer
        cardListContainer   = findViewById(R.id.cardLookup_card_list_container);

        //Editing
        cardNameEdit   = findViewById(R.id.card_name_edit);
        cardTypeEdit   = findViewById(R.id.card_type_edit);
        cardSetEdit    = findViewById(R.id.card_set_edit);
        cardRarityEdit = findViewById(R.id.card_rarity_edit);
        cardPriceEdit  = findViewById(R.id.card_price_edit);

        returnToMain = findViewById(R.id.cardBinder_home_image);
        cardDetailsButton = findViewById(R.id.cardBinder_toSearch_image);
        toChatsButton = findViewById(R.id.cardBinder_tochats_btn);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            id = -1;
            isAdmin = false;
        } else {
            id = extras.getInt("id", -1);
            isAdmin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username"); // used when moving back to the main view.
        }

        returnToMain.setOnClickListener(v -> {
            Intent intent = new Intent(CardBinderActivity.this, MainActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        toChatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(CardBinderActivity.this, LiveChatMenuActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        cardDetailsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(CardBinderActivity.this, CardSearchActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        fetchCards();
    }

    /**
     * Handles creating and clonind cards into the card container via inflation.
     * @param response
     * @throws JSONException
     */
    private void handlePercolate(JSONArray response) throws JSONException {
        cardListContainer.removeAllViews();

        for (int i = 0; i < response.length(); i++) {
            JSONObject c = response.getJSONObject(i);

            View inflated = LayoutInflater.from(this)
                    .inflate(R.layout.activity_cardbinder, null, false);

            CardView clonedCard = inflated.findViewById(R.id.card_view);

            String imgUrl = c.optString("imageUrl");
            String cardId = String.valueOf(c.getLong("id"));

            // Populate display fields
            ((TextView) clonedCard.findViewById(R.id.card_name)).setText("Name: "    + c.getString("cardName"));
            ((TextView) clonedCard.findViewById(R.id.card_type)).setText("Type: "    + c.getString("cardType"));
            ((TextView) clonedCard.findViewById(R.id.card_set)).setText("Set: "      + c.getString("cardSet"));
            ((TextView) clonedCard.findViewById(R.id.card_rarity)).setText("Rarity: "+ c.getString("cardRarity"));
            ((TextView) clonedCard.findViewById(R.id.card_price)).setText("Price: $" + String.format("%.2f", c.getDouble("price")));

            Glide.with(this).load(imgUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into((ImageView) clonedCard.findViewById(R.id.card_image));

            clonedCard.setVisibility(View.VISIBLE);

            CandleStickChart chart = clonedCard.findViewById(R.id.candleStick);
            fetchAndRenderChart(cardId, chart);

            Button btnBinderRemove = clonedCard.findViewById(R.id.card_removeFromBinder_btn);

            btnBinderRemove.setVisibility(View.VISIBLE);
            btnBinderRemove.setOnClickListener(v-> deleteCard(cardId));

            ((ViewGroup) clonedCard.getParent()).removeView(clonedCard);
            cardListContainer.addView(clonedCard);
        }
    }

    /**
     * Handles GET /api/username/binder
     */
    private void fetchCards() {
        String url = BASE_URL + username + "/binder";

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d("GET all Binder Cards", response.toString());
                    try {
                        JSONArray binderArray = response.getJSONArray("binder");
                        JSONArray cards = new JSONArray();

                        for (int i = 0; i < binderArray.length(); i++) {
                            JSONObject binderEntry = binderArray.getJSONObject(i);
                            JSONObject card = binderEntry.getJSONObject("card");
                            cards.put(card);
                        }

                        if (cards.length() > 0) {
                            currentCardId = String.valueOf(cards.getJSONObject(0).getLong("id"));
                        }

                        handlePercolate(cards);

                    } catch (JSONException e) {
                        Log.e("BINDER fetch error", e.getMessage());
                    }
                },
                error -> {
                    Log.e("BINDER fetch error", error.toString());
                    Toast.makeText(this, "No cards found", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { return new HashMap<>(); }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Deletes a card from the user's binder.
     * @param id id of the card
     */
    private void deleteCard(String id) {
        String url = BASE_URL + username + "/binder/" + id;

        StringRequest request = new StringRequest(
                Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(this, "card removed from binder", Toast.LENGTH_SHORT).show();
                    fetchCards();
                }, error -> {
                    String msg = "remove failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    /**
     * handles PUT /api/cards/{id} (unused)
     * @param cardId the card ID
     * @param imgUrl the image Url for the card
     * @param card the card's parent component.
     */
    private void makeJsonObjPutReq(String cardId, String imgUrl, CardView card) {

        EditText editName = card.findViewById(R.id.card_name_edit);
        EditText editType = card.findViewById(R.id.card_type_edit);
        EditText editSet = card.findViewById(R.id.card_set_edit);
        EditText editRarity = card.findViewById(R.id.card_rarity_edit);
        EditText editPrice = card.findViewById(R.id.card_price_edit);
        TextView currName = card.findViewById(R.id.card_name);
        TextView currType = card.findViewById(R.id.card_type);
        TextView currSet = card.findViewById(R.id.card_set);
        TextView currRarity = card.findViewById(R.id.card_rarity);
        TextView currPrice = card.findViewById(R.id.card_price);
        Button btnEdit    = card.findViewById(R.id.card_edit_btn);
        Button btnSave    = card.findViewById(R.id.card_save_btn);
        String priceText = editPrice.getText().toString().trim();

        if (priceText.isEmpty()) {
            Toast.makeText(this, "Price cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject updatedCard = new JSONObject();
            updatedCard.put("imageUrl",   imgUrl);
            updatedCard.put("cardName",   editName.getText().toString().trim());
            updatedCard.put("cardType",   editType.getText().toString().trim());
            updatedCard.put("cardSet",    editSet.getText().toString().trim());
            updatedCard.put("cardRarity", editRarity.getText().toString().trim());
            updatedCard.put("price",      Double.parseDouble(priceText));

            JsonObjectRequest putRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    BASE_URL + "/" + cardId,
                    updatedCard,
                    response -> {
                        try {
                            currName.setText("Name: "    + response.getString("cardName"));
                            currType.setText("Type: "    + response.getString("cardType"));
                            currSet.setText("Set: "      + response.getString("cardSet"));
                            currRarity.setText("Rarity: "+ response.getString("cardRarity"));
                            currPrice.setText("Price: $" + String.format("%.2f", response.getDouble("price")));
                        } catch (JSONException e) {
                            Log.e("PUT error:", e.getMessage());
                        }
                        currName.setVisibility(View.VISIBLE);   editName.setVisibility(View.GONE);
                        currType.setVisibility(View.VISIBLE);   editType.setVisibility(View.GONE);
                        currSet.setVisibility(View.VISIBLE);    editSet.setVisibility(View.GONE);
                        currRarity.setVisibility(View.VISIBLE); editRarity.setVisibility(View.GONE);
                        currPrice.setVisibility(View.VISIBLE);  editPrice.setVisibility(View.GONE);
                        btnEdit.setVisibility(View.VISIBLE);  btnSave.setVisibility(View.GONE);
                        Toast.makeText(this, "Card saved.", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Log.e("PUT error", error.toString());
                        Toast.makeText(this, "Error saving card edits.", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override public Map<String, String> getHeaders() throws AuthFailureError { return new HashMap<>(); }
            };

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(putRequest);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Price must be a valid number.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e("PUT error:", Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Fetches pricing data and renders the chart.
     * @param cardId the card ID
     * @param chart the chart component
     */
    private void fetchAndRenderChart(String cardId, CandleStickChart chart) {
        String url = "http://coms-3090-022.class.las.iastate.edu:8080/api/prices/card/" + cardId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Group prices by date (yyyy-MM-dd)
                        Map<String, List<Double>> byDay = new LinkedHashMap<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject rec = response.getJSONObject(i);
                            double price = rec.getDouble("price");
                            // recordedAt format: "2024-01-15T10:30:00"
                            String dateTime = rec.getString("recordedAt");
                            String day = dateTime.substring(0, 10); // "yyyy-MM-dd"

                            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(price);
                        }

                        List<CandleEntry> entries = new ArrayList<>();
                        int x = 0;
                        for (Map.Entry<String, List<Double>> entry : byDay.entrySet()) {
                            List<Double> prices = entry.getValue();
                            float open  = prices.get(0).floatValue();
                            float close = prices.get(prices.size() - 1).floatValue();
                            float high  = Collections.max(prices).floatValue();
                            float low   = Collections.min(prices).floatValue();

                            entries.add(new CandleEntry(x++, high, low, open, close));
                        }

                        renderCandleChart(chart, entries);

                    } catch (JSONException e) {
                        Log.e("Chart error", e.getMessage());
                    }
                },
                error -> Log.e("Chart fetch error", error.toString())
        ) {
            @Override public Map<String, String> getHeaders() {
                return new HashMap<>();
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Renders the candle stick chart
     * @param chart the chart component
     * @param entries A list of candleEntry data.
     */
    private void renderCandleChart(CandleStickChart chart, List<CandleEntry> entries) {
        if (entries.isEmpty()) {
            chart.setVisibility(View.GONE);
            return;
        }

        CandleDataSet dataSet = new CandleDataSet(entries, "Price History");
        dataSet.setShadowColor(Color.DKGRAY);
        dataSet.setShadowWidth(0.7f);
        dataSet.setDecreasingColor(Color.RED);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(Color.rgb(40, 200, 40));
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        dataSet.setNeutralColor(Color.BLUE);
        dataSet.setDrawValues(false);

        CandleData candleData = new CandleData(dataSet);
        chart.setData(candleData);

        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);

        chart.setVisibility(View.VISIBLE);
        chart.invalidate();
    }

    /**
     * Toggles the editing mode (unused)
     * @param editing To determine if a card is being edited.
     */
    private void toggleEditMode(boolean editing) {
        cardName.setVisibility(editing ? View.GONE : View.VISIBLE);
        cardNameEdit.setVisibility(editing ? View.VISIBLE : View.GONE);

        cardType.setVisibility(editing ? View.GONE : View.VISIBLE);
        cardTypeEdit.setVisibility(editing ? View.VISIBLE : View.GONE);

        cardSet.setVisibility(editing ? View.GONE : View.VISIBLE);
        cardSetEdit.setVisibility(editing ? View.VISIBLE : View.GONE);

        cardRarity.setVisibility(editing ? View.GONE : View.VISIBLE);
        cardRarityEdit.setVisibility(editing ? View.VISIBLE : View.GONE);

        cardPrice.setVisibility(editing ? View.GONE : View.VISIBLE);
        cardPriceEdit.setVisibility(editing ? View.VISIBLE : View.GONE);

        cardEditBtn.setVisibility(editing ? View.GONE : View.VISIBLE);
        cardSaveBtn.setVisibility(editing ? View.VISIBLE : View.GONE);

        if (editing) {
            cardNameEdit.setText(cardName.getText().toString().replace("Name: ", ""));
            cardTypeEdit.setText(cardType.getText().toString().replace("Type: ", ""));
            cardSetEdit.setText(cardSet.getText().toString().replace("Set: ", ""));
            cardRarityEdit.setText(cardRarity.getText().toString().replace("Rarity: ", ""));
            cardPriceEdit.setText(cardPrice.getText().toString().replace("Price: $", ""));
        }
    }
}