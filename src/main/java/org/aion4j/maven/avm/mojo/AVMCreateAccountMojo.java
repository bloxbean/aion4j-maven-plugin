package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.lang.reflect.Method;
import java.math.BigInteger;

@Mojo(name = "create-account")
public class AVMCreateAccountMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }


    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            final Method createAccountMethod = localAvmInstance.getClass().getMethod("createAccountWithBalance", String.class, BigInteger.class);

            String addressToCreate = ConfigUtil.getProperty("address");
            String balance = ConfigUtil.getProperty("balance");

            if(addressToCreate == null || addressToCreate.isEmpty() || balance == null || balance.isEmpty()) {
                getLog().error("Usage:\n mvn -Daddress a0xxxxx -Dbalance=2000000000");
                throw new MojoExecutionException("Account can not be created. Invalid input args. Usage:\n mvn -Daddress a0xxxxx -Dbalance=2000000000");
            }

//            final Object[] args = new Object[2];
//            args[0] = addressToCreate;
//            args[1] = new BigInteger(balance.trim());

            Object response = createAccountMethod.invoke(localAvmInstance, addressToCreate, new BigInteger(balance.trim()));

            if((boolean)response) {
                getLog().info(String.format("Account creation successful"));
                getLog().info("Address: " + addressToCreate);
                getLog().info("Balance: " + balance.trim());
            } else{
                getLog().info("Account creation failed. Please check if account exists");
            }


        } catch (Exception ex) {
            getLog()
                    .error("Account creation failed", ex);
            throw new MojoExecutionException("Account creation failed", ex);
        }
    }

    @Override
    protected void executeRemote() throws MojoExecutionException {
        String password = ConfigUtil.getProperty("password");

        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(resolveWeb3rpcUrl(), MavenLog.getLog(getLog()));

        try {
            String newAddress = remoteAVMNode.createAccount(password);

            getLog().info(String.format("Account creation successful"));
            getLog().info("Address : " + newAddress);

        } catch (Exception e) {
            throw new MojoExecutionException("Account creation failed", e);
        }
    }
}
