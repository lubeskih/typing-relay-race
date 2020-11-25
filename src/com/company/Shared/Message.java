package com.company.Shared;

public class Message implements java.io.Serializable {
    public boolean isResponse;
    public int reply;
    public boolean isError;
    public Object payload;

    public Message (boolean isResponse, int reply, boolean isError, Object payload) {
        this.isResponse = isResponse;
        this.reply = reply;
        this.isError = isError;
        this.payload = payload;
    }
}