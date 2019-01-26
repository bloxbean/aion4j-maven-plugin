package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.exception.LocalAVMException;
import org.aion4j.maven.avm.local.LocalAvmNode;
import org.aion4j.maven.avm.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.aion4j.maven.avm.util.DeployResultConfig;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMDeployMojo extends AVMLocalRuntimeBaseMojo {

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException{
        //check if dAppJar exists
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            throw new MojoExecutionException(String.format("Dapp jar file doesn't exist : %s \n"
                    + "Please make sure you have built the project.", dappJar));
        }
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {

            String deployer = System.getProperty("address");

            final Method deployMethod = localAvmInstance.getClass().getMethod("deploy", String.class, String.class);

            final Object[] args = new Object[2];
            args[0] = new String[]{dappJar};
            args[1] = deployer;

            getLog().info(String.format("Deploying %s to the embedded Avm ...", getDappJar()));
            getLog().info("Avm storage path : " + getStoragePath());

            Object response = deployMethod.invoke(localAvmInstance, dappJar, deployer);

            Method getAddressMethod = response.getClass().getMethod("getAddress");
            Method getEnergyUsed = response.getClass().getMethod("getEnergyUsed");

            String dappAddress = (String)getAddressMethod.invoke(response);

            getLog().info("****************  Dapp deployment status ****************");
            getLog().info("Dapp address: " + dappAddress);
            getLog().info("Energy used: " + getEnergyUsed.invoke(response));
            getLog().info("Deployer Address: " + getDefaultAddress());
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

    //executed when remote kernel
    //@Override
    protected void executeRemote1() throws MojoExecutionException {

        URL urlsForClassLoader = null;
        try {

            if (!new File(getAvmLibDir() + File.separator + "avm.jar").exists()) {
                getLog()
                        .error("avm.jar not found. Please make sure avm.jar exists in avm lib folder."
                                + "\n You can also execution aion4j:init-lib maven goal to copy default jars to avm lib folder.");

                throw new MojoExecutionException("avm.jar is not found in " + getAvmLibDir());
            }

            urlsForClassLoader = new File(getAvmLibDir() + File.separator + "avm.jar")
                    .toURI().toURL();
            getLog().info(urlsForClassLoader.toURI().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ClassLoader classLoader = new URLClassLoader(new URL[]{urlsForClassLoader});

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        // IMPORTANT: Save the old System.out!
        PrintStream oldPs = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);

        try {

            Class clazz = classLoader.loadClass("org.aion.cli.AvmCLI");
            final Method method = clazz.getMethod("main", String[].class);

            final Object[] args = new Object[1];
            args[0] = new String[] { "bytes", dappJar};

            method.invoke(null, args);

            String result = baos.toString();

            System.setOut(oldPs);

            getLog().info(String.format("%s deployed successfully to the embedded AVM.", getDappJar()));

        } catch (Exception e) {
            getLog().error(String.format("%s could not be deployed to the embedded AVM.", getDappJar()), e);
            throw new MojoExecutionException("Dapp jar deployment failed", e);
        } finally {
            System.setOut(oldPs);
        }
    }

    @Override
    protected void executeRemote() throws MojoExecutionException {

        String web3RpcUrl = ConfigUtil.getPropery("web3rpc.url");

        if(web3RpcUrl == null || web3RpcUrl.isEmpty()) {
            getLog().error("web3rpc.url cannot be null. Please set it through -D option in maven commandline.");
            throw new MojoExecutionException("Invalid args");
        }

        String address = ConfigUtil.getPropery("address");

        getLog().info("----------- AVM classpath Urls --------------");
        URL urlsForClassLoader = null;
        try {

            if (!new File(getAvmLibDir() + File.separator + "avm.jar").exists()) {
                getLog()
                        .error("avm.jar not found. Please make sure avm.jar exists in avm lib folder."
                                + "\n You can also execution aion4j:init-lib maven goal to copy default jars to avm lib folder.");

                throw new MojoExecutionException("avm.jar is not found in " + getAvmLibDir());
            }

            urlsForClassLoader = new File(getAvmLibDir() + File.separator + "avm.jar")
                    .toURI().toURL();
            getLog().info(urlsForClassLoader.toURI().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        URL pluginJar = getLocalAVMNodeClassJarLocation();
        if (pluginJar != null) {
            try {
                getLog().info(pluginJar.toURI().toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        getLog().info("----------- AVM classpath Urls Ends --------------");

        ClassLoader avmClassLoader = new URLClassLoader(new URL[]{urlsForClassLoader, pluginJar});

        try {
            Class localAvmClazz = avmClassLoader.loadClass("org.aion4j.maven.avm.local.LocalAvmNode");

            Method getBytesMethod = localAvmClazz.getMethod("getBytesForDeploy", String.class);

            String hexCode = (String)getBytesMethod.invoke(null, dappJar);

            if(hexCode == null) {
                throw new MojoExecutionException("Error getting dappJar content");
            }

            RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3RpcUrl, getLog());
            String txHash = remoteAVMNode.deploy(address, hexCode,  5000000, 100000000000L);

            getLog().info("Dapp deployed successfully. Tx# : " + txHash);

        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed deployment for dapp : " + dappJar, e);
        }

    }

}
