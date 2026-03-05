package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
public class PriceCrudActivity extends AppCompatActivity {
    private static final String PRICES_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/prices";
    private EditText searchEditText;
    private EditText updateEditText;
    private EditText createEditText;
    private TextView priceText;
    private Button searchButton;
    private Button createButton;
    private Button updateButton;
    private Button deleteButton;
    private Button homeButton;
    int currentID = -1;
    int priceIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricecrud);

        searchEditText = findViewById(R.id.pricecrud_search_txt);
        updateEditText = findViewById(R.id.pricecrud_update_txt);
        createEditText = findViewById(R.id.pricecrud_create_txt);
        priceText = findViewById(R.id.pricecrud_currentprice_txt);
        searchButton = findViewById(R.id.pricecrud_search_btn);
        createButton = findViewById(R.id.pricecrud_create_btn);
        updateButton = findViewById(R.id.pricecrud_update_btn);
        deleteButton = findViewById(R.id.pricecrud_deleteALL_btn);
        homeButton = findViewById(R.id.pricecrud_home_btn);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    currentID = Integer.parseInt(searchEditText.getText().toString());
                } catch (NumberFormatException e){
                    currentID = -1;
                }
                FindPriceByID();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    currentID = Integer.parseInt(searchEditText.getText().toString());
                } catch (NumberFormatException e){
                    currentID = -1;
                }
                ManualUpdatePrice();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    currentID = Integer.parseInt(searchEditText.getText().toString());
                } catch (NumberFormatException e){
                    currentID = -1;
                }
                CreateRecord();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    currentID = Integer.parseInt(searchEditText.getText().toString());
                } catch (NumberFormatException e){
                    currentID = -1;
                }
                DeleteAllRecords();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PriceCrudActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    void FindPriceByID(){
        if (currentID == -1){
            return;
        }

        String url = PRICES_URL + "/card/" + currentID + "/latest";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            priceIndex = response.getInt("id");
                            String price = response.getString("price");
                            priceText.setText("Current Price: $" + price);
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                    }
                }
        );
        VolleySingleton.getInstance(getApplicationContext())
                .addToRequestQueue(jsonObjReq);
    }

    void ManualUpdatePrice(){
        if (currentID == -1){
            return;
        }

        String url = PRICES_URL + priceIndex;

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("price", Double.parseDouble(updateEditText.getText().toString()));
            jsonObject.put("cardID", currentID);
        } catch (JSONException e) {
                e.printStackTrace();
                return;
        } catch (NumberFormatException e){
            currentID = -1;
            return;
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String price = response.getString("price");
                            priceText.setText("Updated Price: $" + price);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                    }
                }
        );
        VolleySingleton.getInstance(getApplicationContext())
                .addToRequestQueue(jsonObjReq);

    }

    void CreateRecord(){
        String url = PRICES_URL;
        if (currentID == -1){
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("price", Double.parseDouble(createEditText.getText().toString()));
            jsonObject.put("cardId", currentID);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (NumberFormatException e){
            currentID = -1;
            return;
        }

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        priceText.setText("Created Entry: $" + Double.parseDouble(createEditText.getText().toString()));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                return jsonObject.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    void DeleteAllRecords(){
        if (currentID == -1){
            return;
        }
        String url = PRICES_URL + "/card/" + currentID;

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        priceText.setText("Deleted all ID no." + currentID + " entries.");
                        currentID = -1;
                        priceIndex = -1;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                    }
                });
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

}
