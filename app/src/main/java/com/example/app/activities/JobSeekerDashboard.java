package com.jobportal.app.activities;

import android.content.Intent;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.JobAdapter;
import com.jobportal.app.models.Job;
import com.jobportal.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class JobSeekerDashboard extends BaseActivity {

    TextView tvWelcome, tvLogout, tvSearchAll,
            tvJobCount, tvAppCount, tvSavedCount, tvNotifications;
    EditText etQuickSearch;
    RecyclerView rvJobs;
    BottomNavigationView bottomNav;
    CardView cardApplications, cardSaved, cardProfile,
            cardChat, cardTimeline, cardInterviews;
    LinearLayout llCategories;
    JobAdapter jobAdapter;
    List<Job> jobList;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String[] categories = {"All", "IT", "Finance", "Marketing",
            "Healthcare", "Education", "Sales",
            "Engineering", "Design", "HR"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_seeker_dashboard);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvLogout = findViewById(R.id.tvLogout);
        tvSearchAll = findViewById(R.id.tvSearchAll);
        tvJobCount = findViewById(R.id.tvJobCount);
        tvAppCount = findViewById(R.id.tvAppCount);
        tvSavedCount = findViewById(R.id.tvSavedCount);
        tvNotifications = findViewById(R.id.tvNotifications);
        etQuickSearch = findViewById(R.id.etQuickSearch);
        rvJobs = findViewById(R.id.rvJobs);
        bottomNav = findViewById(R.id.bottomNav);
        cardApplications = findViewById(R.id.cardApplications);
        cardSaved = findViewById(R.id.cardSaved);
        cardProfile = findViewById(R.id.cardProfile);
        cardChat = findViewById(R.id.cardChat);
        cardTimeline = findViewById(R.id.cardTimeline);
        cardInterviews = findViewById(R.id.cardInterviews);
        llCategories = findViewById(R.id.llCategories);

        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobList);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(jobAdapter);

        loadUserName();
        loadJobs();
        loadStats();
        setupCategories();
        loadUnreadNotifications();
        loadUnreadMessages();

        etQuickSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchJobActivity.class)));

        tvSearchAll.setOnClickListener(v ->
                startActivity(new Intent(this, SearchJobActivity.class)));

        tvNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        cardApplications.setOnClickListener(v ->
                startActivity(new Intent(this, MyApplicationsActivity.class)));

        cardSaved.setOnClickListener(v ->
                startActivity(new Intent(this, SavedJobsActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, SeekerProfileActivity.class)));

        cardChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));

        cardTimeline.setOnClickListener(v ->
                startActivity(new Intent(this,
                        ApplicationTimelineActivity.class)));

        cardInterviews.setOnClickListener(v ->
                startActivity(new Intent(this, MyInterviewsActivity.class)));

        tvLogout.setOnClickListener(v -> {
            mAuth.signOut();
            getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchJobActivity.class));
                return true;
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(this, SavedJobsActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatListActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SeekerProfileActivity.class));
                return true;
            }
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadNotifications();
        loadUnreadMessages();
        loadStats();
    }

    // 🔔 Unread Notifications Badge
    private void loadUnreadNotifications() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snap -> {
                    int count = snap.size();
                    if (count > 0) {
                        tvNotifications.setText("🔔 " + count);
                        tvNotifications.setTextColor(
                                Color.parseColor("#FF0000"));
                    } else {
                        tvNotifications.setText("🔔");
                        tvNotifications.setTextColor(
                                Color.parseColor("#FFFFFF"));
                    }
                });
    }

    // 💬 Unread Messages Badge on Bottom Nav
    private void loadUnreadMessages() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("type", "chat")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snap -> {
                    int count = snap.size();
                    if (count > 0) {
                        bottomNav.getOrCreateBadge(R.id.nav_chat)
                                .setNumber(count);
                        bottomNav.getOrCreateBadge(R.id.nav_chat)
                                .setBackgroundColor(
                                        android.graphics.Color.parseColor(
                                                "#FF0000"));
                    } else {
                        // कोई unread नहीं — badge हटाओ
                        bottomNav.removeBadge(R.id.nav_chat);
                    }
                });
    }

    private void setupCategories() {
        for (String category : categories) {
            TextView chip = new TextView(this);
            chip.setText(category);
            chip.setTextSize(12);
            chip.setTextColor(Color.parseColor("#1565C0"));
            chip.setPadding(32, 14, 32, 14);
            chip.setGravity(Gravity.CENTER);

            android.graphics.drawable.GradientDrawable bg =
                    new android.graphics.drawable.GradientDrawable();
            bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            bg.setCornerRadius(50f);
            bg.setColor(Color.parseColor("#FFFFFF"));
            bg.setStroke(1, Color.parseColor("#1565C0"));
            chip.setBackground(bg);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(6, 0, 6, 0);
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> {
                // Selected chip highlight
                llCategories.getChildCount();
                for (int i = 0; i < llCategories.getChildCount(); i++) {
                    TextView c = (TextView) llCategories.getChildAt(i);
                    android.graphics.drawable.GradientDrawable d =
                            new android.graphics.drawable.GradientDrawable();
                    d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    d.setCornerRadius(50f);
                    d.setColor(Color.parseColor("#FFFFFF"));
                    d.setStroke(1, Color.parseColor("#1565C0"));
                    c.setBackground(d);
                    c.setTextColor(Color.parseColor("#1565C0"));
                }

                android.graphics.drawable.GradientDrawable selected =
                        new android.graphics.drawable.GradientDrawable();
                selected.setShape(
                        android.graphics.drawable.GradientDrawable.RECTANGLE);
                selected.setCornerRadius(50f);
                selected.setColor(Color.parseColor("#1565C0"));
                chip.setBackground(selected);
                chip.setTextColor(Color.parseColor("#FFFFFF"));

                filterByCategory(category);
            });
            llCategories.addView(chip);
        }
    }

    private void filterByCategory(String category) {
        if (category.equals("All")) {
            loadJobs();
            return;
        }
        db.collection(Constants.COLLECTION_JOBS)
                .whereEqualTo("active", true)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        jobList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();
                });
    }

    private void loadStats() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_JOBS)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(snap ->
                        tvJobCount.setText(String.valueOf(snap.size())));

        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("seekerId", uid)
                .get()
                .addOnSuccessListener(snap ->
                        tvAppCount.setText(String.valueOf(snap.size())));

        db.collection("bookmarks")
                .whereEqualTo("seekerId", uid)
                .get()
                .addOnSuccessListener(snap ->
                        tvSavedCount.setText(String.valueOf(snap.size())));
    }

    private void loadUserName() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    tvWelcome.setText("Hello, " + name + "! 👋");
                });
    }

    private void loadJobs() {
        db.collection(Constants.COLLECTION_JOBS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        String approval = job.getApprovalStatus();
                        // Active हो AND approved हो OR approval null हो
                        if (job.isActive() && (approval == null
                                || approval.isEmpty()
                                || approval.equals("approved"))) {
                            jobList.add(job);
                        }
                    }
                    if (jobList.isEmpty()) {
                        // Firestore में active field नहीं है
                        // तो सारी jobs दिखाओ
                        reloadAllJobs();
                    } else {
                        jobAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void reloadAllJobs() {
        db.collection(Constants.COLLECTION_JOBS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    jobList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        jobList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();
                });
    }
}