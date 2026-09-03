package com.jobportal.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.JobAdapter;
import com.jobportal.app.models.Job;
import java.util.ArrayList;
import java.util.List;

public class SavedJobsActivity extends BaseActivity {

    RecyclerView rvSavedJobs;
    JobAdapter jobAdapter;
    List<Job> jobList;
    ProgressBar progressBar;
    TextView tvEmpty;
    BottomNavigationView bottomNav;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_jobs);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvSavedJobs = findViewById(R.id.rvSavedJobs);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        bottomNav = findViewById(R.id.bottomNav);

        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobList);
        rvSavedJobs.setLayoutManager(new LinearLayoutManager(this));
        rvSavedJobs.setAdapter(jobAdapter);

        loadSavedJobs();
        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_saved);
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
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, SeekerProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadSavedJobs() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("bookmarks")
                .whereEqualTo("seekerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    jobList.clear();
                    progressBar.setVisibility(View.GONE);
                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String jobId = doc.getString("jobId");
                        db.collection("jobs").document(jobId)
                                .get()
                                .addOnSuccessListener(jobDoc -> {
                                    if (jobDoc.exists()) {
                                        Job job = jobDoc.toObject(Job.class);
                                        jobList.add(job);
                                        jobAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}