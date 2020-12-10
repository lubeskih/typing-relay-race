package com.company.server.types;

import com.company.shared.Message;

import java.io.ObjectOutputStream;

public class InternalMessage {
    public Message message;
    public ObjectOutputStream address;

    public InternalMessage(Message message, ObjectOutputStream address) {
        this.message = message;
        this.address = address;
    }
}
