package com.jobportal.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.activities.ChatActivity;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatListAdapter extends
        RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    Context context;
    List<Map<String, Object>> chatList;
    String currentUserId;
    FirebaseFirestore db;

    public ChatListAdapter(Context context,
                           List<Map<String, Object>> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> chat = chatList.get(position);

        String user1 = (String) chat.get("user1");
        String user2 = (String) chat.get("user2");
        String user1Name = (String) chat.get("user1Name");
        String user2Name = (String) chat.get("user2Name");

        String receiverId = currentUserId.equals(user1) ? user2 : user1;
        String receiverName = currentUserId.equals(user1)
                ? user2Name : user1Name;

        holder.tvName.setText(receiverName != null ? receiverName : "Unknown");

        String lastMessage = (String) chat.get("lastMessage");
        holder.tvLastMessage.setText(
                lastMessage != null ? lastMessage : "No messages yet");

        Long timestamp = (Long) chat.get("lastMessageTime");
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(timestamp)));
        }

        // Unread notifications check करो
        loadUnreadCount(holder, receiverId);

        holder.itemView.setOnClickListener(v -> {
            // Chat खुलने पर notifications read करो
            markNotificationsRead();

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", receiverId);
            intent.putExtra("receiverName", receiverName);
            context.startActivity(intent);
        });
    }

    private void loadUnreadCount(ViewHolder holder, String senderId) {
        // सिर्फ उस specific sender की unread notifications check करो
        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("type", "chat")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc
                            : snapshots) {
                        String title = doc.getString("title");
                        // Title में sender का name होता है
                        // सिर्फ उसी sender की count करो
                        if (title != null) {
                            // Sender का name Firestore से लो
                            db.collection("users").document(senderId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String senderName =
                                                userDoc.getString("name");
                                        if (senderName != null &&
                                                title.contains(senderName)) {
                                            // इस sender के unread messages हैं
                                            holder.viewUnreadDot.setVisibility(
                                                    View.VISIBLE);
                                            holder.tvUnreadCount.setVisibility(
                                                    View.VISIBLE);
                                            holder.tvLastMessage.setTextColor(
                                                    0xFF212121);
                                        }
                                    });
                        }
                    }

                    if (count == 0) {
                        holder.viewUnreadDot.setVisibility(View.GONE);
                        holder.tvUnreadCount.setVisibility(View.GONE);
                        holder.tvLastMessage.setTextColor(0xFF757575);
                    }
                });
    }

    private void markNotificationsRead() {
        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("type", "chat")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc
                            : snapshots) {
                        doc.getReference().update("read", true);
                    }
                });
    }

    public void sortByRecentMessage() {
        Collections.sort(chatList, (a, b) -> {
            Long timeA = (Long) a.get("lastMessageTime");
            Long timeB = (Long) b.get("lastMessageTime");
            if (timeA == null) timeA = 0L;
            if (timeB == null) timeB = 0L;
            return Long.compare(timeB, timeA);
        });
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, tvUnreadCount;
        View viewUnreadDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}