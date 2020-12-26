package com.company.shared.payloads;

public class SessionPayload implements java.io.Serializable {
    public String sessionToken;

    public SessionPayload(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
