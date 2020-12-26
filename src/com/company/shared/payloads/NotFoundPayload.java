package com.company.shared.payloads;

public class NotFoundPayload implements java.io.Serializable {
    public String message;

    public NotFoundPayload(String message) {
        this.message = message;
    }
}
