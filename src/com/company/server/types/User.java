package com.company.server.types;

import com.company.server.Security;

public class User {
    public String username;
    public String hash;
    public byte[] salt;
    public Team team = null;

    public User(String username, String password){
        this.username = username;
        this.hash = hash(password);
    }

    private String hash(String password) {
        Security s = new Security();

        byte[] salt = s.getSalt();
        this.salt = salt;

        String hash = s.getHash(password, salt);

        return hash;
    }
}

