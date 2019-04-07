package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.api.Log;
import org.aion4j.avm.helper.exception.CallFailedException;
import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.avm.helper.util.MethodCallArgsUtil;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Method;
import java.math.BigInteger;

@Mojo(name = "call", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMCallMajo extends AVMLocalRuntimeBaseMojo {

    private long defaultGas = 2000000;
    private long defaultGasPrice = 100000000000L;

    private String contract;
    private String method;
    private String sender;
    private String methodArgs;
    private String value;
    private BigInteger valueB; //used in the txn

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {
        //don nothing
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            final Method callMethod = localAvmInstance.getClass()
                    .getMethod("call", String.class, String.class, String.class, String.class, BigInteger.class);

            parseArgs();

            getLog().info(String.format("Calling contract method ..."));


            Object response = callMethod.invoke(localAvmInstance, contract, sender, method, methodArgs, valueB);

            Method getDataMethod = response.getClass().getMethod("getData");
            Method getEnergyUsed = response.getClass().getMethod("getEnergyUsed");

            Object data = getDataMethod.invoke(response);

            getLog().info("****************  Method call result  ****************");
            getLog().info("Data       : " + data);
            getLog().info("Energy used: " + getEnergyUsed.invoke(response));
            getLog().info("*********************************************************");

            getLog()
                    .info(String.format("Method call successful"));

        } catch (Exception ex) {
            getLog()
                    .error(String.format("Method call failed"),
                            ex);
            throw new MojoExecutionException("Method call failed", ex);
        }

    }

    private void parseArgs() throws MojoExecutionException {
        final Object[] args = new Object[5];

        contract = ConfigUtil.getProperty("contract");
        sender = ConfigUtil.getProperty("address");
        method = ConfigUtil.getProperty("method");
        methodArgs = ConfigUtil.getProperty("args");
        value = ConfigUtil.getProperty("value");

        if(contract == null || contract.isEmpty()) {

            String lastDeployAddress = getCache().getLastDeployedAddress();

            if (lastDeployAddress == null || lastDeployAddress.isEmpty()) {
                getLog().error("Contract address is missing. You need to deploy the contract first using aion4j:deploy." +
                        "\n Also you can pass the contract address from commandline.");
                printHelp();
                throw new MojoExecutionException("Contract address is missing");
            } else {
                contract = lastDeployAddress;
            }

            if (lastDeployAddress == null || lastDeployAddress.isEmpty()) {
                printHelp();
            }
        }

        if(method == null || method.isEmpty()) {
            getLog().error("Method name is missing");
            printHelp();
            throw new MojoExecutionException("Method name is missing");
        }

        getLog().info("Contract Address : " + contract);

        if(sender != null)
            getLog().info("Sender Address   : " + sender);

        getLog().info("Method           : " + method);
        getLog().info("Arguments        : " + methodArgs);

        if(value == null || value.isEmpty())
            valueB = BigInteger.ZERO;
        else {
            valueB = new BigInteger(value.trim());
        }
    }


    private void printHelp() {
        getLog().info("Usage:");
        getLog().info("./mvnw  aion4j:call [-Dcontract=<contract_address>] [-Daddress=<sender_address>]  -Dmethod=<method_name> [-Dvalue=<value>] [-Dargs=<method_args>]");
        getLog().info("Example:");
        getLog().info("./mvnw aion4j:call -Dcontract=0x1122334455667788112233445566778811223344556677881122334455667788 -Daddress=0xa003ddd...  -Dmethod=transfer -Dargs=\"-A 0x1122334455667788112233445566778811223344556677881122334455667788 -J 100\"\n");
    }

    @Override
    protected void executeRemote() throws MojoExecutionException {
        parseArgs();

        String web3RpcUrl = resolveWeb3rpcUrl();

        //Get gas & gas price
        long gas = getGas();
        if(gas == 0)
            gas = defaultGas;

        long gasPrice = getGasPrice();
        if(gasPrice == 0)
            gasPrice = defaultGasPrice;

        try {
            Class localAvmClazz = getLocalAVMClass();
            //Lets do method call encoding

            Method enocodeCallMethodWithArgsStr = localAvmClazz.getMethod("encodeMethodCallWithArgsString", String.class, String.class);

            String encodedMethodCall = (String)enocodeCallMethodWithArgsStr.invoke(null, method, methodArgs);

            //getLog().info("Encoded method call data: " + encodedMethodCall);

            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, MavenLog.getLog(getLog()));

            String retData = remoteAVMNode.call(contract, sender, encodedMethodCall, valueB, gas, gasPrice);

            Object decodedValue = decodeCallResultForRemote(retData, localAvmClazz);

            getLog().info("****************  Method call result  ****************");
            getLog().info("Data          :" + retData);

            if(decodedValue != null)
                getLog().info("Decoded value :" + decodedValue);

            getLog().info("******************************************************");

        } catch (Exception ex) {
            getLog()
                    .error(String.format("Method call failed"),
                            ex);
            throw new MojoExecutionException("Method call failed", ex);
        }

    }

    private Object decodeCallResultForRemote(String retData, Class localAvmClazz) {
        if(retData == null || retData.isEmpty())
            return null;

        try {
            Method decodeResultMethod = localAvmClazz.getMethod("decodeResult", String.class);
            Object decodedValue = decodeResultMethod.invoke(null, retData);

            return decodedValue;

        } catch (Exception e) {
            getLog().debug("Result decoding failed for data:  " + retData, e);
            return null;
        }
    }

}
