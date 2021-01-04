package com.company.server;

import com.company.server.types.InternalMessage;
import com.company.server.types.LoggedInUser;
import com.company.server.types.Score;
import com.company.server.types.Team;
import com.company.shared.Message;
import com.company.shared.payloads.BadRequestPayload;
import com.company.shared.payloads.InfoPayload;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

public class GameCoordinator implements Runnable {
    private final Store store;

    private final Team team;
    private final BlockingQueue<Message> messageBlockingQueue;

    LoggedInUser p1;
    LoggedInUser p2;

    Duration p1totalTime;
    Duration p2totalTime;
    Duration totalTeamTime;

    private Checklist checklist;

    public GameCoordinator(String teamname, Store store) {
        this.store = store;
        this.team = store.teams.get(teamname);

        this.p1 = team.admin;
        this.p2 = team.memberTwo;

        this.messageBlockingQueue = store.gameCoordinationBQs.get(teamname);
        this.checklist = new Checklist();
    }

    private boolean isOwner(String sessionToken) {
        LoggedInUser user = store.loggedInUsers.get(sessionToken);
        String name = user.username;
        return team.admin.username.equals(name);
    }

    private synchronized void sendMessage(Message m, LoggedInUser player) {
        try {
            player.address.writeObject(m);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        LoggedInUser p1 = this.p1;
        LoggedInUser p2 = this.p2;

        InfoPayload ip;
        BadRequestPayload brp;
        Message m;

        try {
            ip = new InfoPayload(team.memberTwo.username + " joined your team.");
            m = new Message(false, 110, false, ip);
            sendMessage(m, this.p1);

            team.teamInGame = true;

            ip = new InfoPayload("Game is about to begin!");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p1);
            sendMessage(m, p2);

            ip = new InfoPayload("Please send :ready.");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p1);

            ip = new InfoPayload("Waiting on player " + team.admin.username + " to accept!");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p2);

            while (!(checklist.p1IsReady)) {
                Message receivedMessage = messageBlockingQueue.take();
                String token = receivedMessage.getSessionToken();

                if (!(isOwner(token))) {
                    ip = new InfoPayload("Not your turn to accept. Wait for your turn.");
                    m = new Message(false, 110, false, ip);
                    sendMessage(m, p2);
                    continue;
                }

                if (!(receivedMessage.payload instanceof InfoPayload)) {
                    brp = new BadRequestPayload("Invalid payload. Might be a bug within the client.");
                    m = new Message(true, 200, true, brp);
                    sendMessage(m, p1);
                    continue;
                } else {
                    if (((InfoPayload) receivedMessage.payload).message.equals("ready")) {
                        checklist.p1IsReady = true;
                    } else {
                        ip = new InfoPayload("Didn't understand. Type ':ready'.");
                        m = new Message(false, 110, false, ip);
                        sendMessage(m, p1);
                    }
                }
            }

