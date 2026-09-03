package com.jobportal.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.Notification;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends
        RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    Context context;
    List<Notification> notificationList;

    public NotificationAdapter(Context context,
                               List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(
                new Date(notification.getTimestamp())));

        switch (notification.getType() != null
                ? notification.getType() : "") {
            case "application":
                holder.tvIcon.setText("📋");
                holder.cardIcon.setCardBackgroundColor(0xFFE3F2FD);
                break;
            case "shortlisted":
                holder.tvIcon.setText("⭐");
                holder.cardIcon.setCardBackgroundColor(0xFFE8F5E9);
                break;
            case "rejected":
                holder.tvIcon.setText("❌");
                holder.cardIcon.setCardBackgroundColor(0xFFFFEBEE);
                break;
            case "job":
                holder.tvIcon.setText("💼");
                holder.cardIcon.setCardBackgroundColor(0xFFFFF3E0);
                break;
            case "chat":
                holder.tvIcon.setText("💬");
                holder.cardIcon.setCardBackgroundColor(0xFFF3E5F5);
                break;
            case "interview":
                holder.tvIcon.setText("📅");
                holder.cardIcon.setCardBackgroundColor(0xFFE8EAF6);
                break;
            default:
                holder.tvIcon.setText("🔔");
                holder.cardIcon.setCardBackgroundColor(0xFFE3F2FD);
        }

        if (!notification.isRead()) {
            holder.cardView.setCardBackgroundColor(0xFFE8F4FD);
            holder.tvUnread.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF);
            holder.tvUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                FirebaseFirestore.getInstance()
                        .collection("notifications")
                        .document(notification.getNotificationId())
                        .update("read", true);
                notification.setRead(true);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() { return notificationList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime, tvIcon, tvUnread;
        CardView cardView, cardIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvUnread = itemView.findViewById(R.id.tvUnread);
            cardView = itemView.findViewById(R.id.cardView);
            cardIcon = itemView.findViewById(R.id.cardIcon);
        }
    }
}