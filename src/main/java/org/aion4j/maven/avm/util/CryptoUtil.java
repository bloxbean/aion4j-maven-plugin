package org.aion4j.maven.avm.util;

import java.math.BigInteger;

public class CryptoUtil {

    public static Double convertAmpToAion(BigInteger value) {
        if(value != null) {
            return (double) (value.doubleValue() / Math.pow(10, 18));
        } else {
            return 0d;
        }
    }
}
