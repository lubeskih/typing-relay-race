package com.company.server;

import com.company.server.types.Score;

import java.util.ArrayList;
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

    public synchronized boolean betterThanTheFirstPlace(int score) {
        if (scoreboard.size() > 0) {
            Score firstTeam = scoreboard.get(0);
            return score < firstTeam.totalScoreInSeconds;
        }

        return true;
    }
}