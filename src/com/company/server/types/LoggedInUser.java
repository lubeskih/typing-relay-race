package com.company.server.types;

import java.io.ObjectOutputStream;

public class LoggedInUser extends User {
    public ObjectOutputStream address;
    public String sessionToken;

    public LoggedInUser(String username, String hash, ObjectOutputStream address,
                        String sessionToken) {
        super(username, hash);
        this.address = address;
        this.sessionToken = sessionToken;
    }
}
