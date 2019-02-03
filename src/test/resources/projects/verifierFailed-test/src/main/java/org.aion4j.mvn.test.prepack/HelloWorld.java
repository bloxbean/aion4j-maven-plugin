package org.aion4j.mvn.test.prepack;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import java.io.File;

public class HelloWorld {

    public static void sayHello() {
        AionList list = new AionList();
        AionMap map = new AionMap<>();
        File file = new File("test");
        BlockchainRuntime.println("Hello world");
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(HelloWorld.class, BlockchainRuntime.getData());
    }
}