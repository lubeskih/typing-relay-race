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
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            final BlockingQueue<Message> bq = new LinkedBlockingDeque<>();
            Store store = new Store();
            ClientProtocolHandler protocol = new ClientProtocolHandler(bq, store);

            Thread read = new Thread(new Read(socket, protocol, store));
            read.start();

            Thread write = new Thread(new Write(socket, bq, store));
            write.start();

            while(true) {
                System.out.print(">> ");
                String input = stdIn.readLine();

                if (input.startsWith(":")) {
                    if (protocol.validCommand(input)) {
                        protocol.processUserInput(input);
                    } else {
                        System.out.println("Invalid command! Type :help to list supported commands.");
                    }
                } else {
                    // think about it
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Read implements Runnable {
    private Socket socket;
    private ClientProtocolHandler protocol;
    private Store store;

    Read(Socket socket, ClientProtocolHandler protocol, Store store) {
        this.socket = socket;
        this.protocol = protocol;
        this.store = store;
    }

    @Override
    public void run() {
        try(
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ) {
            while(true) {
                Message m = (Message) in.readObject();
                System.out.println("Received a " + m.reply + " response, saying: " + m.payload.toString());
                protocol.process(m);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class Write implements Runnable {
    private Socket socket;
    private BlockingQueue<Message> bq;
    private Store store;

    Write(Socket socket, BlockingQueue<Message> bq, Store store) {
        this.socket = socket;
        this.bq = bq;
        this.store = store;
    }

    @Override
    public void run() {
        try(
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ) {

            do {
                Message m = bq.take();
                String sessionToken = this.store.getSessionToken();

                // if we have a session token in the Store
                // then every outbound message will have
                // the session token as part of the message
                if (sessionToken != null) {
                    m.setSessionToken(sessionToken);
                }

                out.writeObject(m);

                System.out.println("Sent a " + m.reply + " request with a payload of " + m.payload);

                // if we have a session token, but the outbound message
                // was "logout", then remove the session token
                if (m.reply == 370 && sessionToken != null) {
                    this.store.setSessionToken(null);
                }
            } while(true); // while true.. ? think about this.

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}