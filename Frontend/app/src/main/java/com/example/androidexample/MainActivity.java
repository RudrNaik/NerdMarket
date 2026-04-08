package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/*

1. To run this project, open the directory "Android Example", otherwise it may not recognize the file structure properly

2. Ensure you are using a compatible version of gradle, to do so you need to check 2 files.

    AndroidExample/Gradle Scripts/build.gradle
    Here, you will have this block of code. Ensure it is set to a compatible version,
    in this case 8.12.2 should be sufficient:
        plugins {
            id 'com.android.application' version '8.12.2' apply false
        }

    Gradle Scripts/gradle-wrapper.properties

3. This file is what actually determines the Gradle version used, 8.13 should be sufficient.
    "distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip" ---Edit the version if needed

4. You might be instructed by the plugin manager to upgrade plugins, accept it and you may execute the default selected options.

5. Press "Sync project with gradle files" located at the top right of Android Studio,
   once this is complete you will be able to run the app

   This version is compatible with both JDK 17 and 21. The Java version you want to use can be
   altered in Android Studio->Settings->Build, Execution, Deployment->Build Tools->Gradle

 */


public class MainActivity extends AppCompatActivity {

    private TextView messageText;   // define message textview variable
    private TextView usernameText;  // define username textview variable
    private Button loginBackButton;
    private Button cardDetailsButton;
    private Button priceCRUDButton;
    private Button signupBackButton;
    private Button deleteAccountButton;
    private Button toAdminButton;
    private Button cardBinderButton;
    private Button toNotificationsButton;
    private int id;

    private String username;
    private boolean isAdmin;
    private static final String DELETE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/users/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);             // link to Main activity XML

        /* initialize UI elements */
        messageText = findViewById(R.id.main_msg_txt);      // link to message textview in the Main activity XML
        usernameText = findViewById(R.id.main_username_txt);// link to username textview in the Main activity XML
        signupBackButton = findViewById(R.id.back_to_signup_btn);
        cardDetailsButton = findViewById(R.id.to_carddetails_btn);
        toAdminButton = findViewById(R.id.to_admin_btn);
        loginBackButton = findViewById(R.id.back_to_login_btn);
        signupBackButton.setVisibility(View.INVISIBLE);
        loginBackButton.setVisibility(View.INVISIBLE);
        deleteAccountButton = findViewById(R.id.delete_account_btn);
        priceCRUDButton = findViewById(R.id.to_pricecrud_btn);
        cardBinderButton = findViewById(R.id.to_cardbinder_btn);
        toNotificationsButton = findViewById(R.id.to_notifs_btn);


        /* extract data passed into this activity from another activity */
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            messageText.setText("Home Page");
        } else {
            id = extras.getInt("id", -1);
            isAdmin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username", "Please log out and back in");
            messageText.setText("Welcome " + extras.getString("username"));
            usernameText.setText(extras.getString("username")); // this will come from LoginActivity
            loginBackButton.setVisibility(View.VISIBLE);            // set new login button visible
            signupBackButton.setVisibility(View.VISIBLE);           // set new signup button visible

            //check if the user is an admin and make the admin tab visible.
            if (isAdmin) {
                toAdminButton.setVisibility(View.VISIBLE);
            } else {
                toAdminButton.setVisibility(View.GONE);
            }

        }

        signupBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        priceCRUDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(MainActivity.this, PriceCrudActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        loginBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountConfirm();
            }
        });

        cardDetailsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, CardSearchActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        toNotificationsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        cardBinderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, CardBinderActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        toAdminButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            Intent intent = new Intent(MainActivity.this, AdminActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("username", extras.getString("username"));
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
            }
        });
    }

    void deleteAccountRequest(String password){
        String url = DELETE_URL + id + "/delete-account";

        JSONObject jsonObject = new JSONObject();

        if (id == -1) {
            Toast.makeText(this, "Bad user session", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "Account delete Failed.";
//                        if (error.networkResponse != null && error.networkResponse.data != null) {
//                            message = new String(error.networkResponse.data); //FOR GETTING SPECIFIC ERROR INFO
//                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Password", password);
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext())
                .addToRequestQueue(stringRequest);
    }
    void deleteAccountConfirm(){
        EditText passwordConfirm = new EditText(this);

        new AlertDialog.Builder(this).setTitle("Are you sure")
                .setMessage("Enter password to confirm").setView(passwordConfirm)
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String password = passwordConfirm.getText().toString();
                                deleteAccountRequest(password);
                            }
                        }).setNegativeButton("Cancel", null).show();
    }
}