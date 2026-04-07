package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BiggestMoversActivity extends AppCompatActivity {
    private LinearLayout cardContainer;
    private int id;
    private boolean isAdmin;
    private String username;
    private String cardId;
    private Button listPokemon;
    private Button listMTG;
    private Button listYugioh;
    private Button homeButton;
    private static final String BASE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/prices/biggest-movers/type/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biggest_movers);

        cardContainer = findViewById(R.id.biggestmovers_cardContainer);
        homeButton = findViewById(R.id.biggestmovers_home_btn);
        listPokemon = findViewById(R.id.biggestmovers_pokemon_btn);
        listMTG = findViewById(R.id.biggestmovers_MTG_btn);
        listYugioh = findViewById(R.id.biggestmovers_YuGiOh_btn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getInt("id", -1);
            isAdmin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username");
        }

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(BiggestMoversActivity.this, MainActivity.class);
            startActivity(intent);
        });
        listPokemon.setOnClickListener(v -> {
            getBiggestMovers("POKEMON");
        });
        listMTG.setOnClickListener(v -> {
            getBiggestMovers("MTG");
        });
        listYugioh.setOnClickListener(v -> {
            getBiggestMovers("Yu-Gi-Oh");
        });
    }

    void getBiggestMovers(String game){
        String url = BASE_URL + game + "/2days";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            handlePercolate(response);
                        } catch (JSONException e) {
                            Log.e("BiggestMovers", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("BiggestMovers", error.toString());
                    }
                }
        ) {
            @Override public Map<String, String> getHeaders() throws AuthFailureError { return new HashMap<>(); }
        };
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    void handlePercolate(JSONArray response)throws JSONException {
        cardContainer.removeAllViews();

        for (int i = 0; i < response.length(); i++){
            JSONObject card = response.getJSONObject(i);

            String cardId = String.valueOf(card.getInt("cardId"));
            String changePercent = String.valueOf(card.getDouble("changePercent"));

            View inflated = LayoutInflater.from(this).inflate(R.layout.activity_biggest_movers, null, false);

            LinearLayout row = inflated.findViewById(R.id.biggestmovers_row_template);
            ((ViewGroup) row.getParent()).removeView(row);

            ((TextView) row.findViewById(R.id.biggestmovers_cardName_txt)).setText("Loading...");
            ((TextView) row.findViewById(R.id.biggestmovers_cardChange_txt)).setText(changePercent + "%");
            row.setVisibility(View.VISIBLE);

            row.setOnClickListener(v -> {
                Intent intent = new Intent(BiggestMoversActivity.this, CardSearchActivity.class);
                intent.putExtra("bundleCardID", Integer.parseInt(cardId));
                startActivity(intent);
            });

            cardContainer.addView(row);
            getCardName(cardId, row);
        }
    }
    void getCardName(String cardID, View view){

    }

}
