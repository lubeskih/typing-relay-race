package com.company.shared.payloads;

public class CreateTeamPayload implements java.io.Serializable {
    public String teamname;
    public boolean openToAll;

    public CreateTeamPayload(String teamname, boolean openToAll) {
        this.teamname = teamname;
        this.openToAll = openToAll;
    }
}
