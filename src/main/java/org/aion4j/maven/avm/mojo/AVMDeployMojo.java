package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.Maven;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMDeployMojo extends AVMLocalRuntimeBaseMojo {

    //Needed for remote
    private long defaultGas = 5000000;
    private long defaultGasPrice = 100000000000L;
    private String deployArgs;
    private String value;
    private BigInteger valueB;

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException{

    }

    private boolean validateDappJar() throws MojoExecutionException {
        //check if dAppJar exists
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            MavenProject project = (MavenProject)getPluginContext().get("project");
            if(project.getModules().size() > 0) {
                return false;
            } else {
                throw new MojoExecutionException(String.format("Contract jar file doesn't exist : %s \n"
                        + "Please make sure you have built the project.", dappJar));
            }
        }

        return true;
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            if(!validateDappJar())
                return;

            String deployer = getAddress();

            //parse other command line args
            parseArgs();

            final Method deployMethod = localAvmInstance.getClass().getMethod("deploy", String.class, String.class, String.class);

            final Object[] args = new Object[2];
            args[0] = new String[]{dappJar};
            args[1] = deployer;

            getLog().info(String.format("Deploying %s to the embedded Avm ...", getDappJar()));

            if(deployer != null && !deployer.trim().isEmpty())
                getLog().info("Deployer address : " + deployer);
            else
                deployer = getLocalDefaultAddress();

            getLog().info("Avm storage path : " + getStoragePath());

            Object response = deployMethod.invoke(localAvmInstance, dappJar, deployArgs, deployer);

            Method getAddressMethod = response.getClass().getMethod("getAddress");
            Method getEnergyUsed = response.getClass().getMethod("getEnergyUsed");

            String dappAddress = (String)getAddressMethod.invoke(response);

            getLog().info("****************  Dapp deployment status ****************");
            getLog().info("Contract Address: " + dappAddress);
            getLog().info("Energy used: " + getEnergyUsed.invoke(response));
            getLog().info("Deployer Address: " + deployer);
            getLog().info("*********************************************************");

            getLog()
                    .info(String.format("%s was deployed successfully to the embedded AVM.", getDappJar()));

            //Update deploy status properties
            getCache().updateDeployAddress(dappAddress);

        } catch (Exception ex) {
            getLog()
                    .error(String.format("%s could not be deployed to the embedded AVM.", getDappJar()),
                            ex);
            throw new MojoExecutionException("Contract jar deployment failed", ex);
        }
    }

    //execute when remote kernel
    @Override
    protected void executeRemote() throws MojoExecutionException {

        //check if dAppJar exists
        if(!validateDappJar())
            return;

        String web3RpcUrl = resolveWeb3rpcUrl();

        String address = getAddress();

        String password = ConfigUtil.getProperty("password");

        String pk = getPrivateKey();
        if(pk == null || pk.isEmpty()) {
            if (address == null || address.isEmpty()) {
                printRemoteHelp();
                getLog().error("Deployer address cannot be null. Please set it through -D option in maven commandline.");
                throw new MojoExecutionException("Invalid args. Please set deployer address through -D option or environment variable.");
            }
        }

        //Parse other args
        parseArgs();

        //Get gas & gas price
        long gas = getGas();
        if(gas == 0)
            gas = defaultGas;

        long gasPrice = getGasPrice();
        if(gasPrice == 0)
            gasPrice = defaultGasPrice;

        try {

            Class localAvmClazz = getLocalAVMClass();

            Method getBytesMethod = localAvmClazz.getMethod("getBytesForDeploy", String.class, String.class);

            String hexCode = (String)getBytesMethod.invoke(null, dappJar, deployArgs);

            if(hexCode == null) {
                throw new MojoExecutionException("Error getting contract jar content");
            }

            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, MavenLog.getLog(getLog()));

            String txHash = null;
            if(pk != null && !pk.isEmpty()) {
                txHash = remoteAVMNode.sendDeployRawTransaction(null, pk, hexCode, BigInteger.ZERO, gas, gasPrice);
            } else {

                if (password != null && !password.isEmpty()) {
                    boolean status = remoteAVMNode.unlock(address, password);

                    if (status) {
                        getLog().info("Account unlocked successfully");
                    } else {

                    }
                }

                txHash = remoteAVMNode.deploy(address, hexCode, gas, gasPrice);
            }

            getLog().info(String.format("%s was deployed successfully.", dappJar));
            getLog().info(String.format("Transaction # : %s", txHash));

            //Update TxReceipt status properties
            getCache().updateDeployTxnReceipt(txHash);

            //Let's try to get receipt
            String wait = ConfigUtil.getProperty("wait");
            boolean enableWait = false;
            if(wait != null && !wait.isEmpty())
                enableWait = true;

            if(enableWait) {
                AVMGetReceiptMojo.startGetReceipt(web3RpcUrl, txHash, "tail", "silent", getCache(), getLog());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed deployment for contract : " + dappJar, e);
        }

    }

    private void parseArgs() throws MojoExecutionException {
        final Object[] args = new Object[5];

        deployArgs = ConfigUtil.getProperty("args");
        value = ConfigUtil.getProperty("value");

        if(value == null || value.isEmpty())
            valueB = BigInteger.ZERO;
        else {
            valueB = new BigInteger(value.trim());
        }
    }

    private void printRemoteHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:deploy -Dweb3rpc.url=<web3rpcUrl> [-Daddress=<address>] [-Dpassword=<password>]\n");
    }

}
