package com.company.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 1111;

    public static void main(String[] args) throws Exception {

        try (var listener = new ServerSocket(PORT)) {
            System.out.println("Server is running ...");
            var pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Worker(listener.accept()));
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
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                String inputLine;

                out.println("Connected.");

                while((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("ping")) {
                        out.println("pong.");
                    }

                    if (inputLine.equals("Bye")) {
                        break;
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}

