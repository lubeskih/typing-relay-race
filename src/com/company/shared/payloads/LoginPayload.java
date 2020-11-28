package com.company.shared.payloads;

public class LoginPayload implements java.io.Serializable {
    public String username;
    public String password;

    public LoginPayload(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
