package com.company.server;

import com.company.shared.Message;

import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

public class GameCoordinator implements Runnable {
    private Team team;
    private Store store;
    private BlockingQueue<InternalMessage> bq;
    private BlockingQueue<InternalMessage> OutboundMessageBQ;

    private Checklist checklist;

    public GameCoordinator(String teamname,
                           Store store,
                           BlockingQueue<InternalMessage> bq,
                           BlockingQueue<InternalMessage> OutboundMessageBQ) {
        this.store = store;
        this.team = store.teams.get(teamname);
        this.bq = bq;
        this.OutboundMessageBQ = OutboundMessageBQ;
        this.checklist = new Checklist();
    }

    private boolean isOwner(String sessionToken) {
        LoggedInUser user = store.loggedInUsers.get(sessionToken);
        String name = user.username;
        return team.memberOne.username.equals(name);
    }

    private void sendMessage(Message m, ObjectOutputStream address) {
        InternalMessage reply = new InternalMessage(m, address);
        this.OutboundMessageBQ.add(reply);
    }

    @Override
    public void run() {
        try {
            Message ask;
            Message notify;
            String payload;

            ObjectOutputStream p1 = team.memberOne.address;
            ObjectOutputStream p2 = team.memberTwo.address;

            payload = "Game is about to begin!";
            Message m = new Message(false, 100, false, payload);

            sendMessage(m, p1);
            sendMessage(m, p2);


            payload = "Please send :ready.";
            ask = new Message(false, 100, false, payload);
            sendMessage(ask, p1);


            payload = "Waiting on Player 1 to accept!";
            notify = new Message(false, 100, false, payload);
            sendMessage(notify, p2);


            while (!(checklist.p1IsReady)) {
                InternalMessage im = bq.take();

                if (!(isOwner(im.message.getSessionToken()))) {
                    payload = "Not your turn to accept. Wait.";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p2);
                    continue;
                }

                if (im.message.payload.equals("ready")) {
                    checklist.p1IsReady = true;
                } else {
                    payload = "Didn't understand. Send 'ready'.";
                    ask = new Message(false, 100, false, payload);
                    sendMessage(ask, p1);
                }
            }

            payload = "Waiting on Player 2 to accept!";
            notify = new Message(false, 100, false, payload);
            sendMessage(notify, p1);

            payload = "Player 1 accepted. Please send :ready to accept.";
            ask = new Message(false, 100, false, payload);
            sendMessage(ask, p2);

            while (!(checklist.p2IsReady)) {
                InternalMessage im = bq.take();

                if ((isOwner(im.message.getSessionToken()))) {
                    payload = "You already accepted, I'm now waiting on Player 2.";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p1);
                    continue;
                }

                if (im.message.payload.equals("ready")) {
                    checklist.p2IsReady = true;
                } else {
                    payload = "Didn't understand. Send 'ready'.";
                    ask = new Message(false, 100, false, payload);
                    sendMessage(ask, p2);
                }
            }

            payload = "Both ready!";
            Message both = new Message(false, 100, false, payload);

            sendMessage(both, p1);
            sendMessage(both, p2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        store.gameCoordinationBQs.remove(team.teamname);
        store.teams.get(team.teamname).memberOne.team = null;
        store.teams.get(team.teamname).memberTwo.team = null;
        store.teams.remove(team.teamname);

        System.out.println("Both accepted! Closing.");
    }

    public static class Checklist {
        boolean p1IsReady = false;
        boolean p2IsReady = false;
        boolean p1done = false;
        boolean p2done = false;
        boolean restart = false;
        boolean inProgress = false;
    }
}
