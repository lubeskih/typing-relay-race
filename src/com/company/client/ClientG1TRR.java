package com.company.client;

// CODE    MESSAGE                     TRANSLATED                          USED BY    AUTH
// =======================================================================================
// 100     JUST DONT YELL              OK                                  BOTH       NO
//
// 200     I BROKE MY ANKLE            Internal Server Error               SERVER     NO
// 210     I DONT SPEAK FRENCH         Bad Request                         SERVER     NO
// 230     GHOSTS NOT ALLOWED          Not Authenticated                   SERVER     NO
// 240     MISTER ANDERSON             Already Authenticated               SERVER     NO
// 250     FOR YOUR INFORMATION        Server Providing Information        SERVER     NO
// 260     SMASH THAT KEYBOARD         Start Typing; Server is Counting    SERVER     NO
//
// 300     LET ME IN                   Login                               CLIENT     NO
// 310     SIGN ME UP                  Register                            CLIENT     NO
// 320     NERD LIST                   List Scoreboard                     CLIENT     YES
// 330     I DAB ON ALL YOU NERDS      Create a Team                       CLIENT     YES
// 340     LET ME HELP                 Join a Team                         CLIENT     YES
// 350     SHOW YASELF                 List Online Teams (only 1/2 joined) CLIENT     YES
// =======================================================================================

import com.company.Shared.Message;

import java.util.concurrent.BlockingQueue;

class G1TRRMESSAGE {
    public int replyCodeNumber;
    public String replyCodeMessage;
    public boolean requiresAuthentication;

    public G1TRRMESSAGE(int replyCodeNumber, String replyCodeMessage, boolean requiresAuthentication) {
        this.replyCodeNumber = replyCodeNumber;
        this.replyCodeMessage = replyCodeMessage;
        this.requiresAuthentication = requiresAuthentication;
    }
}

public class ClientG1TRR {
    public G1TRRMESSAGE JUST_DONT_YELL = new G1TRRMESSAGE(100, "JUST DON'T YELL", false);

    public G1TRRMESSAGE I_BROKE_MY_ANKLE = new G1TRRMESSAGE(200, "I BROKE MY ANKLE", false);
    public G1TRRMESSAGE I_DONT_SPEAK_FRENCH = new G1TRRMESSAGE(210, "I DON'T SPEAK FRENCH", false);
    public G1TRRMESSAGE GHOST_NOT_ALLOWED = new G1TRRMESSAGE(230, "GHOSTS NOT ALLOWED", false);
    public G1TRRMESSAGE MISTER_ANDERSON = new G1TRRMESSAGE(240, "MISTER ANDERSON", false);
    public G1TRRMESSAGE FOR_YOUR_INFORMATION = new G1TRRMESSAGE(250, "FOR YOUR INFORMATION", false);

    public G1TRRMESSAGE LET_ME_IN = new G1TRRMESSAGE(300, "LET ME IN", false);
    public G1TRRMESSAGE SIGN_ME_UP = new G1TRRMESSAGE(310, "SIGN ME UP", false);

    public G1TRRMESSAGE NERD_LIST = new G1TRRMESSAGE(320, "NERD LIST", true);
    public G1TRRMESSAGE I_DAB_ON_ALL_YOU_NERDS = new G1TRRMESSAGE(330,"I DAB ON ALL YOU NERDS", true);
    public G1TRRMESSAGE LET_ME_HELP = new G1TRRMESSAGE(340, "LET ME HELP", true);
    public G1TRRMESSAGE SHOW_YASELF = new G1TRRMESSAGE(350, "SHOW YASELF", true);

    public BlockingQueue<Message> bq;

    public ClientG1TRR(BlockingQueue<Message> bq) {
        this.bq = bq;
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
            case 300: P300(); break;
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

    private void P300() {
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

        System.out.println("Received " + m.reply + " from server, which translates to " + JUST_DONT_YELL.replyCodeMessage);
        System.out.println("Server says: " + m.payload);

        String replyMessage = "Good.";

        Message output = new Message(true, 100, false, replyMessage);

        this.bq.add(output);
        System.out.println("Response added in BQ!");
    }
}
