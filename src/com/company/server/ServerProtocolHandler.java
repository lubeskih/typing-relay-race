package com.company.server;

import com.company.shared.Message;
import com.company.shared.ProtocolDictionary;
import com.company.shared.payloads.LoginPayload;
import com.company.shared.payloads.RegisterPayload;

import java.security.SecureRandom;
import java.util.Base64;

public class ServerProtocolHandler extends ProtocolDictionary {
    private Store store;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public ServerProtocolHandler(Store store) {
        super();
        this.store = store;
    }

    public synchronized InternalMessage process (InternalMessage im) {
        int reply = im.message.reply;

        switch(reply) {
            case 300: return P300(im);
            case 310: return P310(im);
            case 320: return P320(im);
            default: return PDEFAULT(im);
        }
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
        boolean sessionTokenExists = im.message.getSessionToken() != null;
        if (sessionTokenExists) {
            if (store.loggedInUsers.containsKey(im.message.getSessionToken())) {
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

    private synchronized InternalMessage P320(InternalMessage message) {
        String payload = "Everyone won!";

        Message replyMessage = new Message(true, 320, false, payload);
        InternalMessage reply = new InternalMessage(replyMessage, message.address);

        return reply;
    }
}
