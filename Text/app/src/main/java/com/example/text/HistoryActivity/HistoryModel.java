package com.example.text.HistoryActivity;

public class HistoryModel {
    private boolean chatRoomIdTrue;
    private String lastMessage;
    private String chatRoomId;
    private long timestamp;
    private String title;
    private String userUid;
    // No-argument constructor required for Firebase or other serialization libraries
    public HistoryModel() {
    }
    // Getters and Setters

    public boolean isChatRoomIdTrue() {
        return chatRoomIdTrue;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public void setChatRoomIdTrue(boolean chatRoomIdTrue) {
        this.chatRoomIdTrue = chatRoomIdTrue;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

}
