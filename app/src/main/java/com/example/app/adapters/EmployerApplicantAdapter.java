package com.jobportal.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.activities.ChatActivity;
import com.jobportal.app.activities.InterviewScheduleActivity;
import com.jobportal.app.models.Application;
import com.jobportal.app.models.Notification;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmployerApplicantAdapter extends
        RecyclerView.Adapter<EmployerApplicantAdapter.ViewHolder> {

    Context context;
    List<Application> applicationList;
    FirebaseFirestore db;

    public EmployerApplicantAdapter(Context context,
                                    List<Application> applicationList) {
        this.context = context;
        this.applicationList = applicationList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_applicant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application app = applicationList.get(position);

        holder.tvName.setText(app.getSeekerName());
        holder.tvEmail.setText(app.getSeekerEmail());
        holder.tvJobTitle.setText(app.getJobTitle());

        String status = app.getStatus();
        holder.tvStatus.setText(status.toUpperCase());
        switch (status.toLowerCase()) {
            case "pending":
                holder.tvStatus.setBackgroundColor(0xFFFFA000);
                break;
            case "shortlisted":
                holder.tvStatus.setBackgroundColor(0xFF2E7D32);
                break;
            case "rejected":
                holder.tvStatus.setBackgroundColor(0xFFB71C1C);
                break;
            case "interview_scheduled":
                holder.tvStatus.setBackgroundColor(0xFF6A1B9A);
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault());
        holder.tvDate.setText("Applied: " +
                sdf.format(new Date(app.getAppliedDate())));

        holder.btnShortlist.setOnClickListener(v ->
                updateStatus(app.getApplicationId(), "shortlisted",
                        position, app.getSeekerId(), app.getJobTitle()));

        holder.btnReject.setOnClickListener(v ->
                updateStatus(app.getApplicationId(), "rejected",
                        position, app.getSeekerId(), app.getJobTitle()));

        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", app.getSeekerId());
            intent.putExtra("receiverName", app.getSeekerName());
            context.startActivity(intent);
        });

        holder.btnScheduleInterview.setOnClickListener(v -> {
            Intent intent = new Intent(context,
                    InterviewScheduleActivity.class);
            intent.putExtra("seekerId", app.getSeekerId());
            intent.putExtra("seekerName", app.getSeekerName());
            intent.putExtra("jobTitle", app.getJobTitle());
            intent.putExtra("applicationId", app.getApplicationId());
            context.startActivity(intent);
        });

        holder.btnViewResume.setOnClickListener(v -> {
            db.collection("users")
                    .document(app.getSeekerId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String resumeBase64 = doc.getString("resumeBase64");
                        if (resumeBase64 == null || resumeBase64.isEmpty()) {
                            Toast.makeText(context, "No resume uploaded!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            byte[] pdfBytes = android.util.Base64.decode(
                                    resumeBase64, android.util.Base64.DEFAULT);
                            File file = new File(
                                    ((android.app.Activity) context)
                                            .getCacheDir(),
                                    "resume_" + app.getSeekerId() + ".pdf");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(pdfBytes);
                            fos.close();

                            Uri uri = FileProvider.getUriForFile(context,
                                    context.getPackageName() + ".provider",
                                    file);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/pdf");
                            intent.setFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(Intent.createChooser(
                                    intent, "Open Resume"));
                        } catch (Exception e) {
                            Toast.makeText(context, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void updateStatus(String applicationId, String status,
                              int position, String seekerId, String jobTitle) {
        db.collection("applications").document(applicationId)
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    applicationList.get(position).setStatus(status);
                    notifyItemChanged(position);

                    // सिर्फ Seeker को notification भेजो
                    if (status.equals("shortlisted")) {
                        sendNotificationToSeeker(seekerId,
                                "🎉 Shortlisted!",
                                "Congratulations! You have been shortlisted for "
                                        + jobTitle,
                                "shortlisted");
                    } else {
                        sendNotificationToSeeker(seekerId,
                                "📋 Application Update",
                                "Your application for " + jobTitle +
                                        " status has been updated.",
                                "rejected");
                    }

                    Toast.makeText(context,
                            status.equals("shortlisted")
                                    ? "Shortlisted!" : "Rejected!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToSeeker(String seekerId, String title,
                                          String message, String type) {
        // Firestore में Seeker को notification save करो
        String notifId = db.collection("notifications").document().getId();
        Notification notification = new Notification(
                notifId, seekerId, title, message, type);
        db.collection("notifications")
                .document(notifId)
                .set(notification);
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvJobTitle, tvStatus, tvDate;
        Button btnShortlist, btnReject, btnViewResume,
                btnChat, btnScheduleInterview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnShortlist = itemView.findViewById(R.id.btnShortlist);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnViewResume = itemView.findViewById(R.id.btnViewResume);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnScheduleInterview = itemView.findViewById(
                    R.id.btnScheduleInterview);
        }
    }
}