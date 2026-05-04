package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private TextView messageText;   // define message textview variable
    private TextView usernameText;  // define username textview variable
    private TextView portfolioValueText;
    private Button loginBackButton;
    private ImageButton cardDetailsButton;
    private Button priceCRUDButton;
    private Button biggestMoversButton;
    private Button signupBackButton;
    private Button deleteAccountButton;
    private Button toAdminButton;
    private ImageButton cardBinderButton;
    private Button toNotificationsButton;
    private ImageButton hamburgerDropdownButton;

    private ImageButton toChatsButton;
    private CandleStickChart mainChart;

    private int id;
    private String username;
    private boolean isAdmin;
    private double totalPortfolioValue = 0;
    private static final String BINDER_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/users";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        /* initialize UI elements */
        usernameText = findViewById(R.id.main_username_txt);// link to username textview in the Main activity XML
//        signupBackButton = findViewById(R.id.back_to_signup_btn);
        cardDetailsButton = findViewById(R.id.main_toSearch_image);
//        toAdminButton = findViewById(R.id.to_admin_btn);
//        loginBackButton = findViewById(R.id.back_to_login_btn);
//        signupBackButton.setVisibility(View.INVISIBLE);
//        loginBackButton.setVisibility(View.INVISIBLE);
//        deleteAccountButton = findViewById(R.id.delete_account_btn);
//        priceCRUDButton = findViewById(R.id.to_pricecrud_btn);
//        biggestMoversButton = findViewById(R.id.to_biggestmovers_btn);
        cardBinderButton = findViewById(R.id.main_toPortfolio_image);
