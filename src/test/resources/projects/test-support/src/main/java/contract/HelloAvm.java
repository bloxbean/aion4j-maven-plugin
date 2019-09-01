package contract;

import avm.Blockchain;
import avm.Address;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;

public class HelloAvm
{
    private static String myStr = "Hello AVM";

    @Callable
    public static void sayHello() {
        Blockchain.println("Hello Avm");
    }

    @Callable
    public static String greet(String name) {
        return "Hello " + name;
    }

    @Callable
    public static String getString() {
        Blockchain.println("Current string is " + myStr);
        return myStr;
    }

    @Callable
    public static void setString(String newStr) {
        myStr = newStr;
        Blockchain.println("New string is " + myStr);
    }

    @Callable
    public static byte getByte() {
        return (byte)1;
    }

    @Callable
    public static boolean getBoolean() {
        return true;
    }

    @Callable
    public static char getChar() {
        return 'c';
    }

    @Callable
    public static short getShort() {
        return 1;
    }

    @Callable
    public static int getInt() {
        return 2;
    }

    @Callable
    public static float getFloat() {
        return 3.3f;
    }

    @Callable
    public static long getLong() {
        return 4000000L;
    }

    @Callable
    public static double getDouble() {
        return 4.5d;
    }

    @Callable
    public static Address getAddress() {
        return new Address("0xa0d98785eba1e858a79f75f56c496cb62d2a19430670271db053894be34fe995".getBytes());
    }

    @Callable
    public static int[] getIntArray() {
        return new int[] {1,2};
    }
}
