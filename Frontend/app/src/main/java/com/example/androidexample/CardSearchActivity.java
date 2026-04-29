package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CardSearchActivity extends AppCompatActivity {

    /**
     * search field for the card search
     */
    private EditText searchEditText;
    /**
     * button to search for specific card.
     */
    private Button btnSearch;
    /**
     * Cardview container for the cards
     */
    private CardView cardView;
    /**
     * Imageview container for the card's image
     */
    private ImageView cardImage;
    /**
     * Imageview container for the cameras search button
     */
    private ImageView cameraSearch;
    /**
     * The card's name
     */
    private TextView cardName;
    /**
     * The card's type
     */
    private TextView cardType;
    /**
     * the card's set.
     */
    private TextView cardSet;
    /**
     * The card/s rarity
     */
    private TextView cardRarity;
    /**
     * The card's price
     */
    private TextView cardPrice;
    /**
     * Card name edit field
     */
    private EditText cardNameEdit;
    /**
     * Card type edit field
     */
    private EditText cardTypeEdit;
    /**
     * Card set edit field.
     */
    private EditText cardSetEdit;
    /**
     * Card rarity edit field
     */
    private EditText cardRarityEdit;
    /**
     * Card price edit field
     */
    private EditText cardPriceEdit;
    /**
     * Card editing button
     */
    private Button cardEditBtn;
    /**
     * Card save button
     */
    private Button cardSaveBtn;
    /**
     * Save to binder button
     */
    private Button cardBinderSavebtn;
    /**
     * Card remove from binder button (unused)
     */
    private Button cardBinderRemovebtn;
    /**
     * Card list container
     */
    private LinearLayout cardListContainer;

    /**
     * json array of the cards returned from search
     */
    private JSONArray cards;
    /**
     * the current card's id
     */
    private String currentCardId;

    private ImageView returnToMain;
    private ImageButton cardBinderButton;
    private Button toBiggestMovers;
    private int id;
    /**
     * Card's Id from bundle
     */
    private int bundleCardID;
    /**
     * String for the card's iD via bundle
     */
    private String bundleCardIDString;
    /**
     * User's username
     */
    private String username;
    /**
     * Bool to check if user is admin.
     */
    private boolean isAdmin;
    /**
     * Base url to the backend
     */
    private static final String BASE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/cards";

    /**
     * Runs on activity start.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardlookup);

        //Search
        btnSearch      = findViewById(R.id.card_search_btn);
        searchEditText = findViewById(R.id.card_search_field);
        cameraSearch   = findViewById(R.id.Search_camera_btn);

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
        cardEditBtn    = findViewById(R.id.card_edit_btn);
        cardSaveBtn    = findViewById(R.id.card_save_btn);

        //Saving cards to binder
        cardBinderSavebtn = findViewById(R.id.card_addTobinder_btn);

        //Nav to main.
        returnToMain = findViewById(R.id.cardlookup_to_main_button);
        toBiggestMovers = findViewById(R.id.cardLookup_tobiggestmovers_btn);
        cardBinderButton = findViewById(R.id.cardLookup_toPortfolio_image);

        Bundle extras = getIntent().getExtras();

        if (extras == null) {
            id = -1;
            isAdmin = false;
        } else {
            id = extras.getInt("id", -1);
            isAdmin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username"); // used when moving back to the main view.
            bundleCardID = extras.getInt("bundleCardID");
            bundleCardIDString = String.valueOf(bundleCardID);
        }
        cameraSearch.setOnClickListener(v -> {
            Intent intent = new Intent(CardSearchActivity.this, CameraSearchActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("username", username);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            toBiggestMovers.setVisibility(View.INVISIBLE);
            handleSearch();
        });

        toBiggestMovers.setOnClickListener(v -> {
            Intent intent = new Intent(CardSearchActivity.this, MoversActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        cardBinderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(CardSearchActivity.this, CardBinderActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        cardEditBtn.setOnClickListener(v -> toggleEditMode(true));

        returnToMain.setOnClickListener(v -> {
            Intent intent = new Intent(CardSearchActivity.this, MainActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        if (!bundleCardIDString.isEmpty()) {
            fetchCardById(bundleCardIDString);
            bundleCardIDString = null;
        }
    }

    /**
     * Handler for searching for a card.
     */
    private void handleSearch() {
        String query = searchEditText.getText().toString();

        if (query.isEmpty()) {
            Toast.makeText(this, "Enter a card name", Toast.LENGTH_SHORT).show();
            return;
        }

        cardView.setVisibility(View.INVISIBLE);

        currentCardId = query;
        if (Character.isDigit(query.charAt(0))){
            fetchCardById(query);
        } else{
            fetchCardByName(query);
        }
    }

    /**
     * Handles percolating all the cards into the card container.
     * @param response The return value from the backend's search
     * @throws JSONException
     */
    private void handlePercolate(JSONArray response) throws JSONException {
        cardListContainer.removeAllViews();

        for (int i = 0; i < response.length(); i++) {
            JSONObject c = response.getJSONObject(i);

            View inflated = LayoutInflater.from(this)
                    .inflate(R.layout.activity_cardlookup, null, false);

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

            Button btnEdit = clonedCard.findViewById(R.id.card_edit_btn);

            //blocks the button from ever being visible if not an admin
            if(!isAdmin){
                btnEdit.setVisibility(View.GONE);
            }

            Button btnSave = clonedCard.findViewById(R.id.card_save_btn);

            Button btnBinderSave   = clonedCard.findViewById(R.id.card_addTobinder_btn);

            btnBinderSave.setOnClickListener(v -> addCardToBinder(cardId));

            btnEdit.setOnClickListener(v -> {
                // Copy current info into the edit fields
                ((EditText) clonedCard.findViewById(R.id.card_name_edit)).setText(((TextView) clonedCard.findViewById(R.id.card_name)).getText().toString().replace("Name: ", ""));
                ((EditText) clonedCard.findViewById(R.id.card_type_edit)).setText(((TextView) clonedCard.findViewById(R.id.card_type)).getText().toString().replace("Type: ", ""));
                ((EditText) clonedCard.findViewById(R.id.card_set_edit)).setText(((TextView) clonedCard.findViewById(R.id.card_set)).getText().toString().replace("Set: ", ""));
                ((EditText) clonedCard.findViewById(R.id.card_rarity_edit)).setText(((TextView) clonedCard.findViewById(R.id.card_rarity)).getText().toString().replace("Rarity: ", ""));
                ((EditText) clonedCard.findViewById(R.id.card_price_edit)).setText(((TextView) clonedCard.findViewById(R.id.card_price)).getText().toString().replace("Price: $", ""));

                clonedCard.findViewById(R.id.card_name).setVisibility(View.GONE);    clonedCard.findViewById(R.id.card_name_edit).setVisibility(View.VISIBLE);
                clonedCard.findViewById(R.id.card_type).setVisibility(View.GONE);    clonedCard.findViewById(R.id.card_type_edit).setVisibility(View.VISIBLE);
                clonedCard.findViewById(R.id.card_set).setVisibility(View.GONE);     clonedCard.findViewById(R.id.card_set_edit).setVisibility(View.VISIBLE);
                clonedCard.findViewById(R.id.card_rarity).setVisibility(View.GONE);  clonedCard.findViewById(R.id.card_rarity_edit).setVisibility(View.VISIBLE);
                clonedCard.findViewById(R.id.card_price).setVisibility(View.GONE);   clonedCard.findViewById(R.id.card_price_edit).setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                btnSave.setVisibility(View.VISIBLE);
            });

            btnSave.setOnClickListener(v -> makeJsonObjPutReq(cardId, imgUrl, clonedCard));

            ((ViewGroup) clonedCard.getParent()).removeView(clonedCard);
            cardListContainer.addView(clonedCard);
        }
    }

    /**
     * Handler for GET /api/cards/search/{name}
     * @param name name of the card.
     */
    private void fetchCardByName(String name) {
        String url = BASE_URL + "/search/" + name;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d("GET by name", response.toString());
                    cards = response;
                    try { currentCardId = String.valueOf(cards.getJSONObject(0).getLong("id")); }
                    catch (JSONException e) { Log.e("GET error", e.getMessage()); }
                    try {
                        handlePercolate(cards);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e("GET by name error", error.toString());
                    Toast.makeText(this, "No card found with name " + name, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { return new HashMap<>(); }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Handler for GET /api/cards/{id}
     * @param id id of the card
     */
    private void fetchCardById(String id) {
        String url = BASE_URL + "/" + id;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d("GET by id", response.toString());
                    JSONArray cards = new JSONArray();
                    cards.put(response);
                    try { currentCardId = String.valueOf(cards.getJSONObject(0).getLong("id")); }
                    catch (JSONException e) { Log.e("GET error", e.getMessage()); }
                    try {
                        cards.toString();
                        handlePercolate(cards);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.e("GET by name error", error.toString());
                    Toast.makeText(this, "No card found with id of:" + id, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { return new HashMap<>(); }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * handler for PUT /api/cards/{id}
     * @param cardId card's ID
     * @param imgUrl the image URL for the card
     * @param card the cardview component that is parent of the card.
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
     * Adds a card to the user's binder
     * @param cardId The Card's ID
     */
    private void addCardToBinder(String cardId) {
        String url = "http://coms-3090-022.class.las.iastate.edu:8080/api/users/" + username + "/binder";

        try {
            JSONObject body = new JSONObject();
            body.put("card_id", String.valueOf(cardId));

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {
                            String message = response.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.e("BINDER error:", e.getMessage());
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String errorBody = new String(error.networkResponse.data);
                            Log.e("BINDER error", statusCode + ": " + errorBody);
                        }
                        Toast.makeText(this, "Error adding card to binder.", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override public Map<String, String> getHeaders() throws AuthFailureError { return new HashMap<>(); }
            };

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e("BINDER error:", Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Fetches data related to the candlestick chart from backend, specifically prices, and then renders the chart by calling renderCandleChart.
     * @param cardId the card's ID
     * @param chart the chart component.
     */
    private void fetchAndRenderChart(String cardId, CandleStickChart chart) {
        String url = "http://coms-3090-022.class.las.iastate.edu:8080/api/prices/card/" + cardId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Group prices by date (year-month-date)
                        Map<String, List<Double>> byDay = new LinkedHashMap<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject rec = response.getJSONObject(i);
                            double price = rec.getDouble("price");
                            // recordedAt format: "2024-01-15T10:30:00"
                            String dateTime = rec.getString("recordedAt");
                            String day = dateTime.substring(0, 10); // "Year-Month-date"

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

                            // If only 1 price that day, spread slightly so candle is visible
                            if (high == low) {
                                high += 0.01f;
                                low  -= 0.01f;
                            }

                            entries.add(new CandleEntry(x++, high, low, open, close));
                        }

                        renderCandleChart(chart, entries);

                    } catch (JSONException e) {
                        Log.e("Chart error", e.getMessage());
                    }
                },
                error -> Log.e("Chart fetch error", error.toString())
        ) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError {
                return new HashMap<>();
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Renders the candlestick chart.
     * @param chart The chart component
     * @param entries the entries in a list form.
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
     * Editing mode toggle.
     * @param editing Bool to determine if you are editing it or not.
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