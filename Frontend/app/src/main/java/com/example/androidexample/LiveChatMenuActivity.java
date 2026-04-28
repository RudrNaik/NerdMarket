package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class LiveChatMenuActivity extends AppCompatActivity {

    /**
     * List container for the notifications
     */
    private LinearLayout notificationListContainer;
    /**
     * Text field for the amount of unread notifications.
     */
    private TextView unreadCountText;
    /**
     * back button
     */
    private Button  btnBack;

    /***
     * Stomp client for websocket.
     */
    private StompClient stompClient;
    /**
     * user's username
     */
    private String  username;
    /**
     * user ID
     */
    private int  id;
    /**
     * Boolean checking if user isadmin
     */
    private boolean isAdmin;
    /**
     * Base URL for the backend
     */
    private static final String BASE_URL    = "http://coms-3090-022.class.las.iastate.edu:8080/";
    /**
     * websocket url
     */
    private static final String WS_BASE_URL = "ws://coms-3090-022.class.las.iastate.edu:8080/ws/notifications";

    /**
     * Startup onCreate when the activity is started.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationListContainer = findViewById(R.id.notification_list_container);
        unreadCountText           = findViewById(R.id.notification_unread_count);
        btnBack                   = findViewById(R.id.notification_back_btn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            id       = extras.getInt("id", -1);
            isAdmin  = extras.getBoolean("isAdmin", false);
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LiveChatMenuActivity.this, MainActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("username", username);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        });

        stompInit();
        fetchNotifications();
        fetchUnreadCount();
    }

    /**
     * initializes Stomp websocket.
     */
    @SuppressLint("CheckResult")
    private void stompInit() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_BASE_URL);

        stompClient.lifecycle().subscribe(event -> {
            switch (event.getType()) {
                case OPENED:
                    Log.d("STOMP_NOTIF", "Opened");
                    break;
                case ERROR:
                    Log.e("STOMP_NOTIF", "Error", event.getException());
                    break;
                case CLOSED:
                    Log.d("STOMP_NOTIF", "Closed");
                    break;
            }
        });

        stompClient.topic("/topic/notifications/" + username).subscribe(message -> {
            Log.d("STOMP_NOTIF", "Live message: " + message.getPayload());
            try {
                JSONObject n = new JSONObject(message.getPayload());

                long    notifId   = n.optLong("id", -1);
                String  type      = n.optString("notificationType", "");
                String  title     = n.optString("notificationTitle", "");
                String  msg       = n.optString("notificationMessage", "");
                String  timestamp = n.optString("notificationCreatedAt", "");
                boolean isRead    = n.optBoolean("read", false);

                runOnUiThread(() -> {
                    percolateCard(
                            notifId == -1 ? null : notifId, type, title, msg, timestamp, isRead, 0
                    );
                    fetchUnreadCount();
                });

            } catch (JSONException e) {
                Log.e("STOMP_NOTIF", "Parse error: " + e.getMessage());
            }
        });

        stompClient.connect();
    }

    /**
     * Fetches the notifications for the user via ws.
     */
    private void fetchNotifications() {
        String url = BASE_URL + "notifications/user/" + id;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        notificationListContainer.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject n = response.getJSONObject(i);

                            long    notifId   = n.getLong("id");
                            String  type      = n.optString("notificationType", "");
                            String  title     = n.optString("notificationTitle", "");
                            String  message   = n.optString("notificationMessage", "");
                            String  timestamp = n.optString("notificationCreatedAt", "");
                            boolean isRead    = n.optBoolean("read", false);

                            percolateCard(notifId, type, title, message, timestamp, isRead, -1);
                        }
                    } catch (JSONException e) {
                        Log.e("NOTIF", "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("NOTIF", "Fetch error: " + error.toString());
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * fetches the unread notification count.
     */
    private void fetchUnreadCount() {
        String url = BASE_URL + "notifications/unread/" + id;

        StringRequest req = new StringRequest(
                Request.Method.GET, url,
                response -> runOnUiThread(() ->
                        unreadCountText.setText("Unread: " + response.trim())),
                error -> Log.e("NOTIF", "Unread count error: " + error.toString())
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Marks specific notification as read.
     * @param notifId notification's ID
     * @param card the card component
     * @param btnRead the button to mark it as read.
     */
    private void markAsRead(Long notifId, View card, Button btnRead) {
        String url = BASE_URL + "notifications/" + notifId + "/read";

        StringRequest req = new StringRequest(
                Request.Method.PUT, url,
                response -> runOnUiThread(() -> {
                    card.setAlpha(0.5f);
                    btnRead.setVisibility(View.GONE);
                    fetchUnreadCount();
                }),
                error -> {
                    Log.e("NOTIF", "Mark read failed: " + error.toString());
                    Toast.makeText(this, "Failed to mark as read", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Percolates the cards into the card container via inflation
     * @param notifId the notification ID
     * @param type notification type
     * @param title notification title
     * @param message notification's message
     * @param timestamp the timestamp for the notification
     * @param isRead boolean to check if it is read or not
     * @param insertIndex the index to insert it at.
     */
    private void percolateCard(Long notifId, String type, String title, String message, String timestamp, boolean isRead, int insertIndex) {

        if (title == null || title.isEmpty()) return;

        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_notification, notificationListContainer, false);

        TextView tvType      = card.findViewById(R.id.notif_type);
        TextView tvTitle     = card.findViewById(R.id.notif_title);
        TextView tvMessage   = card.findViewById(R.id.notif_message);
        TextView tvTimestamp = card.findViewById(R.id.notif_timestamp);
        Button   btnRead     = card.findViewById(R.id.notif_mark_read_btn);

        if (type != null && !type.isEmpty()) {
            tvType.setText(type);
            tvType.setVisibility(View.VISIBLE);
        } else {
            tvType.setVisibility(View.GONE);
        }

        tvTitle.setText(title);
        tvMessage.setText(message != null ? message : "");
        tvTimestamp.setText(timestamp != null ? timestamp : "");

        if (isRead) {
            card.setAlpha(0.5f);
            btnRead.setVisibility(View.GONE);
        } else {
            btnRead.setVisibility(View.VISIBLE);
            btnRead.setOnClickListener(v -> {
                if (notifId != null) {
                    markAsRead(notifId, card, btnRead);
                }
            });
        }

        if (insertIndex == -1) {
            notificationListContainer.addView(card);
        } else {
            notificationListContainer.addView(card, insertIndex);
        }
    }

    /**
     * Method to destroy the stomp client when the activity ends.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }

}