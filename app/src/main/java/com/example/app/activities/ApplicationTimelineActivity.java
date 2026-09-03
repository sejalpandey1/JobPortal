package com.jobportal.app.activities;

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
import com.jobportal.app.adapters.TimelineAdapter;
import com.jobportal.app.models.Application;
import java.util.ArrayList;
import java.util.List;

public class ApplicationTimelineActivity extends BaseActivity {

    RecyclerView rvTimeline;
    TimelineAdapter timelineAdapter;
    List<Application> applicationList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_timeline);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvTimeline = findViewById(R.id.rvTimeline);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        applicationList = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(this, applicationList);
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        rvTimeline.setAdapter(timelineAdapter);

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
                        rvTimeline.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvTimeline.setVisibility(View.VISIBLE);
                    }
                    timelineAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }
}