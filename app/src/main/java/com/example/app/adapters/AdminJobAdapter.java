package com.jobportal.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.Job;
import com.jobportal.app.models.Notification;
import java.util.List;

public class AdminJobAdapter extends RecyclerView.Adapter<AdminJobAdapter.ViewHolder> {

    Context context;
    List<Job> jobList;
    FirebaseFirestore db;
    boolean isPending;

    public AdminJobAdapter(Context context, List<Job> jobList, boolean isPending) {
        this.context = context;
        this.jobList = jobList;
        this.db = FirebaseFirestore.getInstance();
        this.isPending = isPending;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());
        holder.tvCompany.setText(job.getCompany());
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText("₹ " + job.getSalary());

        if (isPending) {
            holder.btnToggle.setText("✅ Approve");
            holder.btnToggle.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF2E7D32));
            holder.btnToggle.setOnClickListener(v -> {
                db.collection("jobs").document(job.getJobId())
                        .update("approvalStatus", "approved",
                                "active", true)
                        .addOnSuccessListener(unused -> {
                            sendNotification(job.getEmployerId(),
                                    "✅ Job Approved!",
                                    "Your job '" + job.getTitle() +
                                            "' has been approved!",
                                    "job");
                            jobList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Job Approved!",
                                    Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            holder.btnToggle.setText(job.isActive()
                    ? "Deactivate" : "Activate");
            holder.btnToggle.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            job.isActive() ? 0xFFE65100 : 0xFF2E7D32));
            holder.btnToggle.setOnClickListener(v -> {
                boolean newStatus = !job.isActive();
                db.collection("jobs").document(job.getJobId())
                        .update("active", newStatus)
                        .addOnSuccessListener(unused -> {
                            job.setActive(newStatus);
                            notifyItemChanged(position);
                            Toast.makeText(context,
                                    newStatus ? "Activated!" : "Deactivated!",
                                    Toast.LENGTH_SHORT).show();
                        });
            });
        }

        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Delete Job")
                    .setMessage("क्या आप यह job delete करना चाहते हैं?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("jobs").document(job.getJobId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    jobList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Job deleted!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void sendNotification(String userId, String title,
                                  String message, String type) {
        String notifId = db.collection("notifications").document().getId();
        Notification notification = new Notification(
                notifId, userId, title, message, type);
        db.collection("notifications")
                .document(notifId)
                .set(notification);
    }

    @Override
    public int getItemCount() { return jobList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvLocation, tvSalary;
        Button btnDelete, btnToggle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnToggle = itemView.findViewById(R.id.btnToggle);
        }
    }
}