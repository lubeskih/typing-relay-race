package com.company.server;

import com.company.Shared.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 1111;

    public static void main(String[] args) throws Exception {

        try (var listener = new ServerSocket(PORT)) {
            System.out.println("Server is running ...");
            var pool = Executors.newFixedThreadPool(20); // should not be fixed
            while (true) { // after this ends, make sure to signal them to exit
                Socket client = listener.accept();

                // Create a blocking queue
                Thread t = new Thread(new Worker(client));
                t.start();

                // read semaphores, read-write log, mutex, syncronized
            }
        }
    }

    private static class Worker implements Runnable {
        private Socket socket;

        Worker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try(
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            )
            {
                Message m = new Message(false, 100, false,"Hello mate! :)");
                out.writeObject(m);
                System.out.println("Message sent! Closing.");

                Message received = (Message) in.readObject();
                System.out.println("Received " + received.payload);

            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }
}

