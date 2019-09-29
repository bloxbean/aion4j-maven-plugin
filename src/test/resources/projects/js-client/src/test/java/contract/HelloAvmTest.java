package contract;

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
        helloAvmTest.deploy();
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
}
