package com.jobportal.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import java.util.HashMap;
import java.util.Map;

public class EditCompanyProfileActivity extends BaseActivity {

    EditText etCompanyName, etCompanyEmail, etCompanyPhone,
            etCompanyWebsite, etCompanyDescription,
            etCompanyLocation, etCompanySize, etCompanyIndustry;
    Button btnSave;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_company_profile);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etCompanyName = findViewById(R.id.etCompanyName);
        etCompanyEmail = findViewById(R.id.etCompanyEmail);
        etCompanyPhone = findViewById(R.id.etCompanyPhone);
        etCompanyWebsite = findViewById(R.id.etCompanyWebsite);
        etCompanyDescription = findViewById(R.id.etCompanyDescription);
        etCompanyLocation = findViewById(R.id.etCompanyLocation);
        etCompanySize = findViewById(R.id.etCompanySize);
        etCompanyIndustry = findViewById(R.id.etCompanyIndustry);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        loadExistingData();

        btnSave.setOnClickListener(v -> saveCompanyProfile());
    }

    private void loadExistingData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    etCompanyName.setText(doc.getString("companyName"));
                    etCompanyEmail.setText(doc.getString("companyEmail"));
                    etCompanyPhone.setText(doc.getString("companyPhone"));
                    etCompanyWebsite.setText(doc.getString("companyWebsite"));
                    etCompanyDescription.setText(doc.getString("companyDescription"));
                    etCompanyLocation.setText(doc.getString("companyLocation"));
                    etCompanySize.setText(doc.getString("companySize"));
                    etCompanyIndustry.setText(doc.getString("companyIndustry"));
                });
    }

    private void saveCompanyProfile() {
        String companyName = etCompanyName.getText().toString().trim();
        if (companyName.isEmpty()) {
            etCompanyName.setError("Company name डालो");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("companyName", companyName);
        updates.put("companyEmail", etCompanyEmail.getText().toString().trim());
        updates.put("companyPhone", etCompanyPhone.getText().toString().trim());
        updates.put("companyWebsite", etCompanyWebsite.getText().toString().trim());
        updates.put("companyDescription", etCompanyDescription.getText().toString().trim());
        updates.put("companyLocation", etCompanyLocation.getText().toString().trim());
        updates.put("companySize", etCompanySize.getText().toString().trim());
        updates.put("companyIndustry", etCompanyIndustry.getText().toString().trim());

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Company profile saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}