package contract;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.tooling.abi.Callable;
import avm.Address;
import avm.BlockchainRuntime;

public class HelloWorld
{
    private static String owner;
    static {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        owner = decoder.decodeOneString();
    }

    @Callable
    public static String getOwner() {
        return owner;
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
    public static int[] testIntArray1(int[] iarr) {
        return iarr;
    }

    @Callable
    public static String[] testArray2(String[] sarr, int[] iarr, String name, Address[] aadd, short[] shorts, float[] floats, double[] doubles, boolean[] bo, byte[] b) {

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
    public static String[] testArray3(String[] sarr) {
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
