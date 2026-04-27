package com.example.androidexample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private static final String API_URL = "http://coms-3090-022.class.las.iastate.edu:8080/admin";

    // Activate/Deactivate
    private Button activateDeacButton;
    private LinearLayout activateDeacContainer;
    private EditText cardSearchField;
    private Button activateBtn;
    private Button deactivateBtn;

    // Unlock Account
    private Button unlockAccountButton;
    private LinearLayout unlockAccountContainer;
    private EditText unlockSearchField;
    private Button unlockBtn;

    // Promote/Demote
    private Button promoteDemoteButton;
    private LinearLayout promoteDemoteContainer;
    private EditText promoteDemoteField;
    private Button promoteBtn;
    private Button demoteBtn;

    // Delete
    private Button deleteAccountButton;
    private LinearLayout deleteContainer;
    private EditText deleteField;
    private Button deleteBtn;

    // Show All Accounts
    private Button cardDetailsButton;
    private CardView accountCard;
    private LinearLayout accountCardDetail;
    private TextView accountCardDetailName;

    // Back to Main
    private Button toMainButton;

    // To Price Crud
    private Button toPriceCrud;

    // Notifications
    private Button createNotificationButton;
    private LinearLayout createNotificationContainer;
    private EditText notifTitleField, notifMessageField, notifTypeField, notifScheduledAtField;
    private Button sendNotifBtn;

    private int id;

    private boolean admin;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Get all views

        // Activate buttons
        activateDeacButton   = findViewById(R.id.admin_activate_deactivate_btn);
        activateDeacContainer= findViewById(R.id.admin_activate_deactivate_container);
        cardSearchField      = findViewById(R.id.card_search_field);
        activateBtn          = findViewById(R.id.admin_activate_btn);
        deactivateBtn        = findViewById(R.id.admin_deactivate_btn);

        //Unlock buttons
        unlockAccountButton = findViewById(R.id.admin_unlock_btn);
        unlockAccountContainer = findViewById(R.id.admin_unlock_container);
        unlockSearchField = findViewById(R.id.admin_unlock_ID_searchField);
        unlockBtn = findViewById(R.id.admin_unlockAccount_btn);

        //Promotion buttons
        promoteDemoteButton  = findViewById(R.id.admin_promote_demote_btn);
        promoteDemoteContainer = findViewById(R.id.admin_promote_demote_container);
        promoteDemoteField   = findViewById(R.id.admin_promote_demote_field);
        promoteBtn           = findViewById(R.id.admin_promote_btn);
        demoteBtn            = findViewById(R.id.admin_demote_btn);

        // Delete buttons
        deleteAccountButton  = findViewById(R.id.admin_delete_other_account_btn);
        deleteContainer      = findViewById(R.id.admin_delete_container);
        deleteField          = findViewById(R.id.admin_delete_field);
        deleteBtn            = findViewById(R.id.admin_delete_btn);

        // ShowAllAccounts button.
        cardDetailsButton    = findViewById(R.id.admin_showAllAccounts_btn);
        accountCard          = findViewById(R.id.account_card);
        accountCardDetail    = findViewById(R.id.admin_account_cardDetail);
        accountCardDetailName= findViewById(R.id.admin_account_cardDetail_name);

        // Back to main
        toMainButton         = findViewById(R.id.admin_to_main_btn);

        //To Price Crud
        toPriceCrud          = findViewById(R.id.admin_to_priceCrud_btn);

        // notifications
        createNotificationButton   = findViewById(R.id.admin_create_notif_btn);
        createNotificationContainer= findViewById(R.id.admin_create_notif_container);
        notifTitleField            = findViewById(R.id.admin_notif_title_field);
        notifMessageField          = findViewById(R.id.admin_notif_message_field);
        notifTypeField             = findViewById(R.id.admin_notif_type_field);
        notifScheduledAtField      = findViewById(R.id.admin_notif_scheduledat_field);
        sendNotifBtn               = findViewById(R.id.admin_send_notif_btn);

        createNotificationContainer.setVisibility(View.GONE);

        activateDeacContainer.setVisibility(View.GONE);
        promoteDemoteContainer.setVisibility(View.GONE);
        deleteContainer.setVisibility(View.GONE);
        unlockAccountContainer.setVisibility(View.GONE);
        accountCard.setVisibility(View.GONE);

        // Read bundle given from main (which was passed from login).
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            id = -1;
            admin = false;
        } else {
            id = extras.getInt("id", -1);
            admin = extras.getBoolean("isAdmin", false);
            username = extras.getString("username"); // used when moving back to the main view.
        }

        unlockAccountButton.setOnClickListener(v -> toggle(unlockAccountContainer));

        unlockBtn.setOnClickListener(v -> {
            String targetIdStr = unlockSearchField.getText().toString().trim();
            if (targetIdStr.isEmpty() || !targetIdStr.matches(".*\\d.*")) {
                Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            unlockRequest(Long.parseLong(targetIdStr));
        });

        activateDeacButton.setOnClickListener(v -> toggle(activateDeacContainer));

        activateBtn.setOnClickListener(v -> {
            String targetIdStr = cardSearchField.getText().toString().trim();
            if (targetIdStr.isEmpty() || !targetIdStr.matches(".*\\d.*")) {
                Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            activateDeactivateRequest(Long.parseLong(targetIdStr), true);
        });

        deactivateBtn.setOnClickListener(v -> {
            String targetIdStr = cardSearchField.getText().toString().trim();
            if (targetIdStr.isEmpty() || !targetIdStr.matches(".*\\d.*")) { //uses regex to check if there is a number for now because time crunch. Adding this to the backlog to eventually fix to not accept strings with a number in the input as we need ID's.
                Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            activateDeactivateRequest(Long.parseLong(targetIdStr), false);
        });

        promoteDemoteButton.setOnClickListener(v -> toggle(promoteDemoteContainer));

        promoteBtn.setOnClickListener(v -> {
            String targetIdStr = promoteDemoteField.getText().toString().trim();
            if (targetIdStr.isEmpty()) {
                Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            promoteDemoteRequest(Long.parseLong(targetIdStr), true);
        });

        demoteBtn.setOnClickListener(v -> {
            String targetIdStr = promoteDemoteField.getText().toString().trim();
            if (targetIdStr.isEmpty()) {
                Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            promoteDemoteRequest(Long.parseLong(targetIdStr), false);
        });


        deleteAccountButton.setOnClickListener(v -> toggle(deleteContainer));

        deleteBtn.setOnClickListener(v -> {
            String targetIdStr = deleteField.getText().toString().trim();
            if (targetIdStr.isEmpty()) {
                Toast.makeText(this, "Enter a target user ID", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteOtherAccountRequest(Long.parseLong(targetIdStr));
        });


        cardDetailsButton.setOnClickListener(v -> getAllUsersRequest());

        createNotificationButton.setOnClickListener(v -> toggle(createNotificationContainer));

        sendNotifBtn.setOnClickListener(v -> {
            String title     = notifTitleField.getText().toString().trim();
            String message   = notifMessageField.getText().toString().trim();
            String type      = notifTypeField.getText().toString().trim();
            String scheduled = notifScheduledAtField.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Title and message are required", Toast.LENGTH_SHORT).show();
                return;
            }
            createNotificationRequest(title, message, type.isEmpty() ? "GENERAL" : type, scheduled.isEmpty() ? null : scheduled);
        });


        toMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", admin);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        toPriceCrud.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, PriceCrudActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", admin);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    //Toggle helper to switch between views toggled on or off.
    private void toggle(View view) {
        if(view.getVisibility() == View.VISIBLE){
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }

    }


    // GET /admin/users?userId={id}
    private void getAllUsersRequest() {
        if (id == -1) { Toast.makeText(this, "Bad user session", Toast.LENGTH_SHORT).show(); return; }

        String url = API_URL + "/users?userId=" + id;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    accountCard.setVisibility(View.VISIBLE);
                    String retString = "";
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject user = response.getJSONObject(i);
                            retString +=    "ID: "+ user.getLong("id") +
                                            "  //  " + user.getString("username") +
                                            "  //  Active: " + user.getBoolean("active")+
                                            "  //  Admin: "+ user.getBoolean("admin")+
                                            "\n";
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    accountCardDetailName.setText(!retString.isEmpty() ? retString : "No users found.");
                },
                error -> {
                    String msg = "Failed to fetch users.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    // POST /admin/users{targetId}/unlock
    private void unlockRequest(long targetId) {
        if (id == -1) {
            Toast.makeText(getApplicationContext(), "Bad user session", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = API_URL + "/users/" + targetId + "/unlock?userId=" + id;

        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                response -> Toast.makeText(this,
                        "User " + targetId + " unlocked",
                        Toast.LENGTH_SHORT).show(),
                error -> {
                    String msg = "Unlock failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    // PUT /admin/users/{targetId}/activate  OR  /deactivate
    private void activateDeactivateRequest(long targetId, boolean activate) {
        if (id == -1) {
            Toast.makeText(getApplicationContext(), "Bad user session", Toast.LENGTH_SHORT).show();
            return;
        }

        String action;

        if(activate){
            action = "activate";
        } else {
            action ="deactivate";
        }

        String url = API_URL + "/users/" + targetId + "/" + action + "?userId=" + id;

        StringRequest request = new StringRequest(
                Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        "User " + targetId + " " + (action + "d"),
                        Toast.LENGTH_SHORT).show(),
                error -> {
                    String msg = action + " failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


    // PUT /admin/users/{targetId}/promote  or  /demote
    private void promoteDemoteRequest(long targetId, boolean promote) {
        if (id == -1) {
            Toast.makeText(this, "Bad user session", Toast.LENGTH_SHORT).show();
            return;
        }

        String action;
        if(promote){
            action = "promote";
        } else {
            action ="demote";
        }
        String url = API_URL + "/users/" + targetId + "/" + action + "?userId=" + id;

        StringRequest request = new StringRequest(
                Request.Method.PUT, url,
                response -> Toast.makeText(this,
                        "User " + targetId + (" " + action + "d"),
                        Toast.LENGTH_SHORT).show(),
                error -> {
                    String msg = action + " failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }


    // DELETE /admin/users/{targetId}?userId={id}
    private void deleteOtherAccountRequest(long targetId) {
        if (id == -1) {
            Toast.makeText(this, "Bad user session", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = API_URL + "/users/" + targetId + "?userId=" + id;

        StringRequest request = new StringRequest(
                Request.Method.DELETE, url,
                response -> Toast.makeText(this, "User " + targetId + " deleted.", Toast.LENGTH_SHORT).show(),
                error -> {
                    String msg = "Delete failed.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    // POST /notifications
    private void createNotificationRequest(String title, String message, String type, String scheduledAt) {
        if (id == -1) {
            Toast.makeText(this, "Bad user session", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-022.class.las.iastate.edu:8080/notifications";

        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                response -> {
                    Toast.makeText(this,
                            scheduledAt != null ? "Notification scheduled!" : "Notification sent!",
                            Toast.LENGTH_SHORT).show();
                    // Clear fields after send
                    notifTitleField.setText("");
                    notifMessageField.setText("");
                    notifTypeField.setText("");
                    notifScheduledAtField.setText("");
                },
                error -> {
                    String msg = "Failed to create notification.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        msg = new String(error.networkResponse.data);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    JSONObject body = new JSONObject();
                    body.put("senderId", id);
                    body.put("title", title);
                    body.put("message", message);
                    body.put("type", type);
                    if (scheduledAt != null) {
                        body.put("scheduledAt", scheduledAt);
                    }
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}