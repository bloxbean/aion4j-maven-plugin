package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

//Only support remote kernel
@Mojo(name = "get-logs")
public class AVMGetLogsMojo extends AVMBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if(isLocal()) {
            throw new MojoExecutionException("aion4j:get-logs is only supported for remote Aion kernel");
        }

        String web3RpcUrl = getWeb3RpcUrl();

        String fromBlock = System.getProperty("fromBlock");
        String toBlock = System.getProperty("toBlock");

        String address = System.getProperty("address");
        String topics = System.getProperty("topics");
        String blockHash = System.getProperty("blockhash");

        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

        String logs = remoteAVMNode.getLogs(fromBlock, toBlock, address, topics, blockHash);

        if(logs != null)
            getLog().info("Logs: \n" + logs);
        else {
            getLog().info("getlogs() failed");
            throw new MojoExecutionException("getlogs() failed");
        }
    }

    private void printHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:get-logs -Dweb3rpc.url=http://host:port -DfromBlock=<block num> -DtoBlock=<block num>" +
                " -Daddress=<address1, ...> -Dtopics=<topic1, ...> -Dblockhash=<block hash>");
    }

}
