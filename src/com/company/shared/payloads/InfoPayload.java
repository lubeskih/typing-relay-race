package com.company.shared.payloads;

public class InfoPayload implements java.io.Serializable {
    public String message;

    public InfoPayload(String message) {
        this.message = message;
    }
}
