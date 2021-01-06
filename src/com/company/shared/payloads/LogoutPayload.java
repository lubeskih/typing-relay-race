package com.company.shared.payloads;

public class LogoutPayload implements java.io.Serializable {
    public String message;

    public LogoutPayload(String message) {
        this.message = message;
    }
}
