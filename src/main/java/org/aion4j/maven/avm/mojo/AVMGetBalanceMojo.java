package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.lang.reflect.Method;
import java.math.BigInteger;

@Mojo(name = "get-balance")
public class AVMGetBalanceMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

        if (!isLocal())
            throw new MojoExecutionException("get-balance is only supported for local Avm during development.");
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            final Method getBalanceMethod = localAvmInstance.getClass().getMethod("getBalance", String.class);

            String address = ConfigUtil.getPropery("address");

            if(address == null || address.isEmpty()) {
                getLog().error("Usage: \n ./mvnw aion4j:get-balance -Daddress=a0xxxxxxxxxx");
                throw new MojoExecutionException("Please provide -Daddress property");
            }

            Object response = getBalanceMethod.invoke(localAvmInstance, address);

            if(response != null) {
                getLog().info( "Address        : " + address);
                getLog().info(String.format("Balance        : " + response));
            } else
                getLog().info("Balance not found");

        } catch (Exception ex) {
            getLog()
                    .error("getBalance failed", ex);
            throw new MojoExecutionException("getBalance failed", ex);
        }
    }

    @Override
    protected void executeRemote() throws MojoExecutionException {
        String web3RpcUrl = ConfigUtil.getPropery("web3rpc.url");

        if(web3RpcUrl == null || web3RpcUrl.isEmpty()) {
            getLog().error("web3rpc.url cannot be null");
            printRemoteHelp();
            throw new MojoExecutionException("Invalid args");
        }

        String address = ConfigUtil.getPropery("address");

        if(address == null || address.isEmpty()) {
            printRemoteHelp();
            throw new MojoExecutionException("Invalid args");
        }


        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

        String balanceInHex = remoteAVMNode.getBalance(address);

        if(balanceInHex != null && !balanceInHex.trim().isEmpty()) {
            if(balanceInHex.startsWith("0x"))
                balanceInHex = balanceInHex.substring(2);

            BigInteger balance = new BigInteger(balanceInHex, 16);

            getLog().info( "Address         : " + address);
            getLog().info(String.format("Balance         : " + balance));
        } else {
            getLog().info("Balance not found for the account");
        }
    }

    private void printRemoteHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:get-balance -Dweb3rpc.url=http://host:port -Daddress=<address> -Dpassword=<password>");
    }
}
