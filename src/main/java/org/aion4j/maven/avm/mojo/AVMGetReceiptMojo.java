package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.api.Log;
import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.avm.helper.util.ResultCache;
import org.aion4j.maven.avm.impl.DummyLog;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.json.JSONObject;

//Only support remote kernel
@Mojo(name = "get-receipt", aggregator = true)
public class AVMGetReceiptMojo extends AVMBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if(isLocal()) {
            throw new MojoExecutionException("aion4j:get-receipt is only supported for remote Aion kernel");
        }

        String web3RpcUrl = resolveWeb3rpcUrl();
        String txHash = ConfigUtil.getProperty("txHash");

        String tail = ConfigUtil.getProperty("tail");
        String silent = ConfigUtil.getProperty("silent");

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

        startGetReceipt(web3RpcUrl, txHash, tail, silent, getCache(), getLog());

    }

    public static void startGetReceipt(String web3RpcUrl, String txHash, String tail, String silent, ResultCache cache, org.apache.maven.plugin.logging.Log log) throws MojoExecutionException {
        boolean enableTail = false;
        if(tail != null && !tail.isEmpty())
            enableTail = true;

        int counter = 0;
        int maxCountrer = 1;

        if(enableTail) maxCountrer = 15;
        while(counter < maxCountrer) {
            try {

                Log _log = null;
                if(enableTail && silent != null && !silent.isEmpty()) _log = new DummyLog();
                else _log = MavenLog.getLog(log);

                RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, _log);

                JSONObject response = remoteAVMNode.getReceipt(txHash);
                JSONObject resultObj = response.optJSONObject("result");

                counter++;

                if (resultObj == null) {
                    if(enableTail) {
                        log.info("Waiting for transaction to mine ...Trying (" + counter + " of " + maxCountrer + " times)");
                        Thread.currentThread().sleep(9000);
                        continue;
                    }
                } else {
                    String contractAddress = resultObj.optString("contractAddress");
                    if (contractAddress != null && !contractAddress.isEmpty()) {
                        //Update contract address in cache.
                        //Update deploy status properties
                        cache.updateDeployAddress(contractAddress);
                    } else {
                    }
                }

                log.info("Txn Receipt: \n");
                if (resultObj != null) {
                    log.info(resultObj.toString(2));
                } else
                    log.info(response.toString());

                break;
            } catch (Exception e) {
                log.debug(e);
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        if(counter == maxCountrer) {
            log.info("Waited too much for the receipt. Something is wrong.");
        }
    }

    private void printHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:getReceipt -Dweb3rpc.url=http://host:port -DtxHash=<tx-hash>");
    }

}
