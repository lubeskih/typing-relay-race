package com.company.server;

import com.company.shared.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

public class Postoffice implements Runnable {
    private BlockingQueue<InternalMessage> OutboundMessageBQ;

    public Postoffice(BlockingQueue<InternalMessage> OutboundMessageBQ) {
        this.OutboundMessageBQ = OutboundMessageBQ;
    }

    @Override
    public void run() {
        try {
            while(true) {
                InternalMessage m = this.OutboundMessageBQ.take();

                Thread deliver = new Thread(new Postman(m.message, m.address));
                deliver.start();
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private static class Postman implements Runnable {
        private Message m;
        private ObjectOutputStream out;

        public Postman(Message m, ObjectOutputStream s) {
            this.m = m;
            this.out = s;
        }

        @Override
        public void run() {
            try {
                out.writeObject(m);
                System.out.println("Sent a " + m.reply + " request with payload of " + m.payload);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
