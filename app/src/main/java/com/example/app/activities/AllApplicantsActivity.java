package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.EmployerApplicantAdapter;
import com.jobportal.app.models.Application;
import com.jobportal.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class AllApplicantsActivity extends BaseActivity {

    RecyclerView rvApplicants;
    EmployerApplicantAdapter applicantAdapter;
    List<Application> applicationList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_applicants);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvApplicants = findViewById(R.id.rvApplicants);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        applicationList = new ArrayList<>();
        applicantAdapter = new EmployerApplicantAdapter(
                this, applicationList);
        rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        rvApplicants.setAdapter(applicantAdapter);

        loadAllApplicants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllApplicants();
    }

    private void loadAllApplicants() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_APPLICATIONS)
                .whereEqualTo("employerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    applicationList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Application app = doc.toObject(Application.class);
                        applicationList.add(app);
                    }
                    if (applicationList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvApplicants.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvApplicants.setVisibility(View.VISIBLE);
                    }
                    applicantAdapter.notifyDataSetChanged();
                });
    }
}