package com.jobportal.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.Application;
import com.jobportal.app.models.Notification;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends
        RecyclerView.Adapter<ApplicationAdapter.AppViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Application app);
    }

    Context context;
    List<Application> applicationList;
    FirebaseFirestore db;
    OnItemClickListener listener;

    public ApplicationAdapter(Context context,
                              List<Application> applicationList) {
        this.context = context;
        this.applicationList = applicationList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                            int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_application, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        Application app = applicationList.get(position);
        holder.tvJobTitle.setText(app.getJobTitle());
        holder.tvSeekerName.setText(app.getSeekerName());
        holder.tvSeekerEmail.setText(app.getSeekerEmail());

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
            default:
                holder.tvStatus.setBackgroundColor(0xFF1565C0);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault());
        holder.tvDate.setText("Applied: " +
                sdf.format(new Date(app.getAppliedDate())));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(app);
        });

        holder.btnWithdraw.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Withdraw Application")
                    .setMessage("क्या आप यह application withdraw करना चाहते हैं?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("applications")
                                .document(app.getApplicationId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    applicationList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context,
                                            "Application withdrawn!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
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
    public int getItemCount() { return applicationList.size(); }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvSeekerName, tvSeekerEmail, tvStatus, tvDate;
        Button btnWithdraw;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSeekerName = itemView.findViewById(R.id.tvSeekerName);
            tvSeekerEmail = itemView.findViewById(R.id.tvSeekerEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnWithdraw = itemView.findViewById(R.id.btnWithdraw);
        }
    }
}