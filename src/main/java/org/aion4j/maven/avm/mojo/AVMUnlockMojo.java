package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

//Only support remote kernel
@Mojo(name = "unlock")
public class AVMUnlockMojo extends AVMBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if(isLocal()) {
            throw new MojoExecutionException("aion4j:unlock is only supported for remote Aion kernel");
        }

        String web3RpcUrl = resolveWeb3rpcUrl();

        if(web3RpcUrl == null || web3RpcUrl.isEmpty()) {
            getLog().error("web3rpc.url cannot be null");
            printHelp();
            throw new MojoExecutionException("Invalid args - web3rpc.url cannot be null");
        }

        String address = ConfigUtil.getPropery("address");
        String password = ConfigUtil.getPropery("password");

        if(address == null || address.isEmpty() || password == null || password.isEmpty()) {
            printHelp();
            throw new MojoExecutionException("Invalid args - address / password is empty");
        }


        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

        boolean status = remoteAVMNode.unlock(address, password);

        if(status)
            getLog().info("Account unlocked successfully");
        else {
            getLog().info("Account unlock failed");
            throw new MojoExecutionException("Account unlock failed");
        }
    }

    private void printHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:unlock -Dweb3rpc.url=http://host:port -Daddress=<address> -Dpassword=<password>");
    }

}
