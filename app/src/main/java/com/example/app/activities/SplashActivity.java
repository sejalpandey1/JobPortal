package com.jobportal.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Step 1 — Onboarding check
            SharedPreferences onboardPrefs = getSharedPreferences(
                    "onboarding", MODE_PRIVATE);
            boolean onboardingDone = onboardPrefs
                    .getBoolean("completed", false);

            if (!onboardingDone) {
                // पहली बार app खुली — Onboarding दिखाओ
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
                return;
            }

            // Step 2 — Auto Login check
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                FirebaseFirestore.getInstance()
                        .collection(Constants.COLLECTION_USERS)
                        .document(uid)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String userType = doc.getString("userType");
                                SharedPreferences.Editor editor =
                                        getSharedPreferences(
                                                Constants.PREF_NAME,
                                                MODE_PRIVATE).edit();
                                editor.putString(Constants.PREF_USER_TYPE,
                                        userType);
                                editor.putString(Constants.PREF_USER_ID, uid);
                                editor.apply();

                                Intent intent;
                                if (userType.equals(
                                        Constants.USER_TYPE_SEEKER)) {
                                    intent = new Intent(this,
                                            JobSeekerDashboard.class);
                                } else if (userType.equals(
                                        Constants.USER_TYPE_EMPLOYER)) {
                                    intent = new Intent(this,
                                            EmployerDashboard.class);
                                } else if (userType.equals(
                                        Constants.USER_TYPE_ADMIN)) {
                                    intent = new Intent(this,
                                            AdminDashboardActivity.class);
                                } else {
                                    intent = new Intent(this,
                                            LoginActivity.class);
                                }
                                startActivity(intent);
                            } else {
                                startActivity(new Intent(this,
                                        LoginActivity.class));
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            startActivity(new Intent(this,
                                    LoginActivity.class));
                            finish();
                        });
            } else {
                // Logged out — Login page दिखाओ
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }, 2000);
    }
}