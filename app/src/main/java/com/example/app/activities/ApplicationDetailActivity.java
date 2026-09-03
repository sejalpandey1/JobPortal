package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;

public class ApplicationDetailActivity extends BaseActivity {

    TextView tvJobTitle, tvCompany, tvStatus, tvAppliedDate,
            tvSeekerName, tvSeekerEmail, tvSeekerPhone, tvSeekerSkills;
    Button btnWithdraw, btnChat;
    ProgressBar progressBar;
    FirebaseFirestore db;
    String applicationId, seekerId, employerId, jobTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_detail);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();

        applicationId = getIntent().getStringExtra("applicationId");
        seekerId = getIntent().getStringExtra("seekerId");
        employerId = getIntent().getStringExtra("employerId");
        jobTitle = getIntent().getStringExtra("jobTitle");

        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvCompany = findViewById(R.id.tvCompany);
        tvStatus = findViewById(R.id.tvStatus);
        tvAppliedDate = findViewById(R.id.tvAppliedDate);
        tvSeekerName = findViewById(R.id.tvSeekerName);
        tvSeekerEmail = findViewById(R.id.tvSeekerEmail);
        tvSeekerPhone = findViewById(R.id.tvSeekerPhone);
        tvSeekerSkills = findViewById(R.id.tvSeekerSkills);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnChat = findViewById(R.id.btnChat);
        progressBar = findViewById(R.id.progressBar);

        loadDetails();

        btnChat.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(
                    this, ChatActivity.class);
            intent.putExtra("receiverId", employerId);
            intent.putExtra("receiverName", tvCompany.getText().toString());
            startActivity(intent);
        });

        btnWithdraw.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Withdraw Application")
                    .setMessage("क्या आप यह application withdraw करना चाहते हैं?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("applications")
                                .document(applicationId)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this,
                                            "Application withdrawn!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void loadDetails() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("applications").document(applicationId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    tvJobTitle.setText(doc.getString("jobTitle"));
                    String status = doc.getString("status");
                    tvStatus.setText(status != null
                            ? status.toUpperCase() : "PENDING");
                    switch (status != null ? status : "pending") {
                        case "shortlisted":
                            tvStatus.setBackgroundColor(0xFF2E7D32);
                            break;
                        case "rejected":
                            tvStatus.setBackgroundColor(0xFFB71C1C);
                            break;
                        default:
                            tvStatus.setBackgroundColor(0xFFFFA000);
                    }

                    long appliedDate = doc.getLong("appliedDate") != null
                            ? doc.getLong("appliedDate") : 0;
                    java.text.SimpleDateFormat sdf =
                            new java.text.SimpleDateFormat(
                                    "dd MMM yyyy", java.util.Locale.getDefault());
                    tvAppliedDate.setText("Applied: " +
                            sdf.format(new java.util.Date(appliedDate)));

                    tvSeekerName.setText(doc.getString("seekerName"));
                    tvSeekerEmail.setText(doc.getString("seekerEmail"));

                    db.collection("users").document(seekerId)
                            .get()
                            .addOnSuccessListener(userDoc -> {
                                tvSeekerPhone.setText(
                                        userDoc.getString("phone") != null
                                                ? userDoc.getString("phone") : "N/A");
                                tvSeekerSkills.setText(
                                        userDoc.getString("skills") != null
                                                ? userDoc.getString("skills")
                                                : "Not added");
                            });

                    db.collection("jobs").document(
                                    doc.getString("jobId") != null
                                            ? doc.getString("jobId") : "")
                            .get()
                            .addOnSuccessListener(jobDoc -> {
                                if (jobDoc.exists()) {
                                    tvCompany.setText(
                                            jobDoc.getString("company"));
                                }
                            });
                });
    }
}