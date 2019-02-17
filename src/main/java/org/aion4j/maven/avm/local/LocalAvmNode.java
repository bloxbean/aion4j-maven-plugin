package org.aion4j.maven.avm.local;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.StorageWalker;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.*;
import org.aion4j.maven.avm.api.CallResponse;
import org.aion4j.maven.avm.api.DeployResponse;
import org.aion4j.maven.avm.exception.CallFailedException;
import org.aion4j.maven.avm.exception.DeploymentFailedException;
import org.aion4j.maven.avm.exception.LocalAVMException;
import org.aion4j.maven.avm.util.HexUtil;
import org.aion4j.maven.avm.util.MethodCallArgsUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocalAvmNode {

    private org.aion.vm.api.interfaces.Address defaultAddress; // = KernelInterfaceImpl.PREMINED_ADDRESS;
    Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private  VirtualMachine avm;
    private KernelInterfaceImpl kernel;

    private long energyLimit = 100000000; //TODO Needs to configured by the project
    private long energyPrice = 1L;  //TODO Needs to be configured by the project

    public LocalAvmNode(String storagePath, String senderAddress) {
        if(storagePath.isEmpty())
            throw new LocalAVMException("Storage path cannot be null for embedded Avm deployment");

        defaultAddress = AvmAddress.wrap(Helpers.hexStringToBytes(senderAddress));

        init(storagePath);
    }

    public void init(String storagePath) {
        verifyStorageExists(storagePath);
        File storagePathFile = new File(storagePath);
        kernel = new KernelInterfaceImpl(storagePathFile);

        //Open account
        if(kernel.getBalance(defaultAddress) == null || kernel.getBalance(defaultAddress) == BigInteger.ZERO) {
            kernel.createAccount(defaultAddress);
            kernel.adjustBalance(defaultAddress, BigInteger.valueOf(100000000000000L));

            System.out.println(String.format("Created default account %s with balance %s", defaultAddress, BigInteger.valueOf(100000000000000L) ));
        }

        avm = CommonAvmFactory.buildAvmInstance(kernel);
    }

    public DeployResponse deploy(String jarFilePath) throws DeploymentFailedException {
        return deploy(jarFilePath, null);
    }

    public DeployResponse deploy(String jarFilePath, String deployer) throws DeploymentFailedException {

        Address deployerAddress = null;

        if(deployer == null || deployer.isEmpty())
            deployerAddress = defaultAddress;
        else
            deployerAddress = AvmAddress.wrap(Helpers.hexStringToBytes(deployer));

        TransactionContext txContext = createDeployTransaction(jarFilePath, deployerAddress, BigInteger.ZERO);

        DeployResponse deployResponse = createDApp(txContext);

        return deployResponse;
    }

    public CallResponse call(String contract, String sender, String method, String argsString, BigInteger value) throws CallFailedException {

        Address contractAddress = AvmAddress.wrap(Helpers.hexStringToBytes(contract));

        Address senderAddress = null;

        if(sender == null || sender.isEmpty())
            senderAddress = defaultAddress;
        else
            senderAddress = AvmAddress.wrap(Helpers.hexStringToBytes(sender));

        Object[] args = null;
        try {
            args = MethodCallArgsUtil.parseMethodArgs(argsString);
        } catch (Exception e) {
            throw new CallFailedException("Method argument parsing error", e);
        }

        TransactionContext txContext = createCallTransaction(contractAddress, senderAddress, method, args, value, energyLimit, energyPrice);

        TransactionResult result = avm.run(new TransactionContext[]{txContext})[0].get();

        if(result.getResultCode().isSuccess()) {
            CallResponse response = new CallResponse();

            byte[] retData = result.getReturnData();

            if(retData != null) {
                try {
                    Object retObj = ABIDecoder.decodeOneObject(retData);
                    response.setData(retObj);
                } catch (Exception e) {
                    response.setData(HexUtil.bytesToHexString(retData));
                }
            } else {
                response.setData(null);
            }

            response.setEnergyUsed(((AvmTransactionResult) result).getEnergyUsed());
            response.setStatusMessage(result.getResultCode().toString());
            printExecutionLog(txContext);

            return response;
        } else {

            String resultData = Helpers.bytesToHexString(result.getReturnData());
            //failed.
            throw new CallFailedException(String.format("Dapp call failed. Code: %s, Reason: %s",
                    result.getResultCode().toString(), resultData));
        }
    }

    private void printExecutionLog(TransactionContext txContext) {
        //Logs
        List<IExecutionLog> executionLogs = txContext.getSideEffects().getExecutionLogs();
        if(executionLogs != null && executionLogs.size() > 0) {
            System.out.println("************************ Execution Logs ****************************");

            executionLogs.forEach(exLog -> {
                System.out.println("Hex Data: " + HexUtil.bytesToHexString(exLog.getData()));

                if(exLog.getTopics() != null) {
                    List<byte[]> topics = exLog.getTopics();

                    if(topics != null) {
                        for(byte[] topic: topics) {
                            System.out.println("Topic: " + HexUtil.bytesToHexString(topic));
                        }
                    }
                }
                System.out.println("  ");
            });

            System.out.println("************************ Execution Logs ****************************\n");
        }
    }

    private DeployResponse createDApp(TransactionContext txContext) throws DeploymentFailedException {

        TransactionResult result = avm.run(new TransactionContext[] {txContext})[0].get();

        if(result.getResultCode().isSuccess()) {
            DeployResponse deployResponse = new DeployResponse();

            String dappAddress = Helpers.bytesToHexString(result.getReturnData());

            deployResponse.setAddress(dappAddress);
            deployResponse.setEnergyUsed(((AvmTransactionResult) result).getEnergyUsed());
            deployResponse.setStatusMessage(result.getResultCode().toString());

            return deployResponse;
        } else {

            String resultData = Helpers.bytesToHexString(result.getReturnData());
            //failed.
            throw new DeploymentFailedException(String.format("Dapp deployment failed. Code: %s, Reason: %s",
                result.getResultCode().toString(), resultData));
        }
    }

    private TransactionContext createDeployTransaction(String jarPath, Address sender, BigInteger value)
        throws DeploymentFailedException {

        Path path = Paths.get(jarPath);
        byte[] jar;
        try {
            jar = Files.readAllBytes(path);
        }catch (IOException e){
            throw new DeploymentFailedException("deploy : Invalid location of Dapp jar");
        }

        Transaction createTransaction = Transaction.create(sender, kernel.getNonce(sender),
            value, new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);

        return new TransactionContextImpl(createTransaction, block);

    }

    public TransactionContext createCallTransaction(Address contract, Address sender, String method, Object[] args,
                                                           BigInteger value, long energyLimit, long energyPrice) {

        /*if (contract.toBytes().length != Address.LENGTH){
            throw env.fail("call : Invalid Dapp address ");
        }

        if (sender.toBytes().length != Address.LENGTH){
            throw env.fail("call : Invalid sender address");
        }*/

        byte[] arguments = ABIEncoder.encodeMethodArguments(method, args);

//        String callData = Helpers.bytesToHexString(arguments);
//        System.out.println("******** Call data: " + callData);
        BigInteger biasedNonce = kernel.getNonce(sender);//.add(BigInteger.valueOf(nonceBias));
        Transaction callTransaction = Transaction.call(sender, contract, biasedNonce, value, arguments, energyLimit, energyPrice);
        return new TransactionContextImpl(callTransaction, block);

    }

    public boolean createAccountWithBalance(String address, BigInteger balance) {

        Address account = AvmAddress.wrap(Helpers.hexStringToBytes(address));

        //Open account
        if(kernel.getBalance(account) == null || kernel.getBalance(account) == BigInteger.ZERO) {
            kernel.createAccount(account);
            kernel.adjustBalance(account, balance);

            System.out.println(String.format("Create account %s with balance %d", address, balance.longValue()));
            return true;
        } else {
            System.out.println("Account already exists");
            return false;
        }
    }

    public BigInteger getBalance(String address) {

        Address account = AvmAddress.wrap(Helpers.hexStringToBytes(address));

        BigInteger balance = kernel.getBalance(account);

        if(balance == null)
            return BigInteger.ZERO;
        else
            return balance;
    }

    public void explore(String dappAddress, PrintStream printStream) throws Exception {

        try {
            StorageWalker.walkAllStaticsForDapp(printStream, kernel, AvmAddress.wrap(HexUtil.hexStringToBytes(dappAddress)));
        } catch (Exception ex) {
            throw new RuntimeException("Unable to explore storage for dApp : " + dappAddress, ex);
        }
    }

    //Called for remote
    public static String getBytesForDeploy(String dappJarPath) {
        try {
            Path path = Paths.get(dappJarPath);
            byte[] jar = Files.readAllBytes(path);
            return Helpers.bytesToHexString(
                    new CodeAndArguments(jar, new byte[0]).encodeToBytes());
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    //called from remote impl to encode method call args
    public static String encodeMethodCall(String method, Object[] args) {
        return Helpers.bytesToHexString(ABIEncoder.encodeMethodArguments(method, args));
    }

    //called for remote impl to decode hexstring to object
    public static Object decodeResult(String hex) {
        try {
            return ABIDecoder.decodeOneObject(HexUtil.hexStringToBytes(hex));
        } catch (Exception e) {
            return null;
        }
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
