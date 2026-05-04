package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;
import org.json.JSONObject;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class LiveChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText messageInput;
    private Button btnSend;
    private Button btnBack;

    private StompClient stompClient;

    private String username;
    private int id;
    private boolean isAdmin;
    private long roomId;

    private static final String BASE_URL    = "http://coms-3090-022.class.las.iastate.edu:8080/";
    private static final String WS_BASE_URL = "ws://coms-3090-022.class.las.iastate.edu:8080/ws/chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livechat);

        chatContainer  = findViewById(R.id.chat_container);
        chatScrollView = findViewById(R.id.chat_scroll_view);
        messageInput   = findViewById(R.id.chat_message_input);
        btnSend        = findViewById(R.id.chat_send_btn);
        btnBack        = findViewById(R.id.chat_back_btn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            id       = extras.getInt("id", -1);
            isAdmin  = extras.getBoolean("isAdmin", false);
            roomId   = extras.getLong("roomId", -1);
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LiveChatActivity.this, CardBinderActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("username", username);
            intent.putExtra("isAdmin", isAdmin);
            startActivity(intent);
        });

        btnSend.setOnClickListener(v -> sendMessage());

        stompInit();
        fetchHistory();
    }

    @SuppressLint("CheckResult")
    private void stompInit() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_BASE_URL);

        stompClient.lifecycle().subscribe(event -> {
            switch (event.getType()) {
                case OPENED: Log.d("STOMP_CHAT", "Opened"); break;
                case ERROR:  Log.e("STOMP_CHAT", "Error", event.getException()); break;
                case CLOSED: Log.d("STOMP_CHAT", "Closed"); break;
            }
        });

        stompClient.topic("/topic/chat/" + roomId).subscribe(message -> {
            String raw = message.getPayload();

            String[] parsed = parseRawMessage(raw);
            String sender = parsed[0];
            String content = parsed[1];

            runOnUiThread(() -> appendMessage(sender, content));
        });

        stompClient.connect();
    }

    private String[] parseRawMessage(String raw) {
        int splitIndex = raw.indexOf(":");

        if (splitIndex == -1) {
            return new String[]{"Unknown", raw};
        }

        String sender = raw.substring(0, splitIndex).trim();
        String content = raw.substring(splitIndex + 1).trim();

        if (sender.isEmpty()) sender = "Unknown";

        return new String[]{sender, content};
    }

    private void fetchHistory() {
        String url = BASE_URL + "chat/rooms/" + roomId + "/history";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        chatContainer.removeAllViews();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject msg = response.getJSONObject(i);
                            String sender = msg.optString("username", "").trim();
                            String content = msg.optString("content", "");

                            if (sender.isEmpty()) {
                                sender = "Unknown";
                            }

                            appendMessage(sender, content);
                        }
                        scrollToBottom();
                    } catch (JSONException e) {
                        Log.e("CHAT", "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("CHAT", "History fetch error: " + error.toString());
                    Toast.makeText(this, "Failed to load chat history", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) return;

        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("content",  content);

            stompClient.send("/app/chat/" + roomId, payload.toString()).subscribe(
                    () -> runOnUiThread(() -> messageInput.setText("")),
                    error -> Log.e("STOMP_CHAT", "Send error: " + error.getMessage())
            );
        } catch (JSONException e) {
            Log.e("CHAT", "Payload error: " + e.getMessage());
        }
    }

    private void appendMessage(String sender, String content) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.chat_msg_content, chatContainer, false);

        TextView tvContent = card.findViewById(R.id.msg_content);
        tvContent.setText(sender + ": " + content);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = 16;

        boolean isMine = sender.equalsIgnoreCase(username);

        if (isMine) {
            params.gravity = Gravity.END;
            ((androidx.cardview.widget.CardView) card)
                    .setCardBackgroundColor(android.graphics.Color.parseColor("#1B4D3E"));
        } else {
            params.gravity = Gravity.START;
        }

        card.setLayoutParams(params);
        chatContainer.addView(card);
        scrollToBottom();
    }
    private void scrollToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) stompClient.disconnect();
    }
}