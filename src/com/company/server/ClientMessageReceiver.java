package com.company.server;

import com.company.shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientMessageReceiver implements Runnable {
    private Socket socket;
    private BlockingQueue<InternalMessage> InternalMessageBQ;

    ClientMessageReceiver(Socket socket, BlockingQueue<InternalMessage> bq) {
        this.socket = socket;
        this.InternalMessageBQ = bq;
    }

    @Override
    public void run() {
        try(
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        )
        {
            while(true) {
                Message received = (Message) in.readObject();
                System.out.println("User sent a token which is: " + received.getSessionToken());

                InternalMessage im = new InternalMessage(received, out);
                this.InternalMessageBQ.add(im);
            }
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }
}