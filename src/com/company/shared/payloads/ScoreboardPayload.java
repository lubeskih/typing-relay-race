package com.company.shared.payloads;

public class ScoreboardPayload implements java.io.Serializable {
    public String scoreboard;

    public ScoreboardPayload(String scoreboard) {
        this.scoreboard = scoreboard;
    }
}
