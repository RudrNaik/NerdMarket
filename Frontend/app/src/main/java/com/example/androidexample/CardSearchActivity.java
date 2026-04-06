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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CardSearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button btnSearch;

    private CardView cardView;
    private ImageView cardImage;
    private ImageView cameraSearch;
    private String cardUrl;
    private TextView cardName, cardType, cardSet, cardRarity, cardPrice;

    private EditText cardNameEdit, cardTypeEdit, cardSetEdit, cardRarityEdit, cardPriceEdit;
    private Button cardEditBtn, cardSaveBtn;
    private LinearLayout cardListContainer;

    private JSONArray cards;
    private  JSONObject card;
    private String currentCardId;

    private Button returnToMain;
    private int id;
    private int bundleCardID;
    private String bundleCardIDString;
    private String username;
    private boolean isAdmin;

    private static final String BASE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/cards";

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

        returnToMain = findViewById(R.id.cardlookup_to_main_button);

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
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> handleSearch());

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

            Button btnEdit = clonedCard.findViewById(R.id.card_edit_btn);

            //blocks the button from ever being visible if not an admin
            if(!isAdmin){
                btnEdit.setVisibility(View.GONE);
            }

            Button btnSave = clonedCard.findViewById(R.id.card_save_btn);

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

    // GET /api/cards/search/{name}
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

    //GET /api/cards/{id}
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

    // PUT /api/cards/{id}
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