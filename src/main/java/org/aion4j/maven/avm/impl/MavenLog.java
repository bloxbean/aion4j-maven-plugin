package org.aion4j.maven.avm.impl;

import org.aion4j.avm.helper.api.Log;

public class MavenLog implements Log {

    org.apache.maven.plugin.logging.Log log;
    public MavenLog(org.apache.maven.plugin.logging.Log log) {
        this.log = log;
    }
    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }

    @Override
    public void info(String s, Throwable t) {
        log.info(s, t);
    }

    @Override
    public void debug(String s, Throwable t) {
        log.debug(s, t);
    }

    @Override
    public void error(String msg, Throwable t) {
        log.error(msg, t);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        log.warn(s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public static MavenLog getLog(org.apache.maven.plugin.logging.Log log) {
        return new MavenLog(log);
    }
}
