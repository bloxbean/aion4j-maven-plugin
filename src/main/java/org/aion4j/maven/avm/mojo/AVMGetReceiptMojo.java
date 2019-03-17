package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.json.JSONObject;

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

        String cachedTxHash = null;
        if(txHash == null || txHash.isEmpty()) {

            cachedTxHash = getCache().getLastTxnReceipt();

            if(cachedTxHash == null || cachedTxHash.isEmpty()) {
                printHelp();
                throw new MojoExecutionException("Invalid args. \"txHash\" property is missing");
            } else {
                txHash = cachedTxHash;
            }
        }

        try {
            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, MavenLog.getLog(getLog()));

            JSONObject response = remoteAVMNode.getReceipt(txHash);

            JSONObject resultObj = response.optJSONObject("result");

            if(resultObj == null) {
            } else {
                String contractAddress = resultObj.optString("contractAddress");
                if(contractAddress != null && !contractAddress.isEmpty()) {
                    //Update contract address in cache.
                    //Update deploy status properties
                    getCache().updateDeployAddress(contractAddress);
                } else {
                }
            }

            getLog().info("Txn Receipt: \n");
            if(resultObj != null) {
                getLog().info(resultObj.toString(2));
            } else
                getLog().info(response.toString());

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
