package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.crypto.Account;
import org.aion4j.avm.helper.crypto.AccountGenerator;
import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * This mojo is deprecated.
 * This mojo has been replaced by {@link AVMAccountFaucetMojo}
 */
@Mojo(name = "create-account", aggregator = true)
public class AVMCreateAccountMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {
            String addressToCreate = ConfigUtil.getProperty("address");
            String balance = ConfigUtil.getProperty("balance");

            if((addressToCreate == null || addressToCreate.isEmpty()) && (balance == null || balance.isEmpty())) { //generate new private key& address

                generateClientSideAccount();

            } else {

                final Method createAccountMethod = localAvmInstance.getClass().getMethod("createAccountWithBalance", String.class, BigInteger.class);

                if (balance == null || balance.isEmpty()) {
                    getLog().error("Usage:\n mvn create-account -Daddress a0xxxxx -Dbalance=2000000000");

                }

                if (addressToCreate == null || addressToCreate.isEmpty()) {
                    Account account = AccountGenerator.newAddress();

                    if (account.getAddress() == null || account.getAddress().isEmpty()) {
                        throw new MojoExecutionException("Unable to generate a new address. Please provide an address as argument." +
                                " Usage:\n mvn create-account [-Daddress a0xxxxx] -Dbalance=2000000000");
                    } else {
                        addressToCreate = account.getAddress();
                    }
                }

//            final Object[] args = new Object[2];
//            args[0] = addressToCreate;
//            args[1] = new BigInteger(balance.trim());

                Object response = createAccountMethod.invoke(localAvmInstance, addressToCreate, new BigInteger(balance.trim()));

                if ((boolean) response) {
                    getLog().info(String.format("Account creation successful"));
                    getLog().info("Address: " + addressToCreate);
                    getLog().info("Balance: " + balance.trim());
                } else {
                    getLog().info("Account creation failed. Please check if account exists");
                }
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
        boolean kernelManaged = ConfigUtil.getProperty("kernel") != null? Boolean.parseBoolean(ConfigUtil.getProperty("kernel")): false;


        if(kernelManaged) {
            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(resolveWeb3rpcUrl(), MavenLog.getLog(getLog()));

            try {
                String newAddress = remoteAVMNode.createAccount(password);

                getLog().info(String.format("Account creation successful"));
                getLog().info("Address : " + newAddress);

            } catch (Exception e) {
                throw new MojoExecutionException("Account creation failed", e);
            }
        } else {
            generateClientSideAccount();
        }
    }

    private void generateClientSideAccount() {
        Account account = AccountGenerator.newAddress();

        String address = account.getAddress();

        if(!address.startsWith("0x"))
            address = "0x" + address;

        getLog().info(String.format("Account creation successful"));
        getLog().info("Address : " + address);
        getLog().info("Private Key: " + account.getPrivateKey());
    }
}
