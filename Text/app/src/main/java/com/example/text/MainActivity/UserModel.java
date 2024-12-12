package com.example.text.MainActivity;

public class UserModel {
    private String uid;
    private String username;
    private String email;

    // Default constructor required for calls to DataSnapshot.getValue(AppUser.class)
    public UserModel() {
    }

    // Constructor with all fields
    public UserModel(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
