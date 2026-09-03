package com.jobportal.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.ChatListAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListActivity extends BaseActivity {

    RecyclerView rvChats;
    ChatListAdapter chatListAdapter;
    List<Map<String, Object>> chatList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    BottomNavigationView bottomNav;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    ListenerRegistration listener1, listener2;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        rvChats = findViewById(R.id.rvChats);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        bottomNav = findViewById(R.id.bottomNav);

        chatList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(this, chatList);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(chatListAdapter);

        loadChats();
        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_chat);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, JobSeekerDashboard.class));
                finish();
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchJobActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedJobsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_chat) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SeekerProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadChats() {
        progressBar.setVisibility(View.VISIBLE);
        listener1 = db.collection("chats")
                .whereEqualTo("user1", currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    updateChatList(snapshots);
                });
        listener2 = db.collection("chats")
                .whereEqualTo("user2", currentUserId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    updateChatList(snapshots);
                });
    }

    private void updateChatList(
            com.google.firebase.firestore.QuerySnapshot snapshots) {
        progressBar.setVisibility(View.GONE);
        for (QueryDocumentSnapshot doc : snapshots) {
            Map<String, Object> chat = new HashMap<>(doc.getData());
            chat.put("chatRoomId", doc.getId());
            boolean found = false;
            for (int i = 0; i < chatList.size(); i++) {
                if (doc.getId().equals(chatList.get(i).get("chatRoomId"))) {
                    chatList.set(i, chat);
                    found = true;
                    break;
                }
            }
            if (!found) chatList.add(chat);
        }
        if (chatList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvChats.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvChats.setVisibility(View.VISIBLE);
        }
        chatListAdapter.sortByRecentMessage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener1 != null) listener1.remove();
        if (listener2 != null) listener2.remove();
    }
}