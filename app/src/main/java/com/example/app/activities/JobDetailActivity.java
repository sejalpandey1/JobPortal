package com.jobportal.app.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.MyFirebaseMessagingService;
import com.jobportal.app.R;
import com.jobportal.app.adapters.JobAdapter;
import com.jobportal.app.models.Application;
import com.jobportal.app.models.Job;
import com.jobportal.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class JobDetailActivity extends BaseActivity {

    TextView tvTitle, tvCompany, tvLocation, tvSalary,
            tvJobType, tvDescription, tvRequirements, tvCategory;
    Button btnApply, btnBookmark, btnShare, btnChatEmployer;
    RecyclerView rvSimilarJobs;
    JobAdapter similarJobAdapter;
    List<Job> similarJobList;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String jobId, jobCategory, employerIdForChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        jobId = getIntent().getStringExtra(Constants.KEY_JOB_ID);

        tvTitle = findViewById(R.id.tvTitle);
        tvCompany = findViewById(R.id.tvCompany);
        tvLocation = findViewById(R.id.tvLocation);
        tvSalary = findViewById(R.id.tvSalary);
        tvJobType = findViewById(R.id.tvJobType);
        tvDescription = findViewById(R.id.tvDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvCategory = findViewById(R.id.tvCategory);
        btnApply = findViewById(R.id.btnApply);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        btnChatEmployer = findViewById(R.id.btnChatEmployer);
        progressBar = findViewById(R.id.progressBar);
        rvSimilarJobs = findViewById(R.id.rvSimilarJobs);

        similarJobList = new ArrayList<>();
        similarJobAdapter = new JobAdapter(this, similarJobList);
        rvSimilarJobs.setLayoutManager(new LinearLayoutManager(this));
        rvSimilarJobs.setAdapter(similarJobAdapter);

        loadJobDetails();
        checkAlreadyApplied();
        checkBookmark();

        btnApply.setOnClickListener(v -> applyForJob());
        btnBookmark.setOnClickListener(v -> toggleBookmark());
        btnShare.setOnClickListener(v -> shareJob());

        btnChatEmployer.setOnClickListener(v -> {
            if (employerIdForChat != null) {
                db.collection("users").document(employerIdForChat)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String employerName =
                                    doc.getString("companyName") != null
                                            ? doc.getString("companyName")
                                            : doc.getString("name");
                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.putExtra("receiverId", employerIdForChat);
                            intent.putExtra("receiverName", employerName);
                            startActivity(intent);
                        });
            }
        });

        // Notification Permission Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void loadJobDetails() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.COLLECTION_JOBS).document(jobId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    tvTitle.setText(doc.getString("title"));
                    tvCompany.setText(doc.getString("company"));
                    tvLocation.setText(doc.getString("location"));
                    tvSalary.setText("₹ " + doc.getString("salary"));
                    tvJobType.setText(doc.getString("jobType"));
                    tvDescription.setText(doc.getString("description"));
                    tvRequirements.setText(doc.getString("requirements"));
                    String category = doc.getString("category");
                    if (category != null) tvCategory.setText(category);
                    jobCategory = category != null ? category : "";
                    employerIdForChat = doc.getString("employerId");
                    loadSimilarJobs();
                });
    }

    private void loadSimilarJobs() {
        if (jobCategory.isEmpty()) return;
        db.collection("jobs")
                .whereEqualTo("category", jobCategory)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    similarJobList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        if (!job.getJobId().equals(jobId)) {
                            similarJobList.add(job);
                        }
                    }
                    similarJobAdapter.notifyDataSetChanged();
                });
    }

    private void checkAlreadyApplied() {
        String seekerId = mAuth.getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("seekerId", seekerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        btnApply.setText("Already Applied ✓");
                        btnApply.setEnabled(false);
                        btnApply.setBackgroundTintList(
                                android.content.res.ColorStateList
                                        .valueOf(0xFF757575));
                    }
                });
    }

    private void checkBookmark() {
        String seekerId = mAuth.getCurrentUser().getUid();
        db.collection("bookmarks")
                .document(seekerId + "_" + jobId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btnBookmark.setText("Saved ✓");
                        btnBookmark.setBackgroundTintList(
                                android.content.res.ColorStateList
                                        .valueOf(0xFF2E7D32));
                    }
                });
    }

    private void toggleBookmark() {
        String seekerId = mAuth.getCurrentUser().getUid();
        String bookmarkId = seekerId + "_" + jobId;

        db.collection("bookmarks").document(bookmarkId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        db.collection("bookmarks").document(bookmarkId)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    btnBookmark.setText("Save Job");
                                    btnBookmark.setBackgroundTintList(
                                            android.content.res.ColorStateList
                                                    .valueOf(0xFF424242));
                                    Toast.makeText(this, "Bookmark removed!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        java.util.Map<String, Object> bookmark =
                                new java.util.HashMap<>();
                        bookmark.put("jobId", jobId);
                        bookmark.put("seekerId", seekerId);
                        bookmark.put("savedAt", System.currentTimeMillis());

                        db.collection("bookmarks").document(bookmarkId)
                                .set(bookmark)
                                .addOnSuccessListener(unused -> {
                                    btnBookmark.setText("Saved ✓");
                                    btnBookmark.setBackgroundTintList(
                                            android.content.res.ColorStateList
                                                    .valueOf(0xFF2E7D32));
                                    Toast.makeText(this, "Job Saved!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void shareJob() {
        db.collection(Constants.COLLECTION_JOBS).document(jobId)
                .get()
                .addOnSuccessListener(doc -> {
                    String title = doc.getString("title");
                    String company = doc.getString("company");
                    String location = doc.getString("location");
                    String salary = doc.getString("salary");

                    String shareText = "🔥 Job Opening!\n\n" +
                            "Position: " + title + "\n" +
                            "Company: " + company + "\n" +
                            "Location: " + location + "\n" +
                            "Salary: ₹" + salary + "\n\n" +
                            "Apply now on JobPortal App!";

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(shareIntent, "Share Job"));
                });
    }

    private void applyForJob() {
        progressBar.setVisibility(View.VISIBLE);
        btnApply.setEnabled(false);

        String seekerId = mAuth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_USERS).document(seekerId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String seekerName = userDoc.getString("name");
                    String seekerEmail = userDoc.getString("email");

                    db.collection(Constants.COLLECTION_JOBS).document(jobId)
                            .get()
                            .addOnSuccessListener(jobDoc -> {
                                String jobTitle = jobDoc.getString("title");
                                String employerId = jobDoc.getString("employerId");

                                String appId = db.collection(
                                                Constants.COLLECTION_APPLICATIONS)
                                        .document().getId();

                                Application application = new Application(
                                        appId, jobId, seekerId, employerId,
                                        seekerName, seekerEmail, jobTitle);

                                db.collection(Constants.COLLECTION_APPLICATIONS)
                                        .document(appId)
                                        .set(application)
                                        .addOnSuccessListener(unused -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnApply.setText("Already Applied ✓");
                                            btnApply.setBackgroundTintList(
                                                    android.content.res.ColorStateList
                                                            .valueOf(0xFF757575));
                                            Toast.makeText(this,
                                                    "Applied Successfully!",
                                                    Toast.LENGTH_SHORT).show();

                                            // Employer को Firestore notification
                                            String notifId = db.collection(
                                                            "notifications")
                                                    .document().getId();
                                            com.jobportal.app.models.Notification
                                                    notification =
                                                    new com.jobportal.app.models
                                                            .Notification(
                                                            notifId, employerId,
                                                            "📋 New Application!",
                                                            seekerName + " applied for "
                                                                    + jobTitle,
                                                            "application");
                                            db.collection("notifications")
                                                    .document(notifId)
                                                    .set(notification);

                                            // Seeker को Phone Pop-up
                                            MyFirebaseMessagingService
                                                    .sendLocalNotification(
                                                            JobDetailActivity.this,
                                                            "📋 Application Sent!",
                                                            "Your application for " +
                                                                    jobTitle +
                                                                    " has been sent!");
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnApply.setEnabled(true);
                                            Toast.makeText(this,
                                                    "Error: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            });
                });
    }
}