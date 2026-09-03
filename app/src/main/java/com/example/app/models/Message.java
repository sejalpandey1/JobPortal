package com.jobportal.app.models;

public class Message {

    private String messageId;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String message;
    private long timestamp;
    private boolean isRead;

    public Message() {}

    public Message(String messageId, String senderId, String receiverId,
                   String senderName, String message) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}