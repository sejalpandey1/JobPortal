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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.Job;
import com.jobportal.app.utils.Constants;

public class PostJobActivity extends BaseActivity {

    EditText etTitle, etCompany, etLocation, etSalary, etJobType,
            etDescription, etRequirements;
    Spinner spinnerCategory;
    Button btnPost;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String selectedCategory = "IT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etCompany = findViewById(R.id.etCompany);
        etLocation = findViewById(R.id.etLocation);
        etSalary = findViewById(R.id.etSalary);
        etJobType = findViewById(R.id.etJobType);
        etDescription = findViewById(R.id.etDescription);
        etRequirements = findViewById(R.id.etRequirements);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);

        String[] categories = {"IT", "Finance", "Marketing", "Healthcare",
                "Education", "Sales", "Engineering", "Design", "HR", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
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

        btnPost.setOnClickListener(v -> postJob());
    }

    private void postJob() {
        String title = etTitle.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String jobType = etJobType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String requirements = etRequirements.getText().toString().trim();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Job Title डालो"); return; }
        if (TextUtils.isEmpty(company)) { etCompany.setError("Company Name डालो"); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Location डालो"); return; }
        if (TextUtils.isEmpty(salary)) { etSalary.setError("Salary डालो"); return; }
        if (TextUtils.isEmpty(description)) { etDescription.setError("Description डालो"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        String jobId = db.collection(Constants.COLLECTION_JOBS).document().getId();

        Job job = new Job(jobId, title, company, location, salary, description, uid);
        job.setJobType(jobType);
        job.setRequirements(requirements);
        job.setCategory(selectedCategory);
        job.setApprovalStatus("pending");

        db.collection(Constants.COLLECTION_JOBS).document(jobId)
                .set(job)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Job Posted Successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPost.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}