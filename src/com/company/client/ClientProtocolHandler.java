package com.company.client;

import com.company.shared.Message;
import com.company.shared.payloads.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class ClientProtocolHandler {
    public BlockingQueue<Message> messageBlockingQueue;
    public HashMap<String, Boolean> commands = new HashMap<>();
    public Store store;

    public ClientProtocolHandler(BlockingQueue<Message> messageBlockingQueue, Store store) {
        super();
        this.messageBlockingQueue = messageBlockingQueue;
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
        this.commands.put(":logout", true);
        this.commands.put(":ready", true);
    }

    public String translate(int reply) {
        switch (reply) {
            case 100: return "OK";
            case 110: return "Info";
            case 200: return "Bad Request";
            case 210: return "Internal Server Error";
            case 220: return "Forbidden";
            case 230: return "Conflict";
            case 240: return "Not Found";
            default: return "UNKNOWN STATUS CODE";
        }
    }

    public void process (Message m) {
        int reply = m.reply;
        String mp;

        System.out.print("[SERVER: " + translate(reply) + " (" + m.reply + ")] ");

        if (m.payload instanceof SuccessPayload) {
            mp = ((SuccessPayload) m.payload).message;
            System.out.println(mp);
        } else if (m.payload instanceof ConflictPayload) {
            mp = ((ConflictPayload) m.payload).message;
            System.out.println(mp);
        } else if (m.payload instanceof InfoPayload) {
            mp = ((InfoPayload) m.payload).message;

            if (mp.startsWith("Write back this text, fast!")) {
                store.nextMessageIsASentence = true;
            } else if (mp.equals("Correct!")) {
                store.nextMessageIsASentence = false;
            }

            System.out.println(mp);
        } else if (m.payload instanceof SessionPayload) {
            String st = ((SessionPayload) m.payload).sessionToken;

            System.out.println("SESSION TOKEN: " + st);
            store.setSessionToken(st);
        } else if (m.payload instanceof ScoreboardPayload) {
            String sb = ((ScoreboardPayload) m.payload).scoreboard;

            Base64.Decoder decoder = Base64.getDecoder();

            try {
                String scoreboard = new String(decoder.decode(sb.getBytes()));
                System.out.println("\n" + scoreboard);
            } catch (Exception e) {
                System.out.println("[CLIENT] Error while trying to decode scoreboard base64!");
                e.printStackTrace();
            }
        } else if (m.payload instanceof ForbiddenPayload) {
            mp = ((ForbiddenPayload) m.payload).message;
            System.out.println(mp);
        } else if (m.payload instanceof BadRequestPayload) {
            mp = ((BadRequestPayload) m.payload).message;
            System.out.println(mp);
        } else if (m.payload instanceof NotFoundPayload) {
            mp = ((NotFoundPayload) m.payload).message;
            System.out.println(mp);
        } else if (m.payload instanceof TeamsPayload) {
            String sb = ((TeamsPayload) m.payload).message;

            Base64.Decoder decoder = Base64.getDecoder();

            try {
                String listTeams = new String(decoder.decode(sb.getBytes()));
                System.out.println("\n" + listTeams);
            } catch (Exception e) {
                System.out.println("[CLIENT] Error while trying to decode team list base64!");
                e.printStackTrace();
            }
        }
    }

    /////////////////////////
    // HANDLING USER INPUT //
    /////////////////////////

    /**
     * Calls out the assigned function depending on the
     * command submitted as input.
     *
     * @param input user input
     */
    public void processUserInput(String input) {
        String[] split = input.split(" ");

        switch (split[0]) {
            case ":register": processRegisterInput(split); break;
            case ":login": processLoginInput(split); break;
            case ":scoreboard": processScoreboardInput(); break;
            case ":help": help(); break;
            case ":ping": healthcheck(); break;
            case ":logout": logout(); break;
            case ":create": processCreateTeamInput(split); break;
            case ":join": joinTeam(split); break;
            case ":teams": teams(); break;
            case ":ready": ready(); break;
            default: System.out.println("Command is valid but not yet implemented!"); break;
        }
    }

    public void submitSentence(String sentence) {
        InfoPayload ip = new InfoPayload(sentence);
        Message m = new Message(false, 110, false, ip);

        this.messageBlockingQueue.add(m);
    }

    private void ready() {
        InfoPayload ip = new InfoPayload("ready");
        Message m = new Message(false, 110, false, ip);

        this.messageBlockingQueue.add(m);
    }

    private void teams() {
        TeamsPayload tp = new TeamsPayload("");
        Message m = new Message(false, 350, false, tp);

        this.messageBlockingQueue.add(m);
    }

    private void joinTeam(String[] input) {
        String teamname;
        String password;

        if (input.length == 3) {
            teamname= input[1];
            password = input[2];
        } else if (input.length == 2) {
            teamname = input[1];
            password = "";
        } else {
            System.out.println("Invalid join input! Use: :join <teamname> <password>");
            System.out.println("You can leave the password field empty if joining a public team!");
            return;
        }

        JoinTeamPayload jtp = new JoinTeamPayload(teamname, password);
        Message m = new Message(false, 100, false, jtp);

        this.messageBlockingQueue.add(m);
    }

    private void logout() {
        String payload = "";
        Message m = new Message(false, 370, false, payload);
        this.messageBlockingQueue.add(m);
    }

    private void healthcheck() {
        String ping = "ping";

        Message m = new Message(false, 360, false, ping);
        this.messageBlockingQueue.add(m);
    }

    private void processScoreboardInput() {
        ScoreboardPayload sp = new ScoreboardPayload("");
        Message m = new Message(false, 100, false, sp);
        this.messageBlockingQueue.add(m);
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
        this.messageBlockingQueue.add(m);
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

        Message m = new Message(false, 100, false, registerPayload);
        this.messageBlockingQueue.add(m);
    }

    private void processCreateTeamInput(String[] input) {
        if (input.length != 3) {
            System.out.println("Invalid input! Use: :create <teamname> <public OR private>");
            return;
        }

        boolean openToAll;

        if (input[2].equals("public")) {
            openToAll = true;
        } else if (input[2].equals("private")) {
            openToAll = false;
        } else {
            System.out.println("Invalid input! Use: :create <teamname> <public OR private>");
            return;
        }

        String teamname = input[1];

        CreateTeamPayload ctp = new CreateTeamPayload(teamname, openToAll);
        Message m = new Message(false, 100, false, ctp);

        this.messageBlockingQueue.add(m);
    }

    public boolean validCommand(String command) {
        String[] split = command.split(" ");

        return this.commands.containsKey(split[0]);
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
