package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.MyFirebaseMessagingService;
import com.jobportal.app.R;
import com.jobportal.app.adapters.MessageAdapter;
import com.jobportal.app.models.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends BaseActivity {

    RecyclerView rvMessages;
    EditText etMessage;
    LinearLayout btnSend;
    TextView tvReceiverName;
    MessageAdapter messageAdapter;
    List<Message> messageList;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    ListenerRegistration listenerRegistration;
    String receiverId, receiverName, chatRoomId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        applyWindowInsets(findViewById(R.id.rootLayout));


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");
        currentUserId = mAuth.getCurrentUser().getUid();

        chatRoomId = currentUserId.compareTo(receiverId) < 0
                ? currentUserId + "_" + receiverId
                : receiverId + "_" + currentUserId;

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvReceiverName = findViewById(R.id.tvReceiverName);

        tvReceiverName.setText(receiverName);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);

        listenForMessages();
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void listenForMessages() {
        // Real-time listener — हर नए message पर automatically update होगा
        listenerRegistration = db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    messageList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Message message = doc.toObject(Message.class);
                        messageList.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");
        String messageId = db.collection("chats").document().getId();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    String senderName = doc.getString("name");

                    Message message = new Message(messageId, currentUserId,
                            receiverId, senderName, text);

                    db.collection("chats")
                            .document(chatRoomId)
                            .collection("messages")
                            .document(messageId)
                            .set(message)
                            .addOnSuccessListener(unused -> {
                                // Chat room update करो
                                Map<String, Object> chatRoom = new HashMap<>();
                                chatRoom.put("lastMessage", text);
                                chatRoom.put("lastMessageTime",
                                        System.currentTimeMillis());
                                chatRoom.put("user1", currentUserId);
                                chatRoom.put("user2", receiverId);
                                chatRoom.put("user1Name", senderName);
                                chatRoom.put("user2Name", receiverName);
                                db.collection("chats")
                                        .document(chatRoomId)
                                        .set(chatRoom);

                                // Receiver को notification भेजो
                                sendChatNotification(senderName, text);
                            });
                });
    }

    private void sendChatNotification(String senderName, String message) {
        // Firestore में notification save करो receiver के लिए
        String notifId = db.collection("notifications").document().getId();
        com.jobportal.app.models.Notification notification =
                new com.jobportal.app.models.Notification(
                        notifId, receiverId,
                        "💬 " + senderName,
                        message,
                        "chat");
        db.collection("notifications")
                .document(notifId)
                .set(notification);

        // Local notification — sender को confirm दिखाओ
        MyFirebaseMessagingService.sendLocalNotification(
                this, "✅ Message Sent!", "Message delivered to " + receiverName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Chat खुलने पर उस receiver की notifications read करो
        markChatNotificationsAsRead();
    }

    private void markChatNotificationsAsRead() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("type", "chat")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc
                            : snapshots) {
                        // सिर्फ उस receiver की notifications read करो
                        String notifMessage = doc.getString("message");
                        if (notifMessage != null) {
                            doc.getReference().update("read", true);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}