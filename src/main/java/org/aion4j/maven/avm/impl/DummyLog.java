package org.aion4j.maven.avm.impl;

import org.aion4j.avm.helper.api.Log;

public class DummyLog implements Log {

    @Override
    public void info(String s) {

    }

    @Override
    public void debug(String s) {

    }

    @Override
    public void info(String s, Throwable throwable) {

    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public void error(String s, Throwable throwable) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }
}
