package com.jobportal.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.ApplicationAdapter;
import com.jobportal.app.models.Application;
import java.util.ArrayList;
import java.util.List;

public class MyApplicationsActivity extends BaseActivity {

    RecyclerView rvApplications;
    ApplicationAdapter applicationAdapter;
    List<Application> applicationList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvApplications = findViewById(R.id.rvApplications);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        applicationList = new ArrayList<>();
        applicationAdapter = new ApplicationAdapter(this, applicationList);
        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        rvApplications.setAdapter(applicationAdapter);

        applicationAdapter.setOnItemClickListener(app -> {
            Intent intent = new Intent(this, ApplicationDetailActivity.class);
            intent.putExtra("applicationId", app.getApplicationId());
            intent.putExtra("seekerId", app.getSeekerId());
            intent.putExtra("employerId", app.getEmployerId());
            intent.putExtra("jobTitle", app.getJobTitle());
            startActivity(intent);
        });

        loadApplications();
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("applications")
                .whereEqualTo("seekerId", uid)
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
                        rvApplications.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvApplications.setVisibility(View.VISIBLE);
                    }
                    applicationAdapter.notifyDataSetChanged();
                });
    }
}