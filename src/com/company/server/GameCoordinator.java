package com.company.server;

import com.company.shared.Message;

import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class GameCoordinator implements Runnable {
    private Team team;
    private Store store;
    private BlockingQueue<InternalMessage> bq;
    private BlockingQueue<InternalMessage> OutboundMessageBQ;
    Duration p1totalTime;
    Duration p2totalTime;
    Duration totalTeamTime;

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

            payload = "Player 1 is starting first! Waiting to submit.";
            notify = new Message(false, 100, false, payload);
            sendMessage(notify, p2);

            String generatedText = "Hello world!";
            payload = "Write back this text, fast!\n\n" + generatedText;
            ask = new Message(false, 260, false, payload);
            sendMessage(ask, p1);

            Instant starts = Instant.now();
            while (!(checklist.p1done)) {
                InternalMessage im = bq.take();

                if (!(isOwner(im.message.getSessionToken()))) {
                    payload = "Not your turn to submit. Wait.";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p2);
                    continue;
                }

                if (im.message.payload.equals(generatedText)) {
                    checklist.p1done = true;
                    checklist.p1valid = true;
                } else {
                    payload = "Wrong text! Will not be counted. :(";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p1);

                    payload = "Player 1 submitted wrong text and will not be counted!";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p2);
                    break;
                }
            }

            if (checklist.p1valid) {
                Instant end = Instant.now();
                this.p1totalTime = Duration.between(starts, end);
                payload = "Player 1 took: " + this.p1totalTime.getSeconds() + " seconds, or " + this.p1totalTime.toMinutes() + " minutes.";

                this.totalTeamTime = Duration.between(starts, end);

                notify = new Message(false, 100, false, payload);
                sendMessage(notify, p1);
                sendMessage(notify, p2);
            } else {
                this.p1totalTime = null;
            }

            payload = "Player 2's turn! Waiting to submit.";
            notify = new Message(false, 100, false, payload);
            sendMessage(notify, p1);

            generatedText = "Hello morld!";
            payload = "Write back this text, fast!\n\n" + generatedText;
            ask = new Message(false, 260, false, payload);
            sendMessage(ask, p2);

            starts = Instant.now();
            while (!(checklist.p2done)) {
                InternalMessage im = bq.take();

                if ((isOwner(im.message.getSessionToken()))) {
                    payload = "Not your turn to submit. Wait.";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p1);
                    continue;
                }

                if (im.message.payload.equals(generatedText)) {
                    checklist.p2done = true;
                    checklist.p2valid = true;
                } else {
                    payload = "Wrong text! Will not be counted. :(";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p2);

                    payload = "Player 2 submitted wrong text and will not be counted!";
                    notify = new Message(false, 100, false, payload);
                    sendMessage(notify, p1);
                    break;
                }
            }

            if (checklist.p2valid) {
                Instant end = Instant.now();
                this.p2totalTime = Duration.between(starts, end);
                payload = "Player 2 took: " + this.p2totalTime.getSeconds() + " seconds (" + this.p2totalTime.toMinutes() + " minutes).";

                this.totalTeamTime = this.totalTeamTime.plus(Duration.between(starts, end));

                notify = new Message(false, 100, false, payload);
                sendMessage(notify, p1);
                sendMessage(notify, p2);
            } else {
                this.p1totalTime = null;
            }

            payload = "Total time for the team " + team.teamname +
                    " is " + this.totalTeamTime.toSeconds() +
                    " seconds (" + this.totalTeamTime.toMinutes() + ") minutes.";
            notify = new Message(false, 100, false, payload);
            sendMessage(notify, p1);
            sendMessage(notify, p2);

        } catch (Exception e) {
            e.printStackTrace();
        }

        store.gameCoordinationBQs.remove(team.teamname);
        store.teams.get(team.teamname).memberOne.team = null;
        store.teams.get(team.teamname).memberTwo.team = null;
        store.teams.remove(team.teamname);

        System.out.println("Game done. Resetting everything.");
    }

    public static class Checklist {
        boolean p1IsReady = false;
        boolean p2IsReady = false;
        boolean p1done = false;
        boolean p2done = false;
        boolean p1valid = false;
        boolean p2valid = false;
        boolean restart = false;
        boolean inProgress = false;
    }
}
