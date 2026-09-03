package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.AdminJobAdapter;
import com.jobportal.app.models.Job;
import java.util.ArrayList;
import java.util.List;

public class AdminJobsActivity extends BaseActivity {

    RecyclerView rvPending, rvApproved;
    AdminJobAdapter pendingAdapter, approvedAdapter;
    List<Job> pendingList, approvedList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_jobs);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();

        rvPending = findViewById(R.id.rvPending);
        rvApproved = findViewById(R.id.rvApproved);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        pendingList = new ArrayList<>();
        approvedList = new ArrayList<>();

        pendingAdapter = new AdminJobAdapter(this, pendingList, true);
        approvedAdapter = new AdminJobAdapter(this, approvedList, false);

        rvPending.setLayoutManager(new LinearLayoutManager(this));
        rvApproved.setLayoutManager(new LinearLayoutManager(this));

        rvPending.setAdapter(pendingAdapter);
        rvApproved.setAdapter(approvedAdapter);

        loadJobs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }

    private void loadJobs() {
        progressBar.setVisibility(View.VISIBLE);
        pendingList.clear();
        approvedList.clear();

        db.collection("jobs").get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Job job = doc.toObject(Job.class);
                        String status = job.getApprovalStatus();
                        if (status == null || status.equals("pending")) {
                            pendingList.add(job);
                        } else {
                            approvedList.add(job);
                        }
                    }
                    if (pendingList.isEmpty() && approvedList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                    }
                    pendingAdapter.notifyDataSetChanged();
                    approvedAdapter.notifyDataSetChanged();
                });
    }
}