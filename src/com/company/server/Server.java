package com.company.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {
    private static final int PORT = 1111;

    public static void main(String[] args) throws Exception {
        BlockingQueue<InternalMessage> InternalMessageBQ = new LinkedBlockingDeque<>();
        BlockingQueue<InternalMessage> OutboundMessagesBQ = new LinkedBlockingDeque<>();
        Store store = new Store();

        ServerProtocolHandler protocol = new ServerProtocolHandler(store, OutboundMessagesBQ);

        Thread mp = new Thread(new MessageProcessor(InternalMessageBQ, protocol, OutboundMessagesBQ, store));
        mp.start();

        Thread pm = new Thread(new Postoffice(OutboundMessagesBQ));
        pm.start();

        try (var listener = new ServerSocket(PORT)) {
            System.out.println("Server is running ...");
            while (true) {
                Socket client = listener.accept();

                Thread t = new Thread(new ClientMessageReceiver(client, InternalMessageBQ));
                t.start();
            }
        }
    }
}

