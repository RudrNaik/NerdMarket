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

import java.nio.charset.StandardCharsets;
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

        //tracks if the user is an admin.


        //Login listener
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //grab username and pass
                String usernameOrEmail = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fill in all fields", Toast.LENGTH_LONG).show();
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("usernameOrEmail", usernameOrEmail);
                    jsonObject.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                        Request.Method.POST,
                        LOGIN_URL,
                        jsonObject,
                        response -> {
                            try {
                                int ID = response.getInt("id");
                                String USERNAME = response.getString("username");
                                boolean IS_ADMIN = response.getBoolean("admin");
                                Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("id", ID);
                                intent.putExtra("username", USERNAME);
                                intent.putExtra("isAdmin", IS_ADMIN);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            String message = "Login Failed.";
                            if (error.getMessage() != null) {
                                message = error.getMessage();
                            }
                            Log.e("Volley Error", message);
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            volleyError = new VolleyError(
                                    new String(volleyError.networkResponse.data, StandardCharsets.UTF_8)
                            );
                        }
                        return volleyError;
                    }
                };

                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjReq);
            }

        });

        //Listener for signup
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