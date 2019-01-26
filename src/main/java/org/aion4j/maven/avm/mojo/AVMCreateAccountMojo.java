package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

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

            String addressToCreate = ConfigUtil.getPropery("address");
            String balance = ConfigUtil.getPropery("balance");

            if(addressToCreate == null || addressToCreate.isEmpty() || balance == null || balance.isEmpty()) {
                getLog().error("Usage:\n mvn -Daddress a0xxxxx -Dbalance=2000000000");
                throw new MojoExecutionException("Account can not be created. Invalid input args.");
            }

//            final Object[] args = new Object[2];
//            args[0] = addressToCreate;
//            args[1] = new BigInteger(balance.trim());

            Object response = createAccountMethod.invoke(localAvmInstance, addressToCreate, new BigInteger(balance.trim()));

            getLog().info(String.format("Account creation successful"));
            getLog().info("Address: " + addressToCreate);
            getLog().info("Balance: " + balance.trim());


        } catch (Exception ex) {
            getLog()
                    .error("Account creation failed", ex);
            throw new MojoExecutionException("Account creation failed", ex);
        }
    }

    @Override
    protected void executeRemote() throws MojoExecutionException {
        String password = ConfigUtil.getPropery("password");

        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(getWeb3RpcUrl(), getLog());

        try {
            String newAddress = remoteAVMNode.createAccount(password);

            getLog().info(String.format("Account creation successful"));
            getLog().info("Address : " + newAddress);

        } catch (Exception e) {
            throw new MojoExecutionException("Account creation failed", e);
        }
    }
}
