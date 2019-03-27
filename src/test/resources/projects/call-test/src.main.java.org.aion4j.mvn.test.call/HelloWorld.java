package org.aion4j.mvn.test.call;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld
{
    private static String owner;
    static {
        owner = (String) ABIDecoder.decodeOneObject(BlockchainRuntime.getData());
    }

    @Callable
    public static void sayHello() {
        BlockchainRuntime.println("Hello Avm");
    }

    @Callable
    public static void owner() {
        BlockchainRuntime.println("Owner name >> " + owner);
    }

    @Callable
    public static String[] test2Array(String[] sarr, Address[] aarr, int[] iarr) {
        return sarr;
    }

    @Callable
    public static int[] testIntArray(int[] iarr) {
        return iarr;
    }

    @Callable
    public static String[] testArray(String[] sarr, int[] iarr, String name, Address[] aadd, short[] shorts, float[] floats, double[] doubles, boolean[] bo, byte[] b) {

        BlockchainRuntime.println("String ---");
        for(String s: sarr) {
            BlockchainRuntime.println(s);
        }

        BlockchainRuntime.println("Ints ---");
        for(int i: iarr) {
            BlockchainRuntime.println(i + "");
        }

        BlockchainRuntime.println(name);

        BlockchainRuntime.println("Address ---");

        for(Address add: aadd) {
            BlockchainRuntime.println(add.toString());
        }

        return sarr;

    }

    @Callable
    public static String[] testArray(String[] sarr) {
        return sarr;
    }

    @Callable
    public static String greet(String name) {
        return "Hello " + name;
    }

    @Callable
    public static String[] getTopics() {
        String[] topics = new String[] {"topic1", "topic2", "topic3"};

        return topics;
    }

    @Callable
    public static int[] getTopicsInt() {
        int[] topics = new int[] {1,2,3};

        return topics;
    }

    @Callable
    public static long[] getTopicsLong() {
        long[] topics = new long[] {1,2,3};

        return topics;
    }

}
