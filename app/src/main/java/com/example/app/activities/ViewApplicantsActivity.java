package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.EmployerApplicantAdapter;
import com.jobportal.app.models.Application;
import com.jobportal.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class ViewApplicantsActivity extends BaseActivity {

    RecyclerView rvApplicants;
    EmployerApplicantAdapter applicantAdapter;
    List<Application> applicationList;
    ProgressBar progressBar;
    TextView tvEmpty, tvJobTitle;
    FirebaseFirestore db;
    String jobId, jobTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();

        jobId = getIntent().getStringExtra(Constants.KEY_JOB_ID);
        jobTitle = getIntent().getStringExtra("jobTitle");

        rvApplicants = findViewById(R.id.rvApplicants);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvJobTitle = findViewById(R.id.tvJobTitle);

        tvJobTitle.setText(jobTitle);

        applicationList = new ArrayList<>();
        applicantAdapter = new EmployerApplicantAdapter(this, applicationList);
        rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        rvApplicants.setAdapter(applicantAdapter);

        loadApplicants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApplicants();
    }

    private void loadApplicants() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("jobId", jobId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    applicationList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Application app = doc.toObject(Application.class);
                        applicationList.add(app);
                    }
                    if (applicationList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvApplicants.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvApplicants.setVisibility(View.VISIBLE);
                    }
                    applicantAdapter.notifyDataSetChanged();
                });
    }
}