package com.company.shared.payloads;

public class ConflictPayload implements java.io.Serializable {
    public String message;

    public ConflictPayload(String message) {
        this.message = message;
    }
}
