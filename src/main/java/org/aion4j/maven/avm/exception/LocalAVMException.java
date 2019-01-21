package org.aion4j.maven.avm.exception;

public class LocalAVMException extends RuntimeException {

    public LocalAVMException(String msg) {
        super(msg);
    }

    public LocalAVMException(Exception e) {
        super(e);
    }
}
