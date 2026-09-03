package com.jobportal.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.jobportal.app.R;
import com.jobportal.app.models.Application;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    Context context;
    List<Application> applicationList;

    public TimelineAdapter(Context context, List<Application> applicationList) {
        this.context = context;
        this.applicationList = applicationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application app = applicationList.get(position);

        holder.tvJobTitle.setText(app.getJobTitle());

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault());
        holder.tvAppliedDate.setText(
                "Applied: " + sdf.format(new Date(app.getAppliedDate())));

        String status = app.getStatus().toLowerCase();

        // Step 1 — Applied
        setStep(holder.step1, holder.tvStep1, holder.line1,
                "✅", "#2E7D32", true);

        // Step 2 — Under Review
        boolean step2Done = !status.equals("pending");
        setStep(holder.step2, holder.tvStep2, holder.line2,
                step2Done ? "✅" : "⏳",
                step2Done ? "#2E7D32" : "#FFA000", step2Done);

        // Step 3 — Shortlisted / Rejected
        boolean step3Done = status.equals("shortlisted")
                || status.equals("rejected");
        boolean isRejected = status.equals("rejected");
        setStep(holder.step3, holder.tvStep3, holder.line3,
                isRejected ? "❌" : (step3Done ? "⭐" : "⏳"),
                isRejected ? "#B71C1C" : (step3Done ? "#1565C0" : "#BDBDBD"),
                step3Done);

        // Step 4 — Interview (future feature)
        setStep(holder.step4, holder.tvStep4, null,
                "⏳", "#BDBDBD", false);

        // Status Badge
        holder.tvStatus.setText(app.getStatus().toUpperCase());
        switch (status) {
            case "pending":
                holder.tvStatus.setBackgroundColor(0xFFFFA000);
                break;
            case "shortlisted":
                holder.tvStatus.setBackgroundColor(0xFF1565C0);
                break;
            case "rejected":
                holder.tvStatus.setBackgroundColor(0xFFB71C1C);
                break;
            default:
                holder.tvStatus.setBackgroundColor(0xFF2E7D32);
        }
    }

    private void setStep(TextView stepView, TextView labelView,
                         View lineView, String icon, String color, boolean active) {
        stepView.setText(icon);
        if (active) {
            stepView.setAlpha(1f);
            labelView.setAlpha(1f);
        } else {
            stepView.setAlpha(0.4f);
            labelView.setAlpha(0.4f);
        }
        if (lineView != null) {
            lineView.setAlpha(active ? 1f : 0.3f);
        }
    }

    @Override
    public int getItemCount() { return applicationList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvAppliedDate, tvStatus;
        TextView step1, step2, step3, step4;
        TextView tvStep1, tvStep2, tvStep3, tvStep4;
        View line1, line2, line3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            step1 = itemView.findViewById(R.id.step1);
            step2 = itemView.findViewById(R.id.step2);
            step3 = itemView.findViewById(R.id.step3);
            step4 = itemView.findViewById(R.id.step4);
            tvStep1 = itemView.findViewById(R.id.tvStep1);
            tvStep2 = itemView.findViewById(R.id.tvStep2);
            tvStep3 = itemView.findViewById(R.id.tvStep3);
            tvStep4 = itemView.findViewById(R.id.tvStep4);
            line1 = itemView.findViewById(R.id.line1);
            line2 = itemView.findViewById(R.id.line2);
            line3 = itemView.findViewById(R.id.line3);
        }
    }
}