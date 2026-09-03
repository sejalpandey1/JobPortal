package com.jobportal.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.utils.Constants;
import java.util.HashMap;
import java.util.Map;

public class EditJobActivity extends BaseActivity {

    EditText etTitle, etCompany, etLocation, etSalary,
            etJobType, etDescription, etRequirements, etExpiryDate;
    Spinner spinnerCategory;
    Button btnUpdate;
    ProgressBar progressBar;
    FirebaseFirestore db;
    String jobId, selectedCategory = "IT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        jobId = getIntent().getStringExtra(Constants.KEY_JOB_ID);

        etTitle = findViewById(R.id.etTitle);
        etCompany = findViewById(R.id.etCompany);
        etLocation = findViewById(R.id.etLocation);
        etSalary = findViewById(R.id.etSalary);
        etJobType = findViewById(R.id.etJobType);
        etDescription = findViewById(R.id.etDescription);
        etRequirements = findViewById(R.id.etRequirements);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        String[] categories = {"IT", "Finance", "Marketing",
                "Healthcare", "Education", "Sales",
                "Engineering", "Design", "HR", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        selectedCategory = categories[position];
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        loadJobData();
        btnUpdate.setOnClickListener(v -> updateJob());
    }

    private void loadJobData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.COLLECTION_JOBS).document(jobId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    etTitle.setText(doc.getString("title"));
                    etCompany.setText(doc.getString("company"));
                    etLocation.setText(doc.getString("location"));
                    etSalary.setText(doc.getString("salary"));
                    etJobType.setText(doc.getString("jobType"));
                    etDescription.setText(doc.getString("description"));
                    etRequirements.setText(doc.getString("requirements"));
                    etExpiryDate.setText(doc.getString("expiryDate"));
                });
    }

    private void updateJob() {
        String title = etTitle.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Title डालो"); return; }
        if (TextUtils.isEmpty(company)) { etCompany.setError("Company डालो"); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Location डालो"); return; }
        if (TextUtils.isEmpty(salary)) { etSalary.setError("Salary डालो"); return; }
        if (TextUtils.isEmpty(description)) { etDescription.setError("Description डालो"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("company", company);
        updates.put("location", location);
        updates.put("salary", salary);
        updates.put("jobType", etJobType.getText().toString().trim());
        updates.put("description", description);
        updates.put("requirements", etRequirements.getText().toString().trim());
        updates.put("category", selectedCategory);
        updates.put("expiryDate", etExpiryDate.getText().toString().trim());

        db.collection(Constants.COLLECTION_JOBS).document(jobId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Job updated successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}