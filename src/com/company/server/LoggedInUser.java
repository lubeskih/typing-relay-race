package com.company.server;

import java.io.ObjectOutputStream;

public class LoggedInUser extends User {
    public ObjectOutputStream address;

    public LoggedInUser(String username, String hash, ObjectOutputStream address) {
        super(username, hash);
        this.address = address;
    }
}
