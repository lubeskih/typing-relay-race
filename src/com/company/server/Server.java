package com.company.server;

import com.company.shared.Message;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {
    private static final int PORT = 1111;

    public static void main(String[] args) throws Exception {
        final BlockingQueue<InternalMessage> InternalMessageBQ = new LinkedBlockingDeque<>();
        final BlockingQueue<InternalMessage> OutboundMessagesBQ = new LinkedBlockingDeque<>();

        ServerProtocolHandler protocol = new ServerProtocolHandler();

        Thread mp = new Thread(new MessageProcessor(InternalMessageBQ, protocol, OutboundMessagesBQ));
        mp.start();

        Thread pm = new Thread(new Postoffice(OutboundMessagesBQ));
        pm.start();

        try (var listener = new ServerSocket(PORT)) {
            System.out.println("Server is running ...");
//            var pool = Executors.newFixedThreadPool(20); // should not be fixed
            while (true) { // after this ends, make sure to signal them to exit
                Socket client = listener.accept();

                Thread t = new Thread(new ClientMessageReceiver(client, InternalMessageBQ));
                t.start();
            }
        }
    }
}

