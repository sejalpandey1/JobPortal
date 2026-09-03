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
import com.jobportal.app.adapters.InterviewAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyInterviewsActivity extends BaseActivity {

    RecyclerView rvInterviews;
    InterviewAdapter interviewAdapter;
    List<Map<String, Object>> interviewList;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_interviews);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvInterviews = findViewById(R.id.rvInterviews);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        interviewList = new ArrayList<>();
        interviewAdapter = new InterviewAdapter(this, interviewList);
        rvInterviews.setLayoutManager(new LinearLayoutManager(this));
        rvInterviews.setAdapter(interviewAdapter);

        loadInterviews();
    }

    private void loadInterviews() {
        progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("interviews")
                .whereEqualTo("seekerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    interviewList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> interview =
                                new HashMap<>(doc.getData());
                        interviewList.add(interview);
                    }
                    if (interviewList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvInterviews.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvInterviews.setVisibility(View.VISIBLE);
                    }
                    interviewAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }
}