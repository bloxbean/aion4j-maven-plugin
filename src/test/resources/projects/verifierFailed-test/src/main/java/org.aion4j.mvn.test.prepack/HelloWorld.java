package org.aion4j.mvn.test.prepack;

import org.aion.avm.tooling.abi.Callable;
import avm.Blockchain;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import java.io.File;

public class HelloWorld {

    @Callable
    public static void sayHello() {
        AionList list = new AionList();
        AionMap map = new AionMap<>();
        File file = new File("test");
        Blockchain.println("Hello world");
    }

}