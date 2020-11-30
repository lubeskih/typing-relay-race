package com.company.client;

import com.company.shared.Message;
import com.company.shared.ProtocolDictionary;
import com.company.shared.payloads.LoginPayload;
import com.company.shared.payloads.RegisterPayload;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class ClientProtocolHandler extends ProtocolDictionary {
    public BlockingQueue<Message> bq;
    public HashMap<String, Boolean> commands = new HashMap<>();
    public Store store;

    public ClientProtocolHandler(BlockingQueue<Message> bq, Store store) {
        super();
        this.bq = bq;
        this.store = store;

        this.initCommands();
    }

    public void initCommands() {
        this.commands.put(":login", true);
        this.commands.put(":register", true);
        this.commands.put(":clear", true);
        this.commands.put(":scoreboard", true);
        this.commands.put(":teams", true);
        this.commands.put(":join", true);
        this.commands.put(":create", true);
        this.commands.put(":exit", true);
        this.commands.put(":ping", true);
        this.commands.put(":help", true);
    }

    public void process (Message m) throws Exception {
        int reply = m.reply;

        switch(reply) {
            case 100: P100(m); break;
            case 200: P200(); break;
            case 210: P210(); break;
            case 230: P230(); break;
            case 240: P240(); break;
            case 250: P250(); break;
            case 300: P300(m); break;
            case 310: P310(); break;
            case 320: P320(); break;
            case 330: P330(); break;
            case 340: P340(); break;
            case 350: P350(); break;
        }
    }

    private void P350() {
    }

    private void P340() {
    }

    private void P330() {
    }

    private void P320() {
    }

    private void P310() {
    }

    private void P300(Message m) {
        store.setSessionToken((String) m.payload);
    }

    private void P250() {
    }

    private void P240() {
    }

    private void P230() {
    }

    private void P210() {
    }

    private void P200() {
    }

    private void P100 (Message m) throws Exception {
        if (!(m.payload instanceof String)) {
            throw new Exception("Fucked up.");
        }
    }

    public void processUserInput(String input) {
        String[] split = input.split(" ");

        switch (split[0]) {
            case ":register": processRegisterInput(split); break;
            case ":login": processLoginInput(split); break;
            case ":scoreboard": processScoreboardInput(); break;
            case ":help": help(); break;
            case ":ping": healthcheck(); break;
            default: System.out.println("Command is valid but not yet implemented!"); break;
        }
    }

    private void healthcheck() {
        String ping = "ping";

        Message m = new Message(false, 360, false, ping);
        this.bq.add(m);
    }

    private void processScoreboardInput() {
        String payload = this.translateReplyCode(320); // Not really needed
        Message m = new Message(false, 320, false, payload);
        this.bq.add(m);
    }

    private void processLoginInput(String[] credentials) {
        if (credentials.length != 3) {
            System.out.println("Invalid login input! Use: :login <user> <pass>");
            return;
        }

        String username = credentials[1];
        String password = credentials[2];

        LoginPayload lp = new LoginPayload(username, password);

        Message m = new Message(false, 300, false, lp);
        this.bq.add(m);
    }

    private void processRegisterInput(String[] credentials) {
        if (credentials.length != 4) {
            System.out.println("Invalid register input! Use: :register <user> <pass> <pass>");
            return;
        }

        String username = credentials[1];
        String password = credentials[2];
        String repeatedPassword = credentials[3];

        RegisterPayload registerPayload = new RegisterPayload(username, password, repeatedPassword);

        Message m = new Message(false, 310, false, registerPayload);
        this.bq.add(m);
    }

    public boolean validCommand(String command) {
        String[] split = command.split(" ");

        if (this.commands.containsKey(split[0])) {
            return true;
        } else {
            return false;
        }
    }

    public void help() {
        System.out.println("COMMAND                                             ACTION\n" +
                "===========================================================================\n" +
                ":help                                               Show help\n" +
                ":login <username> <password>                        Login\n" +
                ":register <username> <password> <repeat_password>   Register an account\n" +
                ":scoreboard                                         View Scoreboard\n" +
                ":create <team_name>                                 Create a New Team\n" +
                ":join <team_name>                                   Join an Existing Team\n" +
                ":teams                                              List All Teams\n" +
                ":ping                                               Connection Healthcheck\n" +
                ":clear                                              Clears the Screen\n" +
                "===========================================================================\n");
    }
}
