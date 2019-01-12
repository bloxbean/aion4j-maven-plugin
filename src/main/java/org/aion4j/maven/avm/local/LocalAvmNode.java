package org.aion4j.maven.avm.local;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionInterface;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.aion4j.maven.avm.api.DeployResponse;
import org.aion4j.maven.avm.exception.DeploymentFailedException;

public class LocalAvmNode {

    private org.aion.vm.api.interfaces.Address defaultDeployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);


    private  VirtualMachine avm;
    private KernelInterfaceImpl kernel;

    private long energyLimit = 1_000_000l; //TODO Needs to configured by the project
    private long enertyPrice = 11;  //TODO Needs to be configured by the project

    public LocalAvmNode() {
        connect();
    }

    public void connect() {
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        avm = CommonAvmFactory.buildAvmInstance(kernel);
    }

    public DeployResponse deploy(String jarFilePath) throws DeploymentFailedException {
        Transaction tx = createDeployTransaction(jarFilePath, defaultDeployer, BigInteger.ZERO);

        DeployResponse deployResponse = createDApp(tx);

        return deployResponse;
    }

    private DeployResponse createDApp(Transaction tx) throws DeploymentFailedException {

        TransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx, block)})[0].get();

        if(result1.getResultCode().isSuccess()) {
            DeployResponse deployResponse = new DeployResponse();

            String dappAddress = Helpers.bytesToHexString(result1.getReturnData());

            deployResponse.setAddress(dappAddress);
            deployResponse.setEnergyUsed(((AvmTransactionResult) result1).getEnergyUsed());
            deployResponse.setStatusMessage(result1.getResultCode().toString());

            return deployResponse;
        } else {

            String resultData = Helpers.bytesToHexString(result1.getReturnData());
            //failed.
            throw new DeploymentFailedException(String.format("Dapp deployment failed. Code: %s, Reason: %s",
                result1.getResultCode().toString(), resultData));
        }
    }

    private Transaction createDeployTransaction(String jarPath, Address sender, BigInteger value)
        throws DeploymentFailedException {

        Path path = Paths.get(jarPath);
        byte[] jar;
        try {
            jar = Files.readAllBytes(path);
        }catch (IOException e){
            throw new DeploymentFailedException("deploy : Invalid location of Dapp jar");
        }

        Transaction createTransaction = Transaction.create(sender, kernel.getNonce(sender).longValue(),
            value, new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, 1L);

        return createTransaction;

    }

    public void shutdown() {
        avm.shutdown();
    }

}
