package com.jobportal.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.EmployerJobAdapter;
import com.jobportal.app.models.Job;
import com.jobportal.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class EmployerDashboard extends BaseActivity {

    TextView tvWelcome, tvLogout, tvJobsPosted,
            tvTotalApplicants, tvShortlisted;
    CardView cardPostJob, cardCompany, cardChat, cardProfile;
    RecyclerView rvMyJobs;
    BottomNavigationView bottomNav;
    EmployerJobAdapter jobAdapter;
    List<Job> jobList;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_dashboard);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvLogout = findViewById(R.id.tvLogout);
        tvJobsPosted = findViewById(R.id.tvJobsPosted);
        tvTotalApplicants = findViewById(R.id.tvTotalApplicants);
        tvShortlisted = findViewById(R.id.tvShortlisted);
        cardPostJob = findViewById(R.id.cardPostJob);
        cardCompany = findViewById(R.id.cardCompany);
        cardChat = findViewById(R.id.cardChat);
        cardProfile = findViewById(R.id.cardProfile);
        rvMyJobs = findViewById(R.id.rvMyJobs);
        bottomNav = findViewById(R.id.employerBottomNav);

        jobList = new ArrayList<>();
        jobAdapter = new EmployerJobAdapter(this, jobList);
        rvMyJobs.setLayoutManager(new LinearLayoutManager(this));
        rvMyJobs.setAdapter(jobAdapter);

        loadUserName();
        loadMyJobs();
        loadStats();
        loadUnreadMessages();

        cardPostJob.setOnClickListener(v ->
                startActivity(new Intent(this, PostJobActivity.class)));

        cardCompany.setOnClickListener(v ->
                startActivity(new Intent(this, CompanyProfileActivity.class)));

        cardChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));

        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, CompanyProfileActivity.class)));

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
            } else if (id == R.id.nav_post) {
                startActivity(new Intent(this, PostJobActivity.class));
                return true;
            } else if (id == R.id.nav_applicants) {
                // All jobs की applicants दिखाने के लिए
                // अलग Activity बनाते हैं
                startActivity(new Intent(this, AllApplicantsActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatListActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, CompanyProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyJobs();
        loadStats();
        loadUnreadMessages();
    }

    // 💬 Unread Messages Badge
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
                                        Color.parseColor("#FF0000"));
                    } else {
                        bottomNav.removeBadge(R.id.nav_chat);
                    }
                });

        // New Applications Badge
        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("employerId", uid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> {
                    int count = snap.size();
                    if (count > 0) {
                        bottomNav.getOrCreateBadge(R.id.nav_applicants)
                                .setNumber(count);
                        bottomNav.getOrCreateBadge(R.id.nav_applicants)
                                .setBackgroundColor(
                                        Color.parseColor("#FF0000"));
                    } else {
                        bottomNav.removeBadge(R.id.nav_applicants);
                    }
                });
    }

    private void loadStats() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_JOBS)
                .whereEqualTo("employerId", uid)
                .get()
                .addOnSuccessListener(snap ->
                        tvJobsPosted.setText(String.valueOf(snap.size())));

        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("employerId", uid)
                .get()
                .addOnSuccessListener(snap ->
                        tvTotalApplicants.setText(String.valueOf(snap.size())));

        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("employerId", uid)
                .whereEqualTo("status", "shortlisted")
                .get()
                .addOnSuccessListener(snap ->
                        tvShortlisted.setText(String.valueOf(snap.size())));
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

    private void loadMyJobs() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_JOBS)
                .whereEqualTo("employerId", uid)
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