package org.aion4j.maven.avm.it;

import junit.framework.TestCase;

public class BaseTestHelper extends TestCase {

    private static final String PLUGIN_VERSION = System.getProperty("plugin.version");

    public String getPluginVersion() {
        return PLUGIN_VERSION;
    }
}
