package com.jobportal.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;
import de.hdodenhof.circleimageview.CircleImageView;

public class SeekerProfileActivity extends BaseActivity {

    TextView tvName, tvEmail, tvPhone, tvSkills, tvExperience, tvEducation;
    CircleImageView ivProfile;
    Button btnEditProfile, btnChat, btnChangePassword,
            btnDeleteAccount, btnLogout;
    ProgressBar progressBar;
    BottomNavigationView bottomNav;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    Button btnResumeBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_profile);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvSkills = findViewById(R.id.tvSkills);
        tvExperience = findViewById(R.id.tvExperience);
        tvEducation = findViewById(R.id.tvEducation);
        ivProfile = findViewById(R.id.ivProfile);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChat = findViewById(R.id.btnChat);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
        bottomNav = findViewById(R.id.bottomNav);
        btnResumeBuilder = findViewById(R.id.btnResumeBuilder);

        loadProfile();
        setupBottomNav();

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));

        btnResumeBuilder.setOnClickListener(v ->
                startActivity(new Intent(this, ResumeBuilderActivity.class)));

        btnChangePassword.setOnClickListener(v -> changePassword());
        btnLogout.setOnClickListener(v -> logout());
        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_profile);
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
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    tvName.setText(doc.getString("name") != null
                            ? doc.getString("name") : "N/A");
                    tvEmail.setText(doc.getString("email") != null
                            ? doc.getString("email") : "N/A");
                    tvPhone.setText(doc.getString("phone") != null
                            ? doc.getString("phone") : "N/A");
                    tvSkills.setText(doc.getString("skills") != null
                            ? doc.getString("skills") : "Not added");
                    tvExperience.setText(doc.getString("experience") != null
                            ? doc.getString("experience") : "Not added");
                    tvEducation.setText(doc.getString("education") != null
                            ? doc.getString("education") : "Not added");

                    String profilePhoto = doc.getString("profilePhoto");
                    if (profilePhoto != null && !profilePhoto.isEmpty()) {
                        byte[] photoBytes = Base64.decode(
                                profilePhoto, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(
                                photoBytes, 0, photoBytes.length);
                        ivProfile.setImageBitmap(bitmap);
                    }
                });
    }

    private void changePassword() {
        String email = mAuth.getCurrentUser().getEmail();
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("क्या आप logout करना चाहते हैं?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                            .edit().clear().apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("⚠️ क्या आप account permanently delete करना चाहते हैं?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection("users").document(uid)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                mAuth.getCurrentUser().delete()
                                        .addOnSuccessListener(unused2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            getSharedPreferences(
                                                    Constants.PREF_NAME,
                                                    MODE_PRIVATE)
                                                    .edit().clear().apply();
                                            Toast.makeText(this,
                                                    "Account deleted!",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this,
                                                    LoginActivity.class);
                                            intent.setFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        });
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}