package com.company.server;

import com.company.shared.Message;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class InternalMessage {
    public Message message;
    public ObjectOutputStream address;

    public InternalMessage(Message message, ObjectOutputStream address) {
        this.message = message;
        this.address = address;
    }
}
