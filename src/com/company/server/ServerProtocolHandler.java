package com.company.server;

import com.company.shared.Message;
import com.company.shared.ProtocolDictionary;
import com.company.shared.payloads.LoginPayload;
import com.company.shared.payloads.RegisterPayload;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerProtocolHandler extends ProtocolDictionary {
    private Store store;
    private BlockingQueue<InternalMessage> OutboundMessageBQ;

    private static final SecureRandom secureRandom = new SecureRandom();
//    private static final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public ServerProtocolHandler(Store store, BlockingQueue<InternalMessage> OutboundMessageBQ) {
        super();
        this.store = store;
        this.OutboundMessageBQ = OutboundMessageBQ;
    }

    public synchronized InternalMessage process (InternalMessage im) {
        int reply = im.message.reply;

        switch(reply) {
            case 300: return P300(im);
            case 310: return P310(im);
            case 320: return P320(im);
            case 330: return P330(im);
            case 370: return P370(im);
            case 340: return P340(im);
            case 350: return P350(im);
            default: return PDEFAULT(im);
        }
    }

    private InternalMessage P350(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;
        String payload;

        try {
            this.authenticate(im.message.getSessionToken());
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        payload = this.store.teams.toString();
        replyMessage = new Message(true, 100, false, payload);
        reply = new InternalMessage(replyMessage, im.address);
        return reply;
    }

    private InternalMessage P340(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;
        String payload;

        try {
            this.authenticate(im.message.getSessionToken());
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        String teamname = (String) im.message.payload;

        if (!(store.teamExists(teamname))) {
            payload = "Team does not exists.";
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        LoggedInUser member = this.store.loggedInUsers.get(im.message.getSessionToken());

        try {
            this.store.teams.get(teamname).memberTwo = member;
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        payload = "Successfully joined the team " + teamname.toUpperCase() + " whose owner is " + this.store.teams.get(teamname).memberOne.username.toUpperCase();
        replyMessage = new Message(true, 100, false, payload);
        reply = new InternalMessage(replyMessage, im.address);

        member.team = store.teams.get(teamname);

        try {
            BlockingQueue gcbq = new LinkedBlockingQueue<>();
            this.store.gameCoordinationBQs.put(teamname, gcbq);
            GameCoordinator gc = new GameCoordinator(teamname, store, gcbq, this.OutboundMessageBQ);

            Thread gct = new Thread(gc);
            gct.start();

            System.out.println("Game coordinator for team " + teamname + " created!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return reply;
    }

    private synchronized InternalMessage P330(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;
        String payload;

        try {
            this.authenticate(im.message.getSessionToken());
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        String teamname = (String) im.message.payload;
        LoggedInUser memberOne = this.store.loggedInUsers.get(im.message.getSessionToken());
        try {
            this.store.createTeam(teamname, memberOne);
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        System.out.println(this.store.teams.toString());

        memberOne.team = store.teams.get(teamname);

        payload = "Team created!";
        replyMessage = new Message(true, 100, false, payload);
        reply = new InternalMessage(replyMessage, im.address);
        return reply;
    }

    private InternalMessage P370(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;
        String payload;

        try {
            this.authenticate(im.message.getSessionToken());
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        store.loggedInUsers.remove(im.message.getSessionToken());

        payload = "Logged out successfully.";
        replyMessage = new Message(true, 100, false, payload);
        reply = new InternalMessage(replyMessage, im.address);
        return reply;
    }

    private synchronized InternalMessage P300(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;

        if (!(im.message.payload instanceof LoginPayload)) {
            String payload = "Wrong payload while trying to login!";
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        String username = ((LoginPayload) im.message.payload).username;

        User user;
        // check if not registered
        if (!store.registeredUsers.containsKey(username)) {
            String payload = "You are not registered.";
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        // check if already logged in
        // TODO should also check if its logged in from different client (double login)
        boolean sessionTokenExists = im.message.getSessionToken() != null;
        if (sessionTokenExists) {
            if (store.loggedInUsers.containsKey(im.message.getSessionToken()) ) {
                String payload = "You are already logged in.";
                replyMessage = new Message(true, 210, true, payload);
                reply = new InternalMessage(replyMessage, im.address);
                return reply;
            }
        }

        // if registered, use the already existing User object
        user = store.registeredUsers.get(username);

        // check if password is corresponding to hash
        // TODO

        String newSessionToken = generateNewToken();
        store.loginUser(user, im.address, newSessionToken);

        String payload = newSessionToken;
        replyMessage = new Message(true, 300, false, payload);
        reply = new InternalMessage(replyMessage, im.address);

        return reply;
    }

    private synchronized InternalMessage P310(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;

        if (!(im.message.payload instanceof RegisterPayload)) {
            String payload = "Wrong payload while trying to register!";
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        String username = ((RegisterPayload) im.message.payload).username;
        String password = ((RegisterPayload) im.message.payload).password;

        try {
            this.store.registerUser(username, password);
        } catch (Exception e) {
            String payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        System.out.println("registered.");
        System.out.println(this.store.registeredUsers.toString());

        String payload = "Registered successfully.";
        replyMessage = new Message(true, 100, false, payload);
        reply = new InternalMessage(replyMessage, im.address);

        return reply;
    }

    private synchronized InternalMessage PDEFAULT(InternalMessage message) {
        String payload = "DEFAULTED";

        Message replyMessage = new Message(false, 100, false, payload);
        InternalMessage reply = new InternalMessage(replyMessage, message.address);

        return reply;
    }

    // AOP
    // Aspect Oriented Programming
    // aspect-j
    // look it up
    private synchronized InternalMessage P320(InternalMessage im) {
        Message replyMessage;
        InternalMessage reply;
        String payload;

        try {
            this.authenticate(im.message.getSessionToken());
        } catch (Exception e) {
            payload = e.getMessage();
            replyMessage = new Message(true, 210, true, payload);
            reply = new InternalMessage(replyMessage, im.address);
            return reply;
        }

        payload = "Everyone won!";

        replyMessage = new Message(true, 320, false, payload);
        reply = new InternalMessage(replyMessage, im.address);

        return reply;
    }

    // Utilities and Helpers
    private void authenticate(String sessionToken) throws Exception {
        if (sessionToken == null || !(store.loggedInUsers.containsKey(sessionToken))) {
            throw new Exception("Not authenticated. Log in first.");
        }
    }
}