//        toNotificationsButton = findViewById(R.id.to_notifs_btn);
        hamburgerDropdownButton = findViewById(R.id.main_dropdown_btn);
        toChatsButton = findViewById(R.id.main_tochats_btn);
        mainChart    = findViewById(R.id.candleStick);
        portfolioValueText = findViewById(R.id.main_portfolio_value_txt);



        /* extract data passed into this activity from another activity */
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            usernameText.setText("Home Page");
        } else {
            id = extras.getInt("id", -1);
            isAdmin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username", "Please log out and back in");
            usernameText.setText(extras.getString("username")); // this will come from LoginActivity
//            loginBackButton.setVisibility(View.VISIBLE);            // set new login button visible
//            signupBackButton.setVisibility(View.VISIBLE);           // set new signup button visible
//
//            //check if the user is an admin and make the admin tab visible.
//            if (isAdmin) {
//                toAdminButton.setVisibility(View.VISIBLE);
//            } else {
//                toAdminButton.setVisibility(View.GONE);
//            }

        }

        fetchBinderData(username);

        cardDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CardSearchActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        cardBinderButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CardBinderActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        toChatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LiveChatMenuActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        hamburgerDropdownButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenu().add(0, 1, 0, "User Page");
                popupMenu.getMenu().add(0, 2, 1, "Notifications");
                if (isAdmin) {
                    popupMenu.getMenu().add(0, 3, 2, "Admin View");
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == 1) {
                            Intent intent = new Intent(MainActivity.this, UserActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        } else if (item.getItemId() == 2) {
                            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        } else if (item.getItemId() == 3) {
                            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("username", extras.getString("username"));
                            intent.putExtra("isAdmin", isAdmin);
                            startActivity(intent);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void fetchBinderData(String username) {
        String binderUrl = BINDER_URL + "/" + username + "/binder";

        JsonObjectRequest binderReq = new JsonObjectRequest(
                Request.Method.GET, binderUrl, null,
                response -> {
                    try {
                        JSONArray binder = response.getJSONArray("binder");
                        List<JSONObject> cards = new ArrayList<>();

                        for (int i = 0; i < binder.length(); i++) {
                            JSONObject entry = binder.getJSONObject(i);
                            JSONObject card = entry.getJSONObject("card");
                            cards.add(card);
                        }
                        fetchAllCardPrices(cards);

                    } catch (JSONException e) {
                        Log.e("Binder parse error", e.getMessage());
                    }
                },
                error -> Log.e("Binder fetch error", error.toString())
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(binderReq);
    }

    private void fetchAllCardPrices(List<JSONObject> cards) {
        totalPortfolioValue = 0;
        Map<String, List<CandleEntry>> portfolioData = new LinkedHashMap<>();

        if (cards.isEmpty()) {
            portfolioValueText.setText(String.format("$%.2f", totalPortfolioValue));
            renderPortfolio(portfolioData);
            return;
        }

        fetchNextCard(cards, 0, portfolioData);
    }

    private void fetchNextCard(List<JSONObject> cards, int index, Map<String, List<CandleEntry>> portfolioData) {
        if (index >= cards.size()) {
            runOnUiThread(() -> {
                renderPortfolio(portfolioData);
                portfolioValueText.setText(String.format("$%.2f", totalPortfolioValue));
            });
            return;
        }
        JSONObject card = cards.get(index);
        String cardId;
        String cardName;

        try {
            cardId = String.valueOf(card.getLong("id"));
            cardName = card.optString("cardName", "Unknown Card");
        } catch (JSONException e) {
            Log.e("fetchNextCard", "Carddata is malformed: " + card);
            fetchNextCard(cards, index + 1, portfolioData);
            return;
        }

        String url = "http://coms-3090-022.class.las.iastate.edu:8080/api/prices/card/" + cardId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    //Get total prices
                    if (response.length() > 0) {
                        try {
                            JSONObject price = response.getJSONObject(response.length() - 1);
                            totalPortfolioValue += price.getDouble("price");
                        } catch (JSONException e) {
                            Log.e("totalPortfolioValue", e.getMessage());
                        }
                    } else {
                        Log.d("totalPortfolioValue", "No price log for " + cardName);
                    }

                    List<CandleEntry> entries = buildCandles(response);
                    portfolioData.put(cardName, entries);
                    fetchNextCard(cards, index + 1, portfolioData);
                },
                error -> {
                    Log.d("fetchNextCard", "Failed to fetch for card: " + cardName);
                    fetchNextCard(cards, index + 1, portfolioData);
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private List<CandleEntry> buildCandles(JSONArray priceResponse) {
        Map<String, List<Double>> byDay = new LinkedHashMap<>();
        try {
            for (int i = 0; i < priceResponse.length(); i++) {
                JSONObject rec = priceResponse.getJSONObject(i);

                double price = rec.optDouble("price", -1);
                String recordedAt = rec.optString("recordedAt", "");

                if (price < 0 || recordedAt.length() < 10) {
                    Log.w("PARSE", "Skipping bad record: " + rec);
                    continue;
                }

                String day = recordedAt.substring(0, 10);
                byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(price);
            }

        } catch (JSONException e) {
            Log.e("buildCandles", "JSON data malformed, exception:", e);
        }

        List<CandleEntry> entries = new ArrayList<>();
        int x = 0;

        for (Map.Entry<String, List<Double>> entry : byDay.entrySet()) {
            List<Double> prices = entry.getValue();

            if (prices.isEmpty()) continue;

            float open  = prices.get(0).floatValue();
            float close = prices.get(prices.size() - 1).floatValue();
            float high  = Collections.max(prices).floatValue();
            float low   = Collections.min(prices).floatValue();

            entries.add(new CandleEntry(x++, high, low, open, close));
        }

        return entries;
    }

    private void renderPortfolio(Map<String, List<CandleEntry>> portfolioData) {
        List<CandleEntry> combined = new ArrayList<>();
        int x = 0;

        for (List<CandleEntry> entries : portfolioData.values()) {
            if (entries.isEmpty()) continue;

            for (CandleEntry e : entries) {
                combined.add(new CandleEntry(
                        x++,
                        e.getHigh(),
                        e.getLow(),
                        e.getOpen(),
                        e.getClose()
                ));
            }
        }
        renderCandleChart(mainChart, combined);
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
}