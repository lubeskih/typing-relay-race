package com.company.client;

import com.company.shared.Message;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Client {
    private static final int PORT = 1111;
    private static final String HOST = "localhost";

    public static void main(String[] args) {

        try(
            Socket socket = new Socket(HOST, PORT);
            // BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        ) {
            BlockingQueue<Message> bq = new LinkedBlockingDeque<>();
            ClientProtocolHandler protocol = new ClientProtocolHandler(bq);

            Message m = (Message) in.readObject();

            protocol.process(m);

            System.out.println("Code in BQ: " + bq.peek().reply);

            try {
                System.out.println("Sending last object through the wire ...");
                out.writeObject(bq.poll());
                System.out.println("Sent!");
                System.out.println("Size of bq: " + bq.size());
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            System.out.println("Bye bye! :)");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
