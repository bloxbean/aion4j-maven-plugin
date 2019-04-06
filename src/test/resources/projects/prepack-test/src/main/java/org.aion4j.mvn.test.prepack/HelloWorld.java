package org.aion4j.mvn.test.prepack;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.tooling.abi.Callable;
import avm.BlockchainRuntime;

public class HelloWorld {

    @Callable
    public static void sayHello() {
        BlockchainRuntime.println("Hello world");
    }

}