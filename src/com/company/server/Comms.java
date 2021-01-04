package com.company.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comms {
    private ObjectInputStream in;
    private ObjectOutputStream out;

    Comms(Socket socket) throws IOException {
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    public synchronized ObjectInputStream getInputStream() {
        return this.in;
    }

    public synchronized ObjectOutputStream getOutputStream() {
        return this.out;
    }

    public synchronized void closeInputStream() throws IOException {
        this.in.close();
    }

    public synchronized void closeOutputStream() throws IOException {
        this.out.close();
    }
}
