package org.aion4j.maven.avm.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

//Only support for local kernel
@Mojo(name = "explore")
public class AVMExploreMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            final Method exploreMethod = localAvmInstance.getClass().getMethod("explore", String.class, PrintStream.class);

            String deployedAddress = getCache().getLastDeployedAddress();

            if(deployedAddress == null || deployedAddress.isEmpty()) {
                throw new MojoExecutionException("deployedAddress can not be empty. Please make sure the contract has already been deployed");
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            PrintStream printer = new PrintStream(stream);

            exploreMethod.invoke(localAvmInstance, deployedAddress, printer);

            String retData = new String(stream.toByteArray(), StandardCharsets.UTF_8);

            getLog().info(retData);


        } catch (Exception ex) {
            getLog()
                    .error("Explore failed", ex);
            throw new MojoExecutionException("Explore failed", ex);
        }
    }

    @Override
    protected void executeRemote() throws MojoExecutionException {

        getLog().warn("aion4j:explore only works for embedded AVM.");
    }
}
