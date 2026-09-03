package com.jobportal.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.User;
import com.jobportal.app.utils.Constants;

public class RegisterActivity extends BaseActivity {

    EditText etName, etEmail, etPassword, etPhone;
    Button btnRegister;
    TextView tvLogin;
    RadioGroup rgUserType;
    RadioButton rbSeeker, rbEmployer;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        applyWindowInsets(findViewById(R.id.rootLayout));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        rgUserType = findViewById(R.id.rgUserType);
        rbSeeker = findViewById(R.id.rbSeeker);
        rbEmployer = findViewById(R.id.rbEmployer);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("नाम डालो"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email डालो"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password डालो"); return; }
        if (password.length() < 6) { etPassword.setError("Password 6 characters का होना चाहिए"); return; }
        if (rgUserType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Job Seeker या Employer select करो",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userType = rbSeeker.isChecked()
                ? Constants.USER_TYPE_SEEKER : Constants.USER_TYPE_EMPLOYER;

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    User user = new User(uid, name, email, phone, userType);
                    db.collection(Constants.COLLECTION_USERS).document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                mAuth.getCurrentUser()
                                        .sendEmailVerification();
                                progressBar.setVisibility(View.GONE);
                                new androidx.appcompat.app.AlertDialog
                                        .Builder(this)
                                        .setTitle("✅ Registration Successful!")
                                        .setMessage(
                                                "Account created successfully!")
                                        .setPositiveButton("OK",
                                                (dialog, which) -> {
                                                    startActivity(new Intent(this,
                                                            LoginActivity.class));
                                                    finish();
                                                })
                                        .show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}