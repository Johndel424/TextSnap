package com.example.text;

public class ConvoMessage {
    private String sender;      // Sender of the message
    private String message;     // Message content
    private String userType;    // Type of user (e.g., "ai")
    private String messageId;   // Unique ID for the message
    private long timestamp;      // Timestamp of when the message was sent

    // Default constructor required for calls to DataSnapshot.getValue(ConvoMessage.class)
    public ConvoMessage() {
    }

    public ConvoMessage(String sender, String message, String messageId, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

