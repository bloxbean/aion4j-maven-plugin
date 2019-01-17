package org.aion4j.maven.avm.local;

import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.aion4j.maven.avm.api.DeployResponse;
import org.aion4j.maven.avm.exception.DeploymentFailedException;
import org.aion4j.maven.avm.exception.LocalAVMException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalAvmNode {

    private org.aion.vm.api.interfaces.Address defaultDeployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private  VirtualMachine avm;
    private KernelInterfaceImpl kernel;

    private long energyLimit = 100000000; //TODO Needs to configured by the project
//    private long energyPrice = 11;  //TODO Needs to be configured by the project

    public LocalAvmNode() {
        connect("storage");
    }

    public void connect(String storagePath) {
        verifyStorageExists(storagePath);
        File storagePathFile = new File(storagePath);
        kernel = new KernelInterfaceImpl(storagePathFile);

        //Open account
        if(kernel.getBalance(defaultDeployer) == null || kernel.getBalance(defaultDeployer) == BigInteger.ZERO) {
            kernel.createAccount(defaultDeployer);
            kernel.adjustBalance(defaultDeployer, BigInteger.valueOf(100000000000000L));

            System.out.println("Create default account");
        }

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

    private static void verifyStorageExists(String storageRoot) {
        File directory = new File(storageRoot);
        if (!directory.isDirectory()) {
            boolean didCreate = directory.mkdirs();
            // Is this the best way to handle this failure?
            if (!didCreate) {
               throw new LocalAVMException("Unable create storage folder");
            }
        }
    }

    public void shutdown() {
        avm.shutdown();
    }

}
