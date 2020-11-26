package com.company.client;

import com.company.shared.Message;
import com.company.shared.ProtocolDictionary;

import java.util.concurrent.BlockingQueue;

public class ClientProtocolHandler extends ProtocolDictionary {
    public BlockingQueue<Message> bq;

    public ClientProtocolHandler(BlockingQueue<Message> bq) {
        super();
        this.bq = bq;
    }

    public void process (Message m) throws Exception {
        int reply = m.reply;

        switch(reply) {
            case 100: P100(m); break;
            case 200: P200(); break;
            case 210: P210(); break;
            case 230: P230(); break;
            case 240: P240(); break;
            case 250: P250(); break;
            case 300: P300(); break;
            case 310: P310(); break;
            case 320: P320(); break;
            case 330: P330(); break;
            case 340: P340(); break;
            case 350: P350(); break;
        }
    }

    private void P350() {
    }

    private void P340() {
    }

    private void P330() {
    }

    private void P320() {
    }

    private void P310() {
    }

    private void P300() {
    }

    private void P250() {
    }

    private void P240() {
    }

    private void P230() {
    }

    private void P210() {
    }

    private void P200() {
    }

    private void P100 (Message m) throws Exception {
        if (!(m.payload instanceof String)) {
            throw new Exception("Fucked up.");
        }

        System.out.println("Received " + m.reply + " from server, which translates to " + this.translateReplyCode(m.reply));
        System.out.println("Server says: " + m.payload);

        String replyMessage = "Good.";

        Message output = new Message(true, 100, false, replyMessage);

        this.bq.add(output);
        System.out.println("Response added in BQ!");
    }
}
