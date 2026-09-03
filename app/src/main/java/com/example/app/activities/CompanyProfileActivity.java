package com.jobportal.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;

public class CompanyProfileActivity extends BaseActivity {

    TextView tvCompanyName, tvCompanyEmail, tvCompanyPhone,
            tvCompanyWebsite, tvCompanyDescription,
            tvCompanyLocation, tvCompanySize, tvCompanyIndustry;
    Button btnEditCompany, btnChat, btnChangePassword,
            btnLogout, btnDeleteAccount;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyEmail = findViewById(R.id.tvCompanyEmail);
        tvCompanyPhone = findViewById(R.id.tvCompanyPhone);
        tvCompanyWebsite = findViewById(R.id.tvCompanyWebsite);
        tvCompanyDescription = findViewById(R.id.tvCompanyDescription);
        tvCompanyLocation = findViewById(R.id.tvCompanyLocation);
        tvCompanySize = findViewById(R.id.tvCompanySize);
        tvCompanyIndustry = findViewById(R.id.tvCompanyIndustry);
        btnEditCompany = findViewById(R.id.btnEditCompany);
        btnChat = findViewById(R.id.btnChat);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        progressBar = findViewById(R.id.progressBar);

        loadCompanyProfile();

        btnEditCompany.setOnClickListener(v ->
                startActivity(new Intent(this, EditCompanyProfileActivity.class)));

        btnChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));

        btnChangePassword.setOnClickListener(v -> changePassword());

        btnLogout.setOnClickListener(v -> logout());

        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompanyProfile();
    }

    private void loadCompanyProfile() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    tvCompanyName.setText(getValue(doc.getString("companyName")));
                    tvCompanyEmail.setText(getValue(doc.getString("companyEmail")));
                    tvCompanyPhone.setText(getValue(doc.getString("companyPhone")));
                    tvCompanyWebsite.setText(getValue(doc.getString("companyWebsite")));
                    tvCompanyDescription.setText(getValue(doc.getString("companyDescription")));
                    tvCompanyLocation.setText(getValue(doc.getString("companyLocation")));
                    tvCompanySize.setText(getValue(doc.getString("companySize")));
                    tvCompanyIndustry.setText(getValue(doc.getString("companyIndustry")));
                });
    }

    private String getValue(String value) {
        return value != null && !value.isEmpty() ? value : "Not added";
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
                .setMessage("⚠️ क्या आप अपना account permanently delete करना चाहते हैं?")
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
                                                    Constants.PREF_NAME, MODE_PRIVATE)
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