package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.exception.CallFailedException;
import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.aion4j.maven.avm.util.DeployResultConfig;
import org.aion4j.maven.avm.util.MethodCallArgsUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Method;
import java.math.BigInteger;

@Mojo(name = "call", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMCallMajo extends AVMLocalRuntimeBaseMojo {

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

        contract = ConfigUtil.getPropery("contract");
        sender = ConfigUtil.getPropery("address");
        method = ConfigUtil.getPropery("method");
        methodArgs = ConfigUtil.getPropery("args");
        value = ConfigUtil.getPropery("value");

        if(contract == null || contract.isEmpty()) {

            if(isLocal()) {
                String lastDeployAddress = DeployResultConfig.getLastDeployedAddress(project.getName(), getStoragePath());

                if (lastDeployAddress == null || lastDeployAddress.isEmpty()) {
                    getLog().error("Contract address is missing. You need to deploy the contract first using aion4j:deploy." +
                            "\n Also you can pass the contract address from commandline.");
                    printHelp();
                    throw new MojoExecutionException("Contract address is missing");
                } else {
                    contract = lastDeployAddress;
                }
            } else {
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

        BigInteger valueB = null;
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

        String web3RpcUrl = getWeb3RpcUrl();

        try {
            Class localAvmClazz = getLocalAVMClass();
            //Lets do method call encoding

            Method enocodeCallMethod = localAvmClazz.getMethod("encodeMethodCall", String.class, Object[].class);

            //Parse the commandline args
            Object[] args = null;
            try {
                args = MethodCallArgsUtil.parseMethodArgs(methodArgs);
            } catch (Exception e) {
                throw new CallFailedException("Method argument parsing error", e);
            }

            String encodedMethodCall = (String)enocodeCallMethod.invoke(null, method, args);

            //getLog().info("Encoded method call data: " + encodedMethodCall);

            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

            String retData = remoteAVMNode.call(contract, sender, encodedMethodCall, valueB, 2000000, 100000000000L);

            getLog().info("****************  Method call result  ****************");
            getLog().info("Data       :" + retData);
            getLog().info("******************************************************");

        } catch (Exception ex) {
            getLog()
                    .error(String.format("Method call failed"),
                            ex);
            throw new MojoExecutionException("Method call failed", ex);
        }

    }
}
