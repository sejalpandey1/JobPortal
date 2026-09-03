package com.jobportal.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;

public class LoginActivity extends BaseActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoogleLogin;
    TextView tvRegister, tvForgotPassword;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        applyWindowInsets(findViewById(R.id.rootLayout));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        btnGoogleLogin.setVisibility(View.GONE);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("Email डालो");
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this,
                                    "Password reset email sent!",
                                    Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email डालो");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password डालो");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    db.collection(Constants.COLLECTION_USERS).document(uid)
                            .get()
                            .addOnSuccessListener(doc -> {
                                String userType = doc.getString("userType");
                                savePrefsAndNavigate(uid, userType);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Login failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void savePrefsAndNavigate(String uid, String userType) {
        progressBar.setVisibility(View.GONE);
        SharedPreferences.Editor editor = getSharedPreferences(
                Constants.PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(Constants.PREF_USER_TYPE, userType);
        editor.putString(Constants.PREF_USER_ID, uid);
        editor.apply();

        if (userType.equals(Constants.USER_TYPE_SEEKER)) {
            startActivity(new Intent(this, JobSeekerDashboard.class));
        } else if (userType.equals(Constants.USER_TYPE_EMPLOYER)) {
            startActivity(new Intent(this, EmployerDashboard.class));
        } else if (userType.equals(Constants.USER_TYPE_ADMIN)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        }
        finish();
    }
}