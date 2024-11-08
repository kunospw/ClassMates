package com.example.classmatesv2;

public class Message {
    private String text;
    private boolean isUser;
    private String timestamp;

    // Required empty constructor for Firebase
    public Message() {
    }

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}