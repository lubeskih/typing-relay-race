package com.company.server;

import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class Store {
    public ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, LoggedInUser> loggedInUsers = new ConcurrentHashMap<>();

    public Store() {}

    public synchronized void registerUser(String username, String password) throws Exception {
        User user = new User(username, password);

        if (this.registeredUsers.containsKey(username)) {
            throw new Exception("User already registered, choose a different name!");
        } else {
            this.registeredUsers.put(username, user);
        }
    }

    public synchronized void loginUser(User user, ObjectOutputStream address, String sessionToken) {
        LoggedInUser loggedInUser = new LoggedInUser(user.username, user.hash, address);

        this.loggedInUsers.put(sessionToken, loggedInUser);
    }
}
