package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.NotificationAdapter;
import com.jobportal.app.models.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends BaseActivity {

    RecyclerView rvNotifications;
    NotificationAdapter adapter;
    List<Notification> notificationList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvNotifications = findViewById(R.id.rvNotifications);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    progressBar.setVisibility(View.GONE);
                    notificationList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Notification notification =
                                doc.toObject(Notification.class);
                        notificationList.add(notification);
                    }

                    // सारी notifications read mark करो
                    markAllAsRead(uid);

                    if (notificationList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvNotifications.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvNotifications.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // Notification Activity खुलते ही सब read हो जाएं
    private void markAllAsRead(String uid) {
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().update("read", true);
                    }
                });
    }
}