package com.company.shared.payloads;

public class RegisterPayload implements java.io.Serializable {
    public String username;
    public String password;
    public String repeatedPassword;

    public RegisterPayload(String username, String password, String repeatedPassword) {
        this.username = username;
        this.password = password;
        this.repeatedPassword = repeatedPassword;
    }
}
