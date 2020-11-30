package com.company.shared;

public class Message implements java.io.Serializable {
    public boolean isResponse;
    public int reply;
    public boolean isError;
    public Object payload;
    private String sessionToken = null;

    public Message (boolean isResponse, int reply, boolean isError, Object payload) {
        this.isResponse = isResponse;
        this.reply = reply;
        this.isError = isError;
        this.payload = payload;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}