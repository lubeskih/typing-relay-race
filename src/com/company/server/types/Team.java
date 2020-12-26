package com.company.server.types;

public class Team {
    public String teamname;
    public LoggedInUser admin;
    public LoggedInUser memberTwo;
    public String memberOneScore = null;
    public String memberTwoScore = null;
    public String scoreTotal = null;
    public boolean teamInGame = false;
    public String password = null;
    public boolean visible = true;

    public Team(String teamname, LoggedInUser admin, String password) {
        this.teamname = teamname;
        this.admin = admin;

        if (password != null) {
            this.password = password;
            this.visible = false;
        }
    }
}
