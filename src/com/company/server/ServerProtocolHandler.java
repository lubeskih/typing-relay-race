package com.company.server;

import com.company.shared.Message;
import com.company.shared.ProtocolDictionary;

public class ServerProtocolHandler extends ProtocolDictionary {

    public ServerProtocolHandler() {
        super();
    }

    public InternalMessage process (InternalMessage im) {
        int reply = im.message.reply;

        switch(reply) {
            case 320: return P320(im);
            default: return PDEFAULT(im);
        }
    }

    private InternalMessage PDEFAULT(InternalMessage message) {
        String payload = "DEFAULTED";

        Message replyMessage = new Message(false, 100, false, payload);
        InternalMessage reply = new InternalMessage(replyMessage, message.address);

        return reply;
    }

    private InternalMessage P320(InternalMessage message) {
        String payload = "Everyone won!";

        Message replyMessage = new Message(true, 320, false, payload);
        InternalMessage reply = new InternalMessage(replyMessage, message.address);

        return reply;
    }
}
