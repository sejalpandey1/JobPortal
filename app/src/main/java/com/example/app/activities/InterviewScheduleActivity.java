package com.jobportal.app.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.Notification;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class InterviewScheduleActivity extends BaseActivity {

    EditText etInterviewType, etLocation, etNotes;
    TextView tvDate, tvTime, tvCandidateName;
    Button btnPickDate, btnPickTime, btnSchedule;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String seekerId, seekerName, jobTitle, applicationId;
    int selectedYear, selectedMonth, selectedDay,
            selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_schedule);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        seekerId = getIntent().getStringExtra("seekerId");
        seekerName = getIntent().getStringExtra("seekerName");
        jobTitle = getIntent().getStringExtra("jobTitle");
        applicationId = getIntent().getStringExtra("applicationId");

        etInterviewType = findViewById(R.id.etInterviewType);
        etLocation = findViewById(R.id.etLocation);
        etNotes = findViewById(R.id.etNotes);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvCandidateName = findViewById(R.id.tvCandidateName);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSchedule = findViewById(R.id.btnSchedule);
        progressBar = findViewById(R.id.progressBar);

        tvCandidateName.setText("Candidate: " + seekerName);

        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = day;
                tvDate.setText(day + "/" + (month + 1) + "/" + year);
            }, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                selectedHour = hour;
                selectedMinute = minute;
                tvTime.setText(String.format("%02d:%02d", hour, minute));
            }, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE), true).show();
        });

        btnSchedule.setOnClickListener(v -> scheduleInterview());
    }

    private void scheduleInterview() {
        String type = etInterviewType.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (tvDate.getText().toString().equals("Not selected")) {
            Toast.makeText(this, "Date select करो", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tvTime.getText().toString().equals("Not selected")) {
            Toast.makeText(this, "Time select करो", Toast.LENGTH_SHORT).show();
            return;
        }
        if (type.isEmpty()) {
            etInterviewType.setError("Interview type डालो");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSchedule.setEnabled(false);

        String interviewId = db.collection("interviews").document().getId();
        Map<String, Object> interview = new HashMap<>();
        interview.put("interviewId", interviewId);
        interview.put("seekerId", seekerId);
        interview.put("seekerName", seekerName);
        interview.put("employerId", mAuth.getCurrentUser().getUid());
        interview.put("jobTitle", jobTitle);
        interview.put("applicationId", applicationId);
        interview.put("type", type);
        interview.put("location", location);
        interview.put("notes", notes);
        interview.put("date", tvDate.getText().toString());
        interview.put("time", tvTime.getText().toString());
        interview.put("status", "scheduled");
        interview.put("createdAt", System.currentTimeMillis());

        db.collection("interviews").document(interviewId)
                .set(interview)
                .addOnSuccessListener(unused -> {
                    db.collection("applications")
                            .document(applicationId)
                            .update("status", "interview_scheduled");

                    String notifId = db.collection("notifications")
                            .document().getId();
                    Notification notification = new Notification(
                            notifId, seekerId,
                            "🎉 Interview Scheduled!",
                            "Your interview for " + jobTitle +
                                    " is scheduled on " +
                                    tvDate.getText() + " at " +
                                    tvTime.getText(),
                            "interview");
                    db.collection("notifications")
                            .document(notifId)
                            .set(notification);

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Interview scheduled successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSchedule.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}