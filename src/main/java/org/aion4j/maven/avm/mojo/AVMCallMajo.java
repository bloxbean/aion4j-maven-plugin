package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.util.DeployResultConfig;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Method;
import java.math.BigInteger;

@Mojo(name = "call", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMCallMajo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecute() throws MojoExecutionException {
        //don nothing
    }

    @Override
    protected void execute(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            final Method callMethod = localAvmInstance.getClass()
                    .getMethod("call", String.class, String.class, String.class, String.class, BigInteger.class);

            final Object[] args = new Object[5];

            String contract = System.getProperty("contract");
            String sender = System.getProperty("sender");
            String method = System.getProperty("method");
            String methodArgs = System.getProperty("args");
            String value = System.getProperty("value");

            if(contract == null || contract.isEmpty()) {

                String lastDeployAddress = DeployResultConfig.getLastDeployedAddress(getStoragePath());

                if(lastDeployAddress == null || lastDeployAddress.isEmpty()) {
                    getLog().error("Contract address is missing. You need to deploy the contract first using aion4j:deploy." +
                            "\n Also you can pass the contract address from commandline.");
                    printHelp();
                    throw new MojoExecutionException("Contract address is missing");
                } else {
                    contract = lastDeployAddress;
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
                    .info(String.format("%s deployed successfully to the embedded AVM.", getDappJar()));

        } catch (Exception ex) {
            getLog()
                    .error(String.format("Method call failed"),
                            ex);
            throw new MojoExecutionException("Method call failed", ex);
        }

    }

    private void printHelp() {
        getLog().info("Usage:");
        getLog().info("./mvnw  aion4j:call [-Dcontract=<contract_address>] [-Dsender=<sender_address>]  -Dmethod=<method_name> [-Dvalue=<value>] [-Dargs=<method_args>]");
        getLog().info("Example:");
        getLog().info("./mvnw aion4j:call -Dcontract=0x1122334455667788112233445566778811223344556677881122334455667788 -Dsender=0xa003ddd...  -Dmethod=transfer -Dargs=\"-A 0x1122334455667788112233445566778811223344556677881122334455667788 -J 100\"\n");
    }
}
