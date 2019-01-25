package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.lang.reflect.Method;
import java.math.BigInteger;

@Mojo(name = "get-balance")
public class AVMGetBalanceMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecute() throws MojoExecutionException {

        if (!isLocal())
            throw new MojoExecutionException("get-balance is only supported for local Avm during development.");
    }

    @Override
    protected void execute(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

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
                getLog().info(String.format("Account Balance: " + response));
            } else
                getLog().info("Balance not found");

        } catch (Exception ex) {
            getLog()
                    .error("getBalance failed", ex);
            throw new MojoExecutionException("getBalance failed", ex);
        }
    }

}
