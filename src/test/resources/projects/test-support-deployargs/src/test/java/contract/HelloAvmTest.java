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
import org.aion.avm.embed.AvmRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class HelloAvmTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    public static HelloAvmTestImpl helloAvmTest = new HelloAvmTestImpl(avmRule);

    @BeforeClass
    public static void deploy() {
        helloAvmTest.deploy(avmRule.getPreminedAccount(), new BigInteger[]{new BigInteger("433434343")});
    }

    @Test
    public void getString() {
        ResponseContext<String> response = helloAvmTest.getString();

        Assert.assertEquals("Hello AVM", response.getData());
    }

    @Test
    public void getInt() {
        Integer retVal = helloAvmTest.getInt().getData();
        Assert.assertEquals(2, retVal.intValue());
    }

    @Test
    public void getShort() {
        Short retVal = helloAvmTest.getShort().getData();
        Assert.assertEquals(1, retVal.shortValue());
    }

    @Test
    public void getIntArray() {
        int[] retVal = helloAvmTest.getIntArray().getData();
        Assert.assertEquals(1, retVal[0]);
        Assert.assertEquals(2, retVal[1]);
    }

    @Test
    public void getAddress() {
        Address retAddress = helloAvmTest.getDefaultAddress().getData();

        Assert.assertEquals(avmRule.getPreminedAccount(), retAddress);
    }

    @Test
    public void getBis() {
        ResponseContext<BigInteger[]> bis = helloAvmTest.getBis();
        Assert.assertEquals(new BigInteger[]{new BigInteger("433434343")}, bis.getData());
    }
}
