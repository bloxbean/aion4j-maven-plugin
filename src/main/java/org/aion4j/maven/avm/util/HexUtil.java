package org.aion4j.maven.avm.util;

public class HexUtil {

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Converts byte array into its hex string representation.
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes) {
        if (null == bytes){
            return "void";
        }

        int length = bytes.length;

        char[] hexChars = new char[length * 2];
        for (int i = 0; i < length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts hex string into its byte[] representation.
     *
     * @param s
     * @return
     */
    public static byte[] hexStringToBytes(String s) {
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
