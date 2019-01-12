package org.aion4j.maven.avm.exception;

public class DeploymentFailedException extends Exception {

    public DeploymentFailedException(String msg) {
        super(msg);
    }

    public DeploymentFailedException(String msg, Exception ex) {
        super(msg, ex);
    }

}
