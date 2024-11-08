package org.avasthi.java.cli;

public class BhavcopyNotFound extends Exception{
    public BhavcopyNotFound(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    private int code;
}
