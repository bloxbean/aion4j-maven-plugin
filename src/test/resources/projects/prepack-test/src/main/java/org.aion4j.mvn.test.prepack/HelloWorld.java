package org.aion4j.mvn.test.prepack;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.tooling.abi.Callable;
import avm.Blockchain;

public class HelloWorld {

    @Callable
    public static void sayHello() {
        Blockchain.println("Hello world");
    }

}