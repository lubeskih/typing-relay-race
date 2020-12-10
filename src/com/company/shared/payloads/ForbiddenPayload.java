package com.company.shared.payloads;

public class ForbiddenPayload implements java.io.Serializable {
    public String message;

    public ForbiddenPayload(String message) {
        this.message = message;
    }
}
