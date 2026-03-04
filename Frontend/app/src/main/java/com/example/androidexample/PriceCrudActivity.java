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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
public class PriceCrudActivity extends AppCompatActivity {
    private static final String PRICES_URL = "http://coms-3090-022.class.las.iastate.edu:8080/api/prices/";
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
        deleteButton = findViewById(R.id.pricecrud_delete_btn);
        homeButton = findViewById(R.id.pricecrud_home_btn);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    currentID = Integer.parseInt(searchEditText.getText().toString());
                } catch (NumberFormatException e){
                    currentID = -1;
                }
            }
        });
    }

    void FindPriceByID(){
        if (currentID == -1){
            return;
        }

        String url = PRICES_URL + "cards/" + currentID + "/latest";
    }
}
