package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class UserActivity extends AppCompatActivity {
    private Button signupBackButton;
    private Button deleteAccountButton;
    private Button toNotificationsButton;
    private Button toAdminButton;
    private Button loginBackButton;
    private ImageButton hamburgerDropdownButton;
    private ImageButton cardBinderButton;
    private ImageButton cardDetailsButton;
    private ImageButton toChatsButton;
    private ImageView toHomeButton;
    private TextView usernameText;
    private int id;
    private String username;
    private boolean isAdmin;
    private static final String DELETE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        signupBackButton = findViewById(R.id.userPage_back_to_signup_btn);
        deleteAccountButton = findViewById(R.id.userPage_delete_account_btn);
        toNotificationsButton = findViewById(R.id.userPage_to_notifs_btn);
        toAdminButton = findViewById(R.id.userPage_to_admin_btn);
        loginBackButton = findViewById(R.id.userPage_back_to_login_btn);
        hamburgerDropdownButton = findViewById(R.id.userPage_dropdown_btn);
        cardBinderButton = findViewById(R.id.userPage_toPortfolio_image);
        cardDetailsButton = findViewById(R.id.userPage_toSearch_image);
        toHomeButton = findViewById(R.id.userPage_home_image);
        usernameText = findViewById(R.id.userPage_username_txt);
        toChatsButton = findViewById(R.id.userPage_tochats_btn);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            usernameText.setText("User Page");
        } else {
            id = extras.getInt("id", -1);
            isAdmin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username", "Please log out and back in");
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
                Intent intent = new Intent(UserActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        loginBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountConfirm();
            }
        });

        toNotificationsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this, NotificationActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        cardBinderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this, CardBinderActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        cardDetailsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this, CardSearchActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        toHomeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        toChatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, LiveChatMenuActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        toAdminButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this, AdminActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        hamburgerDropdownButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PopupMenu popupMenu = new PopupMenu(UserActivity.this, v);
                popupMenu.getMenu().add(0, 1, 0, "User Page");
                popupMenu.getMenu().add(0, 2, 1, "Notifications");
                if (isAdmin) {
                    popupMenu.getMenu().add(0, 3, 2, "Admin View");
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == 1) {
                            //Go to user page, but we're already there
                        } else if (item.getItemId() == 2) {
                            Intent intent = new Intent(UserActivity.this, NotificationActivity.class);
                            intent.putExtra("id", id);
                            intent.putExtra("isAdmin", isAdmin);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        } else if (item.getItemId() == 3) {
                            Intent intent = new Intent(UserActivity.this, AdminActivity.class);
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
