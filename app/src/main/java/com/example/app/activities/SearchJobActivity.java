package com.jobportal.app.activities;

import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.JobAdapter;
import com.jobportal.app.models.Job;
import java.util.ArrayList;
import java.util.List;

public class SearchJobActivity extends BaseActivity {

    EditText etSearch;
    Spinner spinnerJobType, spinnerSalary;
    RecyclerView rvJobs;
    JobAdapter jobAdapter;
    List<Job> allJobList, filteredList;
    ProgressBar progressBar;
    TextView tvEmpty;
    BottomNavigationView bottomNav;
    FirebaseFirestore db;
    String selectedJobType = "All";
    String selectedSalary = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_job);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();

        etSearch = findViewById(R.id.etSearch);
        spinnerJobType = findViewById(R.id.spinnerJobType);
        spinnerSalary = findViewById(R.id.spinnerSalary);
        rvJobs = findViewById(R.id.rvJobs);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        bottomNav = findViewById(R.id.bottomNav);

        allJobList = new ArrayList<>();
        filteredList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, filteredList);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(jobAdapter);

        setupSpinners();
        loadJobs();
        setupBottomNav(R.id.nav_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterJobs(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNav(int selectedItem) {
        bottomNav.setSelectedItemId(selectedItem);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, JobSeekerDashboard.class));
                finish();
                return true;
            } else if (id == R.id.nav_search) {
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
                startActivity(new Intent(this, SeekerProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupSpinners() {
        String[] jobTypes = {"All", "Full Time", "Part Time", "Remote", "Internship"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, jobTypes);
        typeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerJobType.setAdapter(typeAdapter);
        spinnerJobType.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent,
                                                         View view, int position, long id) {
                        selectedJobType = jobTypes[position];
                        filterJobs();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });

        String[] salaryRanges = {"All", "0-15,000", "15,000-30,000",
                "30,000-50,000", "50,000-1,00,000", "1,00,000+"};
        ArrayAdapter<String> salaryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, salaryRanges);
        salaryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerSalary.setAdapter(salaryAdapter);
        spinnerSalary.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent,
                                                         View view, int position, long id) {
                        selectedSalary = salaryRanges[position];
                        filterJobs();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    private void loadJobs() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("jobs")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    allJobList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        // सारी jobs add करो — filter बाद में होगा
                        allJobList.add(job);
                    }
                    filterJobs();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Error loading jobs: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void filterJobs() {
        String searchText = etSearch.getText().toString().trim().toLowerCase();
        filteredList.clear();
        for (Job job : allJobList) {
            boolean matchesSearch = searchText.isEmpty()
                    || job.getTitle().toLowerCase().contains(searchText)
                    || job.getCompany().toLowerCase().contains(searchText)
                    || job.getLocation().toLowerCase().contains(searchText);
            boolean matchesType = selectedJobType.equals("All")
                    || (job.getJobType() != null
                    && job.getJobType().equalsIgnoreCase(selectedJobType));
            boolean matchesSalary = matchesSalaryRange(job.getSalary());
            if (matchesSearch && matchesType && matchesSalary) {
                filteredList.add(job);
            }
        }
        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvJobs.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvJobs.setVisibility(View.VISIBLE);
        }
        jobAdapter.notifyDataSetChanged();
    }

    private boolean matchesSalaryRange(String salaryStr) {
        if (selectedSalary.equals("All") || salaryStr == null) return true;
        try {
            int salary = Integer.parseInt(salaryStr.replaceAll("[^0-9]", ""));
            switch (selectedSalary) {
                case "0-15,000": return salary <= 15000;
                case "15,000-30,000": return salary > 15000 && salary <= 30000;
                case "30,000-50,000": return salary > 30000 && salary <= 50000;
                case "50,000-1,00,000": return salary > 50000 && salary <= 100000;
                case "1,00,000+": return salary > 100000;
                default: return true;
            }
        } catch (Exception e) { return true; }
    }
}