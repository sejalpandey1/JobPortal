package com.jobportal.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;

public class AdminDashboardActivity extends BaseActivity {

    TextView tvTotalUsers, tvTotalJobs, tvTotalApplications, tvLogout;
    CardView cardManageUsers, cardManageJobs, cardManageChats;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalJobs = findViewById(R.id.tvTotalJobs);
        tvTotalApplications = findViewById(R.id.tvTotalApplications);
        tvLogout = findViewById(R.id.tvLogout);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageJobs = findViewById(R.id.cardManageJobs);
        cardManageChats = findViewById(R.id.cardManageChats);

        loadStats();

        cardManageUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));

        cardManageJobs.setOnClickListener(v ->
                startActivity(new Intent(this, AdminJobsActivity.class)));

        cardManageChats.setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));

        tvLogout.setOnClickListener(v -> {
            mAuth.signOut();
            getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                    .edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        db.collection(Constants.COLLECTION_USERS).get()
                .addOnSuccessListener(snap ->
                        tvTotalUsers.setText(String.valueOf(snap.size())));

        db.collection(Constants.COLLECTION_JOBS).get()
                .addOnSuccessListener(snap ->
                        tvTotalJobs.setText(String.valueOf(snap.size())));

        db.collection(Constants.COLLECTION_APPLICATIONS).get()
                .addOnSuccessListener(snap ->
                        tvTotalApplications.setText(String.valueOf(snap.size())));
    }
}