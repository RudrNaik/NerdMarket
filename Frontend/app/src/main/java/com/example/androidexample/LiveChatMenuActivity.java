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

public class LiveChatMenuActivity extends AppCompatActivity {

    private LinearLayout roomListContainer;
    private Button btnBack;

    private String username;
    private int id;
    private boolean isAdmin;

    private static final String BASE_URL = "http://coms-3090-022.class.las.iastate.edu:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livechatmenu);

        roomListContainer = findViewById(R.id.room_list_container);
        btnBack           = findViewById(R.id.chat_menu_back_btn);

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

        fetchRooms();
    }

    private void fetchRooms() {
        String url = BASE_URL + "chat/rooms/" + id;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        roomListContainer.removeAllViews();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject room = response.getJSONObject(i);
                            long   roomId   = room.getLong("id");
                            String roomName = room.optString("name", "Room " + roomId);
                            String roomType = room.optString("type", "");
                            inflateRoomCard(roomId, roomName, roomType);
                        }
                    } catch (JSONException e) {
                        Log.e("CHAT_MENU", "Parse error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("CHAT_MENU", "Fetch error: " + error.toString());
                    Toast.makeText(this, "Failed to load chat rooms", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void inflateRoomCard(long roomId, String roomName, String roomType) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_chat_room, roomListContainer, false);

        TextView tvName = card.findViewById(R.id.room_name);
        TextView tvType = card.findViewById(R.id.room_type);
        Button   btnJoin = card.findViewById(R.id.room_join_btn);

        tvName.setText(roomName);
        tvType.setText(roomType);

        btnJoin.setOnClickListener(v -> {
            Intent intent = new Intent(LiveChatMenuActivity.this, LiveChatActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("id", id);
            intent.putExtra("isAdmin", isAdmin);
            intent.putExtra("roomId", roomId);
            intent.putExtra("roomName", roomName);
            startActivity(intent);
        });

        roomListContainer.addView(card);
    }
}