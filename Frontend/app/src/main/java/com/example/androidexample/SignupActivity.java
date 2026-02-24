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


public class SignupActivity extends AppCompatActivity {

    private static final String CREATE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/users/signup";
    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText firstnameEditText;   // define confirm edittext variable
    private EditText lastnameEditText;
    private EditText emailEditText;

    private Button createAccountButton;         // define login button variable
    private Button backToLoginButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        createAccountButton = findViewById(R.id.create_account_btn);    // link to login button in the Signup activity XML
        backToLoginButton = findViewById(R.id.back_to_login_btn);  // link to signup button in the Signup activity XML
        /* initialize UI elements */
        usernameEditText = findViewById(R.id.signup_username);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password);  // link to password edtext in the Signup activity XML
        firstnameEditText = findViewById(R.id.signup_firstname);    // link to confirm edtext in the Signup activity XML
        lastnameEditText = findViewById(R.id.signup_lastname);
        emailEditText = findViewById(R.id.signup_email);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                /* Checking if the username and password are valid */
                if (username.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Username is empty", Toast.LENGTH_LONG).show();
                    return;
                } else if (password.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (password.length() < 8){
                    Toast.makeText(getApplicationContext(), "Password must be 8+ characters", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (!password.matches(".*[^a-zA-Z0-9].*")){
                    Toast.makeText(getApplicationContext(), "Password must contain a symbol", Toast.LENGTH_LONG).show();
                    return;
                }
                makeSignupRequest();
            }
        });

        /* click listener on login button pressed */
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);  // go to LoginActivity
            }
        });
    }

void makeSignupRequest() {
    String firstName = firstnameEditText.getText().toString().trim();
    String lastName = lastnameEditText.getText().toString().trim();
    String email = emailEditText.getText().toString().trim();
    String username = usernameEditText.getText().toString().trim();
    String password = passwordEditText.getText().toString().trim();

    JSONObject jsonObject = new JSONObject();

    try{
        jsonObject.put("firstname", firstName);
        jsonObject.put("lastName", lastName);
        jsonObject.put("email", email);
        jsonObject.put("username", username);
        jsonObject.put("password", password);
    } catch (JSONException e) {
        e.printStackTrace(); // Handle JSON parsing errors
    }

    JsonObjectRequest jsonObjReq = new JsonObjectRequest(
            Request.Method.POST,
            CREATE_URL,
            jsonObject,

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Toast.makeText(getApplicationContext(), "Account Created!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Volley Error", error.toString()); // Log error details
                    Toast.makeText(getApplicationContext(), "Signup failed", Toast.LENGTH_LONG).show();
                }
            }
    ){
        // Override getHeaders() if authentication headers are needed
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // Example authentication header
            headers.put("Content-Type", "application/json"); // Example content-type header
            return headers;
        }

        // Override getParams() if you need to send request parameters (for POST/PUT)
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
//                params.put("param1", "value1"); // Example parameter
//                params.put("param2", "value2"); // Example parameter
            return params;
        }
    };
    VolleySingleton.getInstance(getApplicationContext())
            .addToRequestQueue(jsonObjReq);
    }
}