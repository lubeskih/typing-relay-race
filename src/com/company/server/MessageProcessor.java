package com.company.server;

import com.company.shared.Message;

import java.util.concurrent.BlockingQueue;

public class MessageProcessor implements Runnable {
    private BlockingQueue<InternalMessage> OutboundMessageBQ;
    private BlockingQueue<InternalMessage> InternalMessageBQ;
    private ServerProtocolHandler protocol;

    public MessageProcessor(BlockingQueue<InternalMessage> InternalMessageBQ, ServerProtocolHandler protocol, BlockingQueue<InternalMessage> OutboundMessageBQ) {
        this.OutboundMessageBQ = OutboundMessageBQ;
        this.protocol = protocol;
        this.InternalMessageBQ = InternalMessageBQ;
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
        private InternalMessage m;
        private BlockingQueue<InternalMessage> OutboundMessageBQ;

        public ProcessMessage(InternalMessage m, BlockingQueue<InternalMessage> OutboundMessageBQ) {
            this.m = m;
            this.OutboundMessageBQ = OutboundMessageBQ;
        }

        @Override
        public void run() {
            ServerProtocolHandler protocol = new ServerProtocolHandler();
            InternalMessage im = protocol.process(this.m);
            this.OutboundMessageBQ.add(im);
        }
    }
}