            ip = new InfoPayload("OK. Now waiting on player " + team.memberTwo.username + " to accept!");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p1);

            ip = new InfoPayload("Player " + team.admin.username + " accepted. It's your turn. Type :ready to accept.");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p2);

            while (!(checklist.p2IsReady)) {
                Message receivedMessage = messageBlockingQueue.take();
                String token = receivedMessage.getSessionToken();

                if ((isOwner(token))) {
                    ip = new InfoPayload("You already accepted. Waiting on " + team.memberTwo.username + ".");
                    m = new Message(false, 110, false, ip);
                    sendMessage(m, p1);
                    continue;
                }

                if (!(receivedMessage.payload instanceof InfoPayload)) {
                    brp = new BadRequestPayload("Invalid payload. Might be a bug within the client.");
                    m = new Message(true, 200, true, brp);
                    sendMessage(m, p2);
                    continue;
                } else {
                    if (((InfoPayload) receivedMessage.payload).message.equals("ready")) {
                        checklist.p2IsReady = true;
                    } else {
                        ip = new InfoPayload("Didn't understand. Type ':ready'.");
                        m = new Message(false, 110, false, ip);
                        sendMessage(m, p2);
                    }
                }
            }

            ip = new InfoPayload("Get ready! Game begins in ...");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p1);
            sendMessage(m, p2);

            for (int i = 5; i >= 1; i--) {
                ip = new InfoPayload("Countdown: " + i);
                m = new Message(false, 110, false, ip);
                sendMessage(m, p1);
                sendMessage(m, p2);

                Thread.sleep(1000);
            }

            ip = new InfoPayload("Player " + team.admin.username + " is starting first. Waiting to submit.");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p2);

            String generatedText =  store.generateRandomWords();
            ip = new InfoPayload("Write back this text, fast!\n\n" + generatedText);
            m = new Message(false, 110, false, ip);
            sendMessage(m, p1);

            Instant start = Instant.now();
            while (!(checklist.p1done)) {
                Message receivedMessage = messageBlockingQueue.take();
                String token = receivedMessage.getSessionToken();

                if (!(isOwner(token))) {
                    ip = new InfoPayload("Not your turn to submit. Wait for your turn.");
                    m = new Message(false, 110, false, ip);
                    sendMessage(m, p2);
                    continue;
                }

                if (((InfoPayload) receivedMessage.payload).message.equals(generatedText)) {
                    checklist.p1done = true;
                    checklist.p1valid = true;

                    ip = new InfoPayload("Correct!");
                } else {
                    ip = new InfoPayload("Wrong submission! Try again, fast!");
                }

                m = new Message(false, 110, false, ip);
                sendMessage(m, p1);
            }

            if (checklist.p1valid) {
                Instant end = Instant.now();
                this.p1totalTime = Duration.between(start, end);

                ip = new InfoPayload("It took " + team.admin.username + " " + p1totalTime.getSeconds() + " seconds to finish. That adds up to " +  p1totalTime.toMinutes() + " minutes.");
                m = new Message(false, 110, false, ip);
                sendMessage(m, p2);

                ip = new InfoPayload("It took you " + p1totalTime.getSeconds() + " seconds to finish. That adds up to " +  p1totalTime.toMinutes() + " minutes.");
                m = new Message(false, 110, false, ip);
                sendMessage(m, p1);
            } else {
                p1totalTime = null;
            }

            ip = new InfoPayload("Now it's your turn!");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p2);

            ip = new InfoPayload("Now it's " + team.memberTwo.username + " turn! Waiting to submit.");
            m = new Message(false, 110, false, ip);
            sendMessage(m, p1);

            generatedText = store.generateRandomWords();
            ip = new InfoPayload("Write back this text, fast!\n\n" + generatedText);
            m = new Message(false, 110, false, ip);
            sendMessage(m, p2);

            start = Instant.now();
            while (!(checklist.p2done)) {
                Message receivedMessage = messageBlockingQueue.take();
                String token = receivedMessage.getSessionToken();

                if ((isOwner(token))) {
                    ip = new InfoPayload("You already submitted your text. Now waiting on " + team.memberTwo.username + ".");
                    m = new Message(false, 110, false, ip);
                    sendMessage(m, p1);
                    continue;
                }

                if (((InfoPayload) receivedMessage.payload).message.equals(generatedText)) {
                    checklist.p2done = true;
                    checklist.p2valid = true;

                    ip = new InfoPayload("Correct!");
                } else {
                    ip = new InfoPayload("Wrong submission! Try again, fast!");
                }

                m = new Message(false, 110, false, ip);
                sendMessage(m, p2);
            }

            if (checklist.p2valid) {
                Instant end = Instant.now();
                this.p2totalTime = Duration.between(start, end);

                ip = new InfoPayload("It took " + team.memberTwo.username + " " + p2totalTime.getSeconds() + " seconds to finish. That adds up to " +  p2totalTime.toMinutes() + " minutes.");
                m = new Message(false, 110, false, ip);
                sendMessage(m, p1);

                ip = new InfoPayload("It took you " + p2totalTime.getSeconds() + " seconds to finish. That adds up to " +  p2totalTime.toMinutes() + " minutes.");
                m = new Message(false, 110, false, ip);
                sendMessage(m, p2);

                totalTeamTime = p1totalTime.plus(p2totalTime);
            } else {
                p2totalTime = null;
            }

            if (this.totalTeamTime != null) {
                ip = new InfoPayload("Total time for the team " + team.teamname +
                        " is " + (this.totalTeamTime.getSeconds()) +
                        " seconds (" + this.totalTeamTime.toMinutes() + " minutes).");

                m = new Message(false, 110, false, ip);
                sendMessage(m, p1);
                sendMessage(m, p2);

                Score score = new Score(this.team.teamname, this.totalTeamTime);

                if (store.scoreboard.betterThanTheFirstPlace(score.totalScoreInSeconds)) {
                    ip = new InfoPayload("You are now first place! Congratulations! :)");
                    m = new Message(false, 110, false, ip);

                    sendMessage(m, p1);
                    sendMessage(m, p2);
                }

                store.scoreboard.addNewScore(score);
            } else {
                ip = new InfoPayload("You both submitted wrong text. :(");
                m = new Message(false, 110, false, ip);
                sendMessage(m, p1);
                sendMessage(m, p2);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        store.gameCoordinationBQs.remove(team.teamname);
        store.teams.get(team.teamname).admin.team = null;
        store.teams.get(team.teamname).memberTwo.team = null;
        store.teams.remove(team.teamname);

        ip = new InfoPayload("Game coordination finished. Your team was removed, but if you scored, your score was logged. Create a new team to play again.");
        m = new Message(false, 110, false, ip);
        sendMessage(m, p1);
        sendMessage(m, p2);

        System.out.println("Game done for team " + team.teamname + ". Resetting everything.");
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
