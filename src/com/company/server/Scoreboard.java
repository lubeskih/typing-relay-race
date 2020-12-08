package com.company.server;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;

public class Scoreboard {
    public ArrayList<Score> scoreboard;

    public Scoreboard() {
        this.scoreboard = new ArrayList();
    }

    public synchronized void addNewScore(Score score) {
        scoreboard.add(score);
        Collections.sort(scoreboard);

        if (scoreboard.size() > 5) {
            scoreboard.remove(scoreboard.size() - 1);
        }
    }

    public String viewScoreboard() {
        Base64.Encoder encoder = Base64.getEncoder();
        String b64score;

        if (scoreboard.size() == 0) {
            String score = "Scoreboard empty! No one has played, yet.";
            b64score = encoder.encodeToString(score.getBytes());
            return b64score;
        }

        String score = "SCOREBOARD\n";
        score += "==========================\n";

        for (Score s:scoreboard) {
            score += s.teamname + "\t" + s.totalScoreInSeconds + "s. (" + s.totalScoreInMinutes + "m)\n";
        }

        b64score = encoder.encodeToString(score.getBytes());

        return b64score;
    }
}
