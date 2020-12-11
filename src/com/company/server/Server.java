package com.company.server;

import com.company.server.types.InternalMessage;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {
    private static final int PORT = 1111;

    public static void main(String[] args) throws Exception {
        Store store = new Store();

        try (ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Server is running ...");
            while (true) {
                Socket client = listener.accept();

                Thread t = new Thread(new ClientMessageReceiver(client, store));
                t.start();
            }
        }
    }
}

