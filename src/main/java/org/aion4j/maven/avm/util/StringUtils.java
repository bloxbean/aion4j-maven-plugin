package org.aion4j.maven.avm.util;

public class StringUtils {

    public static boolean isEmpty(String str) {
        if(str == null || str.trim().isEmpty())
            return true;
        else
            return false;
    }
}
