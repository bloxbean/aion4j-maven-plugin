package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.exception.LocalAVMException;
import org.aion4j.maven.avm.local.LocalAvmNode;
import org.aion4j.maven.avm.util.DeployResultConfig;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMDeployMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecute() throws MojoExecutionException{
        //check if dAppJar exists
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            throw new MojoExecutionException(String.format("Dapp jar file doesn't exist : %s \n"
                    + "Please make sure you have built the project.", dappJar));
        }
    }

    @Override
    protected void execute(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            final Method deployMethod = localAvmInstance.getClass().getMethod("deploy", String.class);

            final Object[] args = new Object[1];
            args[0] = new String[]{dappJar};

            getLog().info(String.format("Deploying %s to the embedded Avm ...", getDappJar()));
            getLog().info("Avm storage path : " + getStoragePath());

            Object response = deployMethod.invoke(localAvmInstance, dappJar);

            Method getAddressMethod = response.getClass().getMethod("getAddress");
            Method getEnergyUsed = response.getClass().getMethod("getEnergyUsed");

            String dappAddress = (String)getAddressMethod.invoke(response);

            getLog().info("****************  Dapp deployment status ****************");
            getLog().info("Dapp address: " + dappAddress);
            getLog().info("Energy used: " + getEnergyUsed.invoke(response));
            getLog().info("*********************************************************");

            getLog()
                    .info(String.format("%s deployed successfully to the embedded AVM.", getDappJar()));

            //Update deploy status properties
            DeployResultConfig.updateDeployAddress(getStoragePath() , dappAddress);

        } catch (Exception ex) {
            getLog()
                    .error(String.format("%s could not be deployed to the embedded AVM.", getDappJar()),
                            ex);
            throw new MojoExecutionException("Dapp jar deployment failed", ex);
        }
    }

}
