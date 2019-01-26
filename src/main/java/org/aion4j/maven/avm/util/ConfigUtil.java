package org.aion4j.maven.avm.util;

public class ConfigUtil {

    public static String getPropery(String name) {

        String value = System.getProperty(name);

        if(value != null && !value.isEmpty())
            return value;
        else {
            name = name.replace(".", "_");
            return System.getenv(name);
        }
    }

}
