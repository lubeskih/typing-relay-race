package com.company.shared;

public abstract class ProtocolDictionary {
    private final String RC100 = "JUST DON'T YELL";

    private final String RC200 = "I BROKE MY ANKLE";
    private final String RC210 = "I DON'T SPEAK FRENCH";
    private final String RC230 = "GHOSTS NOT ALLOWED";
    private final String RC240 = "MISTER ANDERSON";
    private final String RC250 =  "FOR YOUR INFORMATION";

    private final String RC300 = "LET ME IN";
    private final String RC310 = "SIGN ME UP";

    private final String RC320 = "NERD LIST";
    private final String RC330 = "I DAB ON ALL YOU NERDS";
    private final String RC340 = "LET ME HELP";
    private final String RC350 = "SHOW YASELF";
    private final String RC370 = "IM OUT";

    public String translateReplyCode(int replyCode) {
        switch(replyCode) {
            case 100: return this.RC100;
            case 200: return this.RC200;
            case 210: return this.RC210;
            case 230: return this.RC230;
            case 240: return this.RC240;
            case 250: return this.RC250;
            case 300: return this.RC300;
            case 310: return this.RC310;
            case 320: return this.RC320;
            case 330: return this.RC330;
            case 340: return this.RC340;
            case 350: return this.RC350;
            case 370: return this.RC370;
            default: return "Couldn't match message!";
        }
    }
}