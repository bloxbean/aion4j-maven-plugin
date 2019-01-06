package org.aion4j.mvn.test.prepack;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld {

    public static void sayHello() {
        BlockchainRuntime.println("Hello world");
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(HelloWorld.class, BlockchainRuntime.getData());
    }
}