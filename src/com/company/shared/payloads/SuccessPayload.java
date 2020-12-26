package com.company.shared.payloads;

public class SuccessPayload implements java.io.Serializable {
    public String message;

    public SuccessPayload(String message) {
        this.message = message;
    }
}
