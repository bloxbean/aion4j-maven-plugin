/*
 * MIT License
 *
 * Copyright (c) 2019 BloxBean Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package contract;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Initializable;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;

import java.math.BigInteger;

public class HelloAvm
{
    @Initializable
    private static Address address;

    @Initializable
    private static BigInteger[] values;

    private static String myStr = "Hello AVM";

    private static AionSet aionSet = new AionSet();
    private static AionMap aionMap = new AionMap<>();

    @Callable
    public static Address getDefaultAddress() {
        return address;
    }

    @Callable
    public static BigInteger[] getBis() {
        return values;
    }

    @Callable
    public static void sayHello() {
        aionMap.put("1", "hello");
        Blockchain.println("Hello Avm" + aionMap.toString());
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
    public static avm.Address getAddress() {
        return new Address("0xa0d98785eba1e858a79f75f56c496cb62d2a19430670271db053894be34fe995".getBytes());
    }

    @Callable
    public static int[] getIntArray() {
        return new int[] {1,2};
    }

}
