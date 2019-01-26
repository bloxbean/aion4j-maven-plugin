package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.math.BigInteger;

//Only support remote kernel
@Mojo(name = "transfer")
public class AVMTransferMojo extends AVMBaseMojo {

    private long defaultGas = 2000000;
    private long defaultGasPrice = 100000000000L;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if(isLocal()) {
            throw new MojoExecutionException("aion4j:transfer is only supported for remote Aion kernel");
        }

        String web3RpcUrl = getWeb3RpcUrl();

        long gas = getGas();
        if(gas == 0)
            gas = defaultGas;

        long gasPrice = getGasPrice();
        if(gasPrice == 0)
            gasPrice = defaultGasPrice;


        String from = ConfigUtil.getPropery("from");
        String to = ConfigUtil.getPropery("to");
        String value = ConfigUtil.getPropery("value");

        String password = ConfigUtil.getPropery("password");

        if(from == null || from.isEmpty() || to == null || to.isEmpty() || value == null || value.isEmpty()) {
            printHelp();
            throw new MojoExecutionException("Invalid args");
        }

        BigInteger valueInAmp = BigInteger.ZERO;
        try {
            valueInAmp = new BigInteger(value.trim());
            //Long aionValue = Long.parseLong(value.trim());
            //valueInAmp = new BigInteger(String.valueOf(aionValue)).multiply(new BigInteger(String.valueOf(Math.pow(10,18))));
        } catch (NumberFormatException ex) {
            throw new MojoExecutionException("Invalid value or amount. " + value, ex);
        }

        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());

        if(password != null && !password.trim().isEmpty()) {
            //unlock
            remoteAVMNode.unlock(from, password);
        }

        String txReceipt = remoteAVMNode.transfer(from, to, valueInAmp, gas, gasPrice);

        if(txReceipt != null){

            getLog().info("Transfer successful");
            getLog().info("****************  Transfer Txn result  ****************");
            getLog().info("Transaction receipt       :" + txReceipt);
            getLog().info("******************************************************");
        } else {
            getLog().error("Transfer failed");

            throw new MojoExecutionException("Transfer failed");
        }
    }

    private void printHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:transfer -Dweb3rpc.url=http://host:port -Dfrom=<address> -Dto=<address> -Dvalue=<Amount>");
    }

}
