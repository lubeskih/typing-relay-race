package com.company.client;

import com.company.server.Server;
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
            BlockingQueue<Message> bq = new LinkedBlockingDeque<>();
            ClientProtocolHandler protocol = new ClientProtocolHandler(bq);

            Thread read = new Thread(new Read(socket, protocol));
            read.start();

            Thread write = new Thread(new Write(socket, bq));
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

    Read(Socket socket, ClientProtocolHandler protocol) {
        this.socket = socket;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try(
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ) {
            while(true) {
                Message m = (Message) in.readObject();
                System.out.println("Received a " + m.reply + " request.");
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

    Write(Socket socket, BlockingQueue<Message> bq) {
        this.socket = socket;
        this.bq = bq;
    }

    @Override
    public void run() {
        try(
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ) {
            while(true) {
                if(!bq.isEmpty()) {
                    Message m = bq.poll();
                    out.writeObject(m);

                    System.out.println("Sent a " + m.reply + " request!");
                } else {
                    Thread.sleep(500);
                }
            }

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}