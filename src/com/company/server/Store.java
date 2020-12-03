package com.company.server;

import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class Store {
    public ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, LoggedInUser> loggedInUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<>();

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

    public synchronized void createTeam(String teamname, LoggedInUser memberOne) throws Exception {
        if (this.teams.containsKey(teamname)) {
            throw new Exception("There is already a team with that name. Choose a different name.");
        }

        if (this.teams.values()
                .stream()
                .filter(t -> t.memberOne.equals(memberOne) || t.memberTwo.equals(memberOne))
                .findAny()
                .isPresent()
        ) {
            throw new Exception("You are already in the team. Leave that team to create a new one.");
        }

        Team team = new Team(teamname, memberOne);
        this.teams.put(teamname, team);
    }
}
