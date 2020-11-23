package com.company.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static final int PORT = 1111;
    private static final String HOST = "localhost";

    public static void main(String[] args) {

        try(
            Socket kkSocket = new Socket(HOST, PORT);
            PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            String fromServer, fromUser;

            while ((fromServer = in.readLine()) != null) {
                System.out.println(fromServer);

                if (fromServer.equals("Bye."))
                    break;

                System.out.print(">> ");
                fromUser = stdIn.readLine();

                if (fromUser != null) {
                    out.println(fromUser);
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
