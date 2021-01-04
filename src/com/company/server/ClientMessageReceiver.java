package com.company.server;

import com.company.server.types.InternalMessage;
import com.company.server.types.LoggedInUser;
import com.company.shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientMessageReceiver implements Runnable {
    private Socket socket;
    MessageProcessor messageProcessor;
    private Store store;

    ClientMessageReceiver(Socket socket, Store store) {
        this.socket = socket;
        this.messageProcessor = new MessageProcessor(store);
        this.store = store;
    }

    @Override
    public void run() {
        Comms comms = null;

        try {
            comms = new Comms(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ObjectInputStream in = comms.getInputStream();
            ObjectOutputStream out = comms.getOutputStream();

            while(true) {
                Message received = (Message) in.readObject();
                InternalMessage message = new InternalMessage(received, out);

                String token = received.getSessionToken();

                if (store.isAuthenticated(token) && store.isInGame(token)) {
                    // pass to GC
                    LoggedInUser user = store.getUser(token);

                    BlockingQueue<Message> assignedGameCoordinator = this.store.gameCoordinationBQs.get(user.team.teamname);
                    assignedGameCoordinator.add(received);
                } else {
                    InternalMessage im = messageProcessor.process(message);

                    if (im.message.isResponse) {
                        out.writeObject(im.message);
                    } else {
                        // nothing
                    }
                }
            }
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        } finally {
            try {
                comms.closeInputStream();
                comms.closeOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}