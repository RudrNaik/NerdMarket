package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String LOGIN_URL = "http://coms-3090-022.class.las.iastate.edu:8080/users/login";
    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button goButton;         // define login button variable
    private Button signupButton;        // define signup button variable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML
        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username);
        passwordEditText = findViewById(R.id.login_password);
        goButton = findViewById(R.id.login_go_btn);
        signupButton = findViewById(R.id.login_signup_btn);


        /* click listener on login button pressed */
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String usernameOrEmail = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fill in all fields", Toast.LENGTH_LONG).show();
                    return;
                }

                JSONObject jsonObject = new JSONObject();

                try{
                    jsonObject.put("usernameOrEmail", usernameOrEmail);
                    jsonObject.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace(); // Handle JSON parsing errors
                }


                JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                        Request.Method.POST,
                        LOGIN_URL,
                        jsonObject,

                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                try {
                                    int ID = jsonObject.getInt("id");
                                    String USERNAME = jsonObject.getString("username");
                                    Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("id", ID);  // key-value to pass to the MainActivity
                                    intent.putExtra("username", USERNAME);  // key-value to pass to the MainActivity
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace(); // Handle JSON parsing errors
                                }
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = "Login Failed.";
                                if (error.networkResponse != null){
                                    message = new String(error.networkResponse.data);
                                }
                                Log.e("Volley Error", error.toString()); // Log error details
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        }
                ){
                    // Override getHeaders() if authentication headers are needed
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
//                      headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // Example authentication header
                        headers.put("Content-Type", "application/json"); // Example content-type header
                        return headers;
                    }
                };
                VolleySingleton.getInstance(getApplicationContext())
                        .addToRequestQueue(jsonObjReq);
            }

        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });

    }
}