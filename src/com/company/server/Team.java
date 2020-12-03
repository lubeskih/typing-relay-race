package com.company.server;

public class Team {
    public String teamname;
    public LoggedInUser memberOne;
    public LoggedInUser memberTwo = null;
    public String memberOneScore = null;
    public String memberTwoScore = null;
    public String scoreTotal = null;

    public Team(String teamname, LoggedInUser memberOne) {
        this.teamname = teamname;
        this.memberOne = memberOne;
    }
}
