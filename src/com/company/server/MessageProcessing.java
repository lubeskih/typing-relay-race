package com.company.server;

import com.company.server.types.InternalMessage;
import com.company.server.types.Score;
import com.company.shared.Message;
import com.company.shared.payloads.*;

public class MessageProcessing {
    Store store;

    public MessageProcessing(Store store) {
        this.store = store;
    }

    public InternalMessage process(InternalMessage im) {
        Message newMessage;
        InternalMessage newIm;

        if (im.message.payload instanceof RegisterPayload) {
            newMessage = registerUser((RegisterPayload) im.message.payload);
        } else if (im.message.payload instanceof LoginPayload) {
            newMessage = loginUser((LoginPayload) im.message.payload, im.message.getSessionToken());
        } else if (im.message.payload instanceof ScoreboardPayload) {
            newMessage = showScoreboard(im.message.getSessionToken());
        } else if (im.message.payload instanceof CreateTeamPayload) {
            newMessage = createTeam((CreateTeamPayload) im.message.payload, im.message.getSessionToken());
        } else {
            InfoPayload ip = new InfoPayload("Bad Request. Not existing payload.");
            newMessage = new Message(true, 200, true, ip);
        }

        newIm = new InternalMessage(newMessage, im.address);
        return newIm;
    }

    private Message createTeam(CreateTeamPayload payload, String sessionToken) {
        if (!store.isAuthenticated(sessionToken)) {
            return store.notAuthenticatedMessage();
        }

        String teamname = payload.teamname;
        boolean openToAll = payload.openToAll;

        return store.createTeam(teamname, openToAll, sessionToken);
    }

    private Message showScoreboard(String sessionToken) {
        if (!store.isAuthenticated(sessionToken)) {
            return store.notAuthenticatedMessage();
        }

        return store.viewScoreboard();
    }

    private Message loginUser(LoginPayload payload, String sessionToken) {
        return store.loginUser(payload.username, payload.password, sessionToken);
    }

    private Message registerUser(RegisterPayload payload) {
        return store.registerUser(payload.username, payload.password);
    }
}
