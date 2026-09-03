package com.jobportal.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.jobportal.app.R;
import java.util.List;
import java.util.Map;

public class InterviewAdapter extends
        RecyclerView.Adapter<InterviewAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> interviewList;

    public InterviewAdapter(Context context,
                            List<Map<String, Object>> interviewList) {
        this.context = context;
        this.interviewList = interviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_interview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> interview = interviewList.get(position);

        holder.tvJobTitle.setText(
                interview.get("jobTitle") != null
                        ? (String) interview.get("jobTitle") : "N/A");
        holder.tvType.setText(
                interview.get("type") != null
                        ? (String) interview.get("type") : "N/A");
        holder.tvDate.setText(
                interview.get("date") != null
                        ? (String) interview.get("date") : "N/A");
        holder.tvTime.setText(
                interview.get("time") != null
                        ? (String) interview.get("time") : "N/A");
        holder.tvLocation.setText(
                interview.get("location") != null
                        ? (String) interview.get("location") : "N/A");
        holder.tvNotes.setText(
                interview.get("notes") != null
                        ? (String) interview.get("notes") : "No notes");

        String status = interview.get("status") != null
                ? (String) interview.get("status") : "scheduled";
        holder.tvStatus.setText(status.toUpperCase());
    }

    @Override
    public int getItemCount() { return interviewList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvType, tvDate, tvTime,
                tvLocation, tvNotes, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}