package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

//Only support remote kernel
@Mojo(name = "get-receipt")
public class AVMGetReceiptMojo extends AVMBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if(isLocal()) {
            throw new MojoExecutionException("aion4j:get-receipt is only supported for remote Aion kernel");
        }

        String web3RpcUrl = resolveWeb3rpcUrl();
        String txHash = ConfigUtil.getPropery("txHash");

        if(txHash == null || txHash.isEmpty()) {
            printHelp();
            throw new MojoExecutionException("Invalid args");
        }

        try {
            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

            String receipt = remoteAVMNode.getReceipt(txHash);

            getLog().info("Txn Receipt: \n");
            getLog().info(receipt);
        } catch (Exception e) {
            getLog().debug(e);
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private void printHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:getReceipt -Dweb3rpc.url=http://host:port -DtxHash=<tx-hash>");
    }

}
