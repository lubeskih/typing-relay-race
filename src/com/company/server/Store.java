package com.company.server;

import com.company.server.types.*;
import com.company.shared.Message;
import com.company.shared.payloads.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Store {
    public ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, LoggedInUser> loggedInUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, BlockingQueue<InternalMessage>> gameCoordinationBQs = new ConcurrentHashMap<>();
    public Scoreboard scoreboard = new Scoreboard();

    public Store() {}

    public synchronized Message registerUser(String username, String password) {
        User user = new User(username, password);
        Message m;

        if (this.registeredUsers.containsKey(username)) {
            ConflictPayload cp = new ConflictPayload("User already registered, choose a different name!");
            m = new Message(true, 200, true, cp);
        } else {
            this.registeredUsers.put(username, user);
            SuccessPayload sp = new SuccessPayload("Registered successfully.");
            m = new Message(true, 100, false, sp);
        }

        return m;
    }

    public synchronized Message loginUser(String username, String password, String sessionToken) {
        Message m;
        User user;

        // check if not registered
        if (!registeredUsers.containsKey(username)) {
            ConflictPayload cp = new ConflictPayload("You are not registered.");
            m = new Message(true, 240, true, cp);
            return m;
        } else {
            user = registeredUsers.get(username);
        }

        // check if already logged in
        // TODO should also check if its logged in from different client (double login)
        if (sessionToken != null) {
            if (loggedInUsers.containsKey(sessionToken)) {
                ConflictPayload cp = new ConflictPayload("You are already logged in.");
                m = new Message(true, 230, true, cp);
                return m;
            }
        }

        // Generate new token
        String newSessionToken = generateNewToken();

        // log in the user
        LoggedInUser loggedInuser = new LoggedInUser(user.username, user.hash, null);
        loggedInUsers.put(newSessionToken, loggedInuser);

        // reply
        SessionPayload sp = new SessionPayload(newSessionToken);
        m = new Message(true, 100, false, sp);

        return m;
    }

    public Message viewScoreboard() {
        Message m;

        Base64.Encoder encoder = Base64.getEncoder();
        String b64score;

        if (scoreboard.scoreboard.size() == 0) {
            InfoPayload ip = new InfoPayload("Scoreboard empty! No one has played, yet.");
            m = new Message(true, 110, false, ip);

            return m;
        }

        String score = "SCOREBOARD\n";
        score += "==========================\n";

        for (Score s:scoreboard.scoreboard) {
            score += s.teamname + "\t" + s.totalScoreInSeconds + "s. (" + s.totalScoreInMinutes + "m)\n";
        }

        b64score = encoder.encodeToString(score.getBytes());

        // send message
        ScoreboardPayload sp = new ScoreboardPayload(b64score);
        m = new Message(true, 100, false, sp);
        return m;
    }

    public synchronized Message createTeam(String teamname, boolean openToAll, String sessionToken) {
        Message m;

        if (this.teams.containsKey(teamname)) {
            ConflictPayload cp = new ConflictPayload("There is already a team with that name. Choose a different name.");
            m = new Message(true, 230, true, cp);
            return m;
        }

        LoggedInUser admin = loggedInUsers.get(sessionToken);

        if (teams.values()
                .stream()
                .filter(t -> t.admin.equals(admin) || (t.memberTwo != null && t.memberTwo.equals(admin))) // || t.memberTwo.equals(admin)
                .findAny()
                .isPresent()
        ) {
            ConflictPayload cp = new ConflictPayload("You are already in a team.");
            m = new Message(true, 230, true, cp);
            return m;
        }

        Team team;

        if (openToAll) {
            team = new Team(teamname, admin, null);
            SuccessPayload sp = new SuccessPayload("Team " + teamname.toUpperCase() + " created successfully. Members 1/2. Open to all!");
            m = new Message(true, 100, false, sp);
        } else {
            String password = generateTeamPassword();
            team = new Team(teamname, admin, password);
            SuccessPayload sp = new SuccessPayload("Team " + teamname.toUpperCase() + " created successfully. Members 1/2. Password for joining is: " + password);
            m = new Message(true, 100, false, sp);
        }

        admin.team = teams.get(teamname);

        teams.put(teamname, team);
        return m;
    }

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////

    public boolean teamExists(String teamname) {
        return teams.containsKey(teamname);
    }

    // private static final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Shamelessly stolen from https://www.baeldung.com/java-random-string :)))))
     */
    public String generateTeamPassword() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString.toUpperCase();
    }

    public boolean isAuthenticated(String sessionToken) {
        return sessionToken != null && this.loggedInUsers.containsKey(sessionToken);
    }

    public Message notAuthenticatedMessage() {
        ForbiddenPayload fp = new ForbiddenPayload("You are not authenticated. Log in first.");
        Message m = new Message(true, 220, true, fp);

        return m;
    }

    public boolean isInGame(String sessionToken) {
        LoggedInUser user = loggedInUsers.get(sessionToken);

        return user.team != null && gameCoordinationBQs.containsKey(user.team.teamname);
    }

    public LoggedInUser getUser(String sessionToken) {
        return loggedInUsers.get(sessionToken);
    }

}
