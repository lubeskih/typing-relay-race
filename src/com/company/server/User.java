package com.company.server;

public class User {
    public String username;
    public String hash;
    public Team team = null;

    public User(String username, String password) {
        this.username = username;
        this.hash = hash(password);
    }

    private String hash(String password) {
        return password;
    }
}

