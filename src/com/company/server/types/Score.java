package com.company.server.types;

import java.time.Duration;

public class Score implements Comparable<Score> {
    public String teamname;
    public int totalScoreInSeconds;
    public int totalScoreInMinutes;

    public Score(String teamname, Duration totalTime) {
        this.teamname = teamname;
        this.totalScoreInSeconds = (int) totalTime.toSeconds();
        this.totalScoreInMinutes = (int) totalTime.toMinutes();
    }

    @Override
    public int compareTo(Score score) {
        return this.totalScoreInSeconds - score.totalScoreInSeconds;
    }

    @Override
    public String toString() {
        return this.teamname + "\t" + this.totalScoreInSeconds + "s. (" + this.totalScoreInMinutes + "m.)";
    }
}