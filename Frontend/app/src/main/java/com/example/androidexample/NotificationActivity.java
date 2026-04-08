package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
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

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotificationActivity extends AppCompatActivity {

    private LinearLayout notificationListContainer;
    private TextView unreadCountText;
    private Button btnBack;

    private WebSocket webSocket;
    private OkHttpClient client;

    private String username;
    private int id;
    private boolean isAdmin;

    private static final String BASE_URL    = "http://coms-3090-022.class.las.iastate.edu:8080/";
    private static final String WS_BASE_URL = "ws://coms-3090-022.class.las.iastate.edu:8080/notifications/";

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
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("username", username);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        });

        WebSocketInit();
        fetchNotifications();
        fetchUnreadCount();
    }

    private void WebSocketInit() {
        client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(WS_BASE_URL + username)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d("WS_NOTIF", "Connected: " + username);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d("WS_NOTIF", "Received: " + text);
                runOnUiThread(() -> {
                    percolateCards(text, null, null, null, false);
                    fetchUnreadCount(); // refresh
                });
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                ws.close(1000, null);
                Log.d("WS_NOTIF", "Closing: " + reason);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e("WS_NOTIF", "Error: " + t.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(NotificationActivity.this, "Connection failed", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void sendMarkAsRead(Long notificationId) {
        if (webSocket != null) {
            webSocket.send("read:" + notificationId);
        }
    }

    private void fetchNotifications() {
        String url = BASE_URL + "notifications/user/" + id;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        notificationListContainer.removeAllViews();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject n = response.getJSONObject(i);

                            Long   notifId    = n.getLong("id");
                            String type       = n.optString("notificationType", "");
                            String title      = n.optString("notificationTitle", "");
                            String message    = n.optString("notificationMessage", "");
                            String timestamp  = n.optString("notificationCreatedAt", "");
                            boolean isRead    = n.optBoolean("read", false);

                            percolateCards(
                                    "[" + type + "] " + title + ": " + message,
                                    notifId,
                                    title,
                                    timestamp,
                                    isRead
                            );
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

    private void fetchUnreadCount() {
        String url = BASE_URL + "notifications/unread/" + id;

        com.android.volley.toolbox.StringRequest req = new com.android.volley.toolbox.StringRequest(
                Request.Method.GET, url,
                response -> unreadCountText.setText("Unread: " + response.trim()),
                error  -> Log.e("NOTIF", "Unread count error: " + error.toString())
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void percolateCards(String rawMessage, Long notifId, String title, String timestamp, boolean isRead) {
        if(title == null){
            return;
        }

        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_notification, notificationListContainer, false);

        TextView tvType      = card.findViewById(R.id.notif_type);
        TextView tvTitle     = card.findViewById(R.id.notif_title);
        TextView tvMessage   = card.findViewById(R.id.notif_message);
        TextView tvTimestamp = card.findViewById(R.id.notif_timestamp);
        Button   btnRead     = card.findViewById(R.id.notif_mark_read_btn);

        if (title != null) {
            tvTitle.setText(title);
            tvMessage.setText(rawMessage);
            tvTimestamp.setText(timestamp != null ? timestamp : "");
            tvType.setVisibility(View.GONE);
        }

        if (isRead) {
            card.setAlpha(0.5f);
            btnRead.setVisibility(View.GONE);
        } else {
            btnRead.setVisibility(View.VISIBLE);
            btnRead.setOnClickListener(v -> {
                if (notifId != null) {
                    sendMarkAsRead(notifId);
                }
                card.setAlpha(0.5f);
                btnRead.setVisibility(View.GONE);
                fetchUnreadCount();
            });
        }


        if (title == null) {
            notificationListContainer.addView(card, 0);
        } else {
            notificationListContainer.addView(card);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}