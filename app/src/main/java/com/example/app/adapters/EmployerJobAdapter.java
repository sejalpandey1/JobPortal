package com.jobportal.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.activities.EditJobActivity;
import com.jobportal.app.activities.ViewApplicantsActivity;
import com.jobportal.app.models.Job;
import com.jobportal.app.utils.Constants;
import java.util.List;

public class EmployerJobAdapter extends
        RecyclerView.Adapter<EmployerJobAdapter.ViewHolder> {

    Context context;
    List<Job> jobList;

    public EmployerJobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_employer_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());
        holder.tvCompany.setText(job.getCompany());
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText("₹ " + job.getSalary());
        holder.tvJobType.setText(job.getJobType());

        String approval = job.getApprovalStatus();
        if (approval == null || approval.equals("pending")) {
            holder.tvApprovalStatus.setText("⏳ Pending Approval");
            holder.tvApprovalStatus.setTextColor(0xFFE65100);
        } else if (approval.equals("approved")) {
            holder.tvApprovalStatus.setText("✅ Approved");
            holder.tvApprovalStatus.setTextColor(0xFF2E7D32);
        } else {
            holder.tvApprovalStatus.setText("❌ Rejected");
            holder.tvApprovalStatus.setTextColor(0xFFB71C1C);
        }

        FirebaseFirestore.getInstance()
                .collection("applications")
                .whereEqualTo("jobId", job.getJobId())
                .get()
                .addOnSuccessListener(snap ->
                        holder.tvApplicantCount.setText(
                                snap.size() + " Applicants"));

        holder.btnViewApplicants.setOnClickListener(v -> {
            Intent intent = new Intent(context,
                    ViewApplicantsActivity.class);
            intent.putExtra(Constants.KEY_JOB_ID, job.getJobId());
            intent.putExtra("jobTitle", job.getTitle());
            context.startActivity(intent);
        });

        holder.btnEditJob.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditJobActivity.class);
            intent.putExtra(Constants.KEY_JOB_ID, job.getJobId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return jobList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvLocation, tvSalary,
                tvJobType, tvApprovalStatus, tvApplicantCount;
        Button btnViewApplicants, btnEditJob;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvApprovalStatus = itemView.findViewById(R.id.tvApprovalStatus);
            tvApplicantCount = itemView.findViewById(R.id.tvApplicantCount);
            btnViewApplicants = itemView.findViewById(R.id.btnViewApplicants);
            btnEditJob = itemView.findViewById(R.id.btnEditJob);
        }
    }
}