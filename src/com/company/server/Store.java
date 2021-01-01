package com.company.server;

import com.company.server.types.*;
import com.company.shared.Message;
import com.company.shared.payloads.*;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Store {
    public ConcurrentHashMap<String, User> registeredUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, LoggedInUser> loggedInUsers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, BlockingQueue<Message>> gameCoordinationBQs = new ConcurrentHashMap<>();
    public Scoreboard scoreboard = new Scoreboard();
    public String[] words = {"street", "inch", "lot", "nothing", "course", "stay", "wheel",
            "full", "force", "blue", "object", "decide", "surface", "deep", "moon", "island",
            "foot", "yet", "busy", "test", "record", "boat", "common", "gold", "possible", "plane",
            "age", "dry", "wonder", "laugh", "thousand", "ago", "ran", "check", "game", "shape",
            "yes", "hot", "miss", "brought", "heat", "snow", "bed", "bring", "sit", "perhaps",
            "fill", "east", "weight", "language", "among"};

    public Store() {}

    public String generateRandomWords() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        int limit = 80;
        while(limit > 0) {
            String word = words[random.nextInt(words.length)];
            limit = limit - word.length() - 1;

            sb.append(word + " ");
        }

        if (sb.length() > 80) {
            sb.delete(77, sb.length());
            sb.append("...");
        }

        if (sb.toString().endsWith(" ")) {
            sb.replace(80, 80, ".");
        }

        return sb.toString();
    }

    public synchronized Message registerUser(String username, String password) {
        Message m;
        User user = new User(username, password);

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

    public synchronized Message loginUser(String username,
                                          String password,
                                          String sessionToken,
                                          ObjectOutputStream address) {
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

        // check if password is correct
        Security sec = new Security();

        if (!sec.correctPassword(password, user.salt, user.hash)) {
            ForbiddenPayload fp = new ForbiddenPayload("Invalid credentials.");
            m = new Message(true, 220, true, fp);
            return m;
        }

        // Generate new token
        String newSessionToken = generateNewToken();

        // log in the user
        LoggedInUser loggedInuser = new LoggedInUser(user.username, user.hash, address, newSessionToken);
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

        StringBuilder s = new StringBuilder();
        s.append(String.format("%-20s%-20s%-20s\n", "Rank", "Team Name", "Total Score (s)"));
        s.append(String.format("===================================================\n"));
        int i = 1;

        for (Score score:scoreboard.scoreboard) {
            s.append(String.format("%-20s", i));
            s.append(String.format("%-20s", score.teamname));
            s.append(String.format("%-20s", score.totalScoreInSeconds + "s. (" + score.totalScoreInMinutes + "m.)"));
            s.append(String.format("\n"));

            i++;
        }

        b64score = encoder.encodeToString(s.toString().getBytes());

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
                .filter(t -> t.admin.equals(admin) || (t.memberTwo != null && t.memberTwo.equals(admin)))
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

        teams.put(teamname, team);
        admin.team = team;

        return m;
    }

    public synchronized Message joinTeam(String teamname, String password, String sessionToken) {
        Message m;
        Team team = teams.get(teamname);

        // check if team exists
        if (!teamExists(teamname)) {
            NotFoundPayload nfp = new NotFoundPayload("Team does not exists.");
            m = new Message(true, 240, true, nfp);
            return m;
        }

        // check if team requires passcode
        if (!team.visible) {
            if (!team.password.equals(password)) {
                ForbiddenPayload fp = new ForbiddenPayload("Invalid passcode.");
                m = new Message(true, 220, true, fp);
                return m;
            }
        }

        LoggedInUser member = loggedInUsers.get(sessionToken);

        if (team.admin.username.equals(member.username)) {
            ConflictPayload cp = new ConflictPayload("You cannot join your own team. Already joined.");
            m = new Message(true, 230, true, cp);
            return m;
        }

        // check if team is full
        if (teams.get(teamname).memberTwo != null) {
            ConflictPayload cp = new ConflictPayload("Someone joined before you! Sorry.");
            m = new Message(true, 230, true, cp);
            return m;
        } else {
            team.memberTwo = member;
            member.team = team;
        }

        SuccessPayload sp = new SuccessPayload("You joined the team " + teamname.toUpperCase() + " whose owner is " + team.admin.username.toUpperCase());
        m = new Message(true, 100, false, sp);

        spawnGameCoordinator(teamname);

        return m;
    }

    public synchronized void spawnGameCoordinator(String teamname) {
        BlockingQueue<Message> gameCoordinatorBQ = new LinkedBlockingQueue<>();
        gameCoordinationBQs.put(teamname, gameCoordinatorBQ);
        GameCoordinator gameCoordinator = new GameCoordinator(teamname, this);

        Thread gct = new Thread(gameCoordinator);
        gct.start();

        System.out.println("Game coordinator for team " + teamname + " spawned!");
    }

    public Message listTeams() {
        Message m;

        Base64.Encoder encoder = Base64.getEncoder();
        String b64score;

        if (teams.size() == 0) {
            InfoPayload ip = new InfoPayload("There are no teams created. You can be the first. :)");
            m = new Message(true, 110, false, ip);

            return m;
        }

        StringBuilder s = new StringBuilder();
        s.append(String.format("%-20s%-20s%-20s%-20s\n","Team Name","Created By","Requires Password","Players"));
        s.append(String.format("===================================================================\n"));

        for (Map.Entry<String, Team> entry : teams.entrySet()) {
            String key = entry.getKey();
            Team value = entry.getValue();

            try {
                String requiresPassword = value.visible ? "No" : "Yes";

                // List only teams that are not in-game
                if (!value.teamInGame) {
                    s.append(String.format("%-20s", key));
                    s.append(String.format("%-20s", value.admin.username));
                    s.append(String.format("%-20s", requiresPassword));
                    s.append(String.format("%-20s", "1/2"));
                    s.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(s.toString());
        b64score = encoder.encodeToString(s.toString().getBytes());

        // send message
        TeamsPayload tp = new TeamsPayload(b64score);
        m = new Message(true, 100, false, tp);
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
        if (sessionToken != null) {
            LoggedInUser user = loggedInUsers.get(sessionToken);

            return user.team != null && gameCoordinationBQs.containsKey(user.team.teamname);
        } else {
            return false;
        }
    }

    public LoggedInUser getUser(String sessionToken) {
        return loggedInUsers.get(sessionToken);
    }
}
