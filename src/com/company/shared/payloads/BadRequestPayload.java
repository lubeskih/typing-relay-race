package com.company.shared.payloads;

public class BadRequestPayload implements java.io.Serializable {
    public String message;

    public BadRequestPayload(String message) {
        this.message = message;
    }
}
