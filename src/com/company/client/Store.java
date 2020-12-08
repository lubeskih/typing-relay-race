package com.company.client;

public class Store {
    private String sessionToken = null;
    public boolean nextMessageIsASentence = false;

    public Store() {}

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
