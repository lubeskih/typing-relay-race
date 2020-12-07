package com.company.server;

import com.company.shared.Message;

import java.util.concurrent.BlockingQueue;

public class MessageProcessor implements Runnable {
    private final BlockingQueue<InternalMessage> OutboundMessageBQ;
    private final BlockingQueue<InternalMessage> InternalMessageBQ;
    private final ServerProtocolHandler protocol;
    private Store store;

    public MessageProcessor(BlockingQueue<InternalMessage> InternalMessageBQ,
                            ServerProtocolHandler protocol,
                            BlockingQueue<InternalMessage> OutboundMessageBQ,
                            Store store) {
        this.OutboundMessageBQ = OutboundMessageBQ;
        this.protocol = protocol;
        this.InternalMessageBQ = InternalMessageBQ;
        this.store = store;
    }

    @Override
    public void run() {
        try {
            while(true) {
                InternalMessage m = this.InternalMessageBQ.take();
                Thread ProcessMessageThread = new Thread(new ProcessMessage(m, this.OutboundMessageBQ));
                ProcessMessageThread.start();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ProcessMessage implements Runnable {
        private final InternalMessage m;
        private final BlockingQueue<InternalMessage> OutboundMessageBQ;

        public ProcessMessage(InternalMessage m,
                              BlockingQueue<InternalMessage> OutboundMessageBQ) {
            this.m = m;
            this.OutboundMessageBQ = OutboundMessageBQ;
        }

        @Override
        public void run() {

            String token = m.message.getSessionToken();

            if (store.isAuthenticated(token) && store.isInGame(token)) {
                LoggedInUser user = store.getUser(token);
                store.gameCoordinationBQs.get(user.team.teamname).add(m);
            } else {
                InternalMessage im = protocol.process(this.m);
                this.OutboundMessageBQ.add(im);
            }
        }
    }
}
