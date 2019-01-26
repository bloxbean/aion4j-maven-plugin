package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.aion4j.maven.avm.util.DeployResultConfig;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMDeployMojo extends AVMLocalRuntimeBaseMojo {

    //Needed for remote
    private long defaultGas = 5000000;
    private long defaultGasPrice = 100000000000L;

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException{
        //check if dAppJar exists
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            throw new MojoExecutionException(String.format("Dapp jar file doesn't exist : %s \n"
                    + "Please make sure you have built the project.", dappJar));
        }
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            String deployer = getAddress();

            final Method deployMethod = localAvmInstance.getClass().getMethod("deploy", String.class, String.class);

            final Object[] args = new Object[2];
            args[0] = new String[]{dappJar};
            args[1] = deployer;

            getLog().info(String.format("Deploying %s to the embedded Avm ...", getDappJar()));

            if(deployer != null && !deployer.trim().isEmpty())
                getLog().info("Deployer address : " + deployer);
            else
                deployer = getLocalDefaultAddress();

            getLog().info("Avm storage path : " + getStoragePath());

            Object response = deployMethod.invoke(localAvmInstance, dappJar, deployer);

            Method getAddressMethod = response.getClass().getMethod("getAddress");
            Method getEnergyUsed = response.getClass().getMethod("getEnergyUsed");

            String dappAddress = (String)getAddressMethod.invoke(response);

            getLog().info("****************  Dapp deployment status ****************");
            getLog().info("Dapp address: " + dappAddress);
            getLog().info("Energy used: " + getEnergyUsed.invoke(response));
            getLog().info("Deployer Address: " + deployer);
            getLog().info("*********************************************************");

            getLog()
                    .info(String.format("%s deployed successfully to the embedded AVM.", getDappJar()));

            //Update deploy status properties
            DeployResultConfig.updateDeployAddress(getStoragePath() , dappAddress);

        } catch (Exception ex) {
            getLog()
                    .error(String.format("%s could not be deployed to the embedded AVM.", getDappJar()),
                            ex);
            throw new MojoExecutionException("Dapp jar deployment failed", ex);
        }
    }

    //execute when remote kernel
    @Override
    protected void executeRemote() throws MojoExecutionException {

        //check if dAppJar exists
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            throw new MojoExecutionException(String.format("Dapp jar file doesn't exist : %s \n"
                    + "Please make sure you have built the project.", dappJar));
        }

        String web3RpcUrl = getWeb3RpcUrl();

        String address = getAddress();

        String password = ConfigUtil.getPropery("password");

        //Get gas & gas price
        long gas = getGas();
        if(gas == 0)
            gas = defaultGas;

        long gasPrice = getGasPrice();
        if(gasPrice == 0)
            gasPrice = defaultGasPrice;

        try {

            Class localAvmClazz = getLocalAVMClass();

            Method getBytesMethod = localAvmClazz.getMethod("getBytesForDeploy", String.class);

            String hexCode = (String)getBytesMethod.invoke(null, dappJar);

            if(hexCode == null) {
                throw new MojoExecutionException("Error getting dappJar content");
            }

            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

            if(password != null && !password.isEmpty()) {
                boolean status = remoteAVMNode.unlock(address, password);

                if(status) {
                   getLog().info("Account unlocked successfully");
                } else {

                }
            }

            String txHash = remoteAVMNode.deploy(address, hexCode,  gas, gasPrice);

            getLog().info("Dapp deployed successfully. Tx# : " + txHash);

        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed deployment for dapp : " + dappJar, e);
        }

    }

}
