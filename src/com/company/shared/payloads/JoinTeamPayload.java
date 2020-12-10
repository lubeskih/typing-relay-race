package com.company.shared.payloads;

public class JoinTeamPayload implements java.io.Serializable {
    public String teamname;
    public String password;

    public JoinTeamPayload(String teamname, String password) {
        this.teamname = teamname;
        this.password = password;
    }
}
