package com.company.server;

import com.company.server.types.InternalMessage;
import com.company.server.types.LoggedInUser;
import com.company.shared.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientMessageReceiver implements Runnable {
    private Socket socket;
    MessageProcessing messageProcessor;
    private Store store;

    ClientMessageReceiver(Socket socket, Store store) {
        this.socket = socket;
        this.messageProcessor = new MessageProcessing(store);
        this.store = store;
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
                InternalMessage message = new InternalMessage(received, out);

                String token = received.getSessionToken();

                System.out.println("User sent a token which is: " + received.getSessionToken());

                // pass to MP
                // get from MP
                // hasToken and inGame? pass to GameCoordinator
                // if not
                // is response?
                // if yes -> write
                // if not -> none

                if (store.isAuthenticated(token) && store.isInGame(token)) {
                    // pass to GC
                    LoggedInUser user = store.getUser(token);

                    BlockingQueue assignedGameCoordinator = this.store.gameCoordinationBQs.get(user.team.teamname);
                    assignedGameCoordinator.add(message);
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
        }
    }
}