package com.company.server;

import com.company.shared.Message;

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

                for(;;) {
                    System.out.println("Waiting for an inputObject ...");
                    Message received = (Message) in.readObject();
                    System.out.println("Received a " + received.reply + " requests with a payload of " + received.payload);

                    System.out.println("Preparing message ...");
                    Message r = new Message(false, 100, false,"Hello mate! :)");
                    out.writeObject(r);
                    System.out.println("Message sent! Closing.");
                }
            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }
}

