package org.aion4j.maven.avm.exception;

public class CallFailedException extends Exception {

    public CallFailedException(String msg) {
        super(msg);
    }

    public CallFailedException(String msg, Exception ex) {
        super(msg, ex);
    }
}
