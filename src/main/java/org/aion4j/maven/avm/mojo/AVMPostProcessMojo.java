package org.aion4j.maven.avm.mojo;

import static org.aion4j.avm.helper.util.ConfigUtil.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "postpack", defaultPhase = LifecyclePhase.PACKAGE)
public class AVMPostProcessMojo extends AVMAbstractBaseMojo {

    private final static String DISABLE_JAR_OPTIMIZATION = "disableJarOptimization";

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeRemote() throws MojoExecutionException {
        startPostProcessing();
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {
       startPostProcessing();
    }

    private void startPostProcessing() throws MojoExecutionException {
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            throw new MojoExecutionException(String.format("Post processing failed. Contract jar file doesn't exist : %s \n"
                    + "Please make sure you have built the project.", dappJar));
        }

        backupOriginalJar(Paths.get(getDappJar()));

        byte[] jarBytes = readJarContent();

        jarBytes = abiCompile(getLocalAVMClass(), jarBytes);

        boolean disableJarOptimization = getAvmConfigurationBooleanProps(DISABLE_JAR_OPTIMIZATION, false);
        if(!disableJarOptimization)
            jarBytes = optimizeJar(getLocalAVMClass(), jarBytes, getAvmConfigurationBooleanProps(PRESERVE_DEBUGGABILITY, false));

        writeJarContent(jarBytes);
    }

    private byte[] readJarContent() throws MojoExecutionException{
        try {
            Path dAppJarPath = Paths.get(getDappJar());
            byte[] jarBytes = Files.readAllBytes(dAppJarPath);
            return jarBytes;
        } catch (Exception e) {
            getLog().error(String.format("Error reading contract jar content : " + getDappJar() ),e);
            throw new MojoExecutionException("Error reading contract jar content: " + getDappJar(), e);
        }
    }

    private void writeJarContent(byte[] bytes) throws MojoExecutionException {
        try {
            Path compileJarPath = Paths.get(getDappJar());
            Files.write(compileJarPath, bytes);
        } catch (Exception e) {
            getLog().error(String.format("Error writing contract jar content : " + getDappJar() ),e);
            throw new MojoExecutionException("Error writing contract jar content : " + getDappJar(), e);
        }
    }

    private byte[] abiCompile(Class localAvmClazz, byte[] jarBytes) throws MojoExecutionException {

        try {
            getLog().info("Post compile the jar >> " + dappJar);
            Method compileJarBytesMethod = localAvmClazz.getMethod("compileJarBytes", byte[].class);
            byte[] compiledBytes = (byte[]) compileJarBytesMethod.invoke(null, jarBytes );

            return compiledBytes;
        } catch (Exception ex) {
            getLog()
                    .error(String.format("Contract Jar post compilation failed"),
                            ex);
            throw new MojoExecutionException("Contract Jar post compilation failed", ex);
        }
    }

    private byte[] optimizeJar(Class localAvmClazz, byte[] jarBytes, boolean debugMode) throws MojoExecutionException {

        try {
            getLog().info("Optimizing the jar content >> " + dappJar);

            Method optimizeJarBytesMethod = localAvmClazz.getMethod("optimizeJarBytes", byte[].class, boolean.class);

            byte[] optimizedBytes = (byte[]) optimizeJarBytesMethod.invoke(null, jarBytes, debugMode );

            return optimizedBytes;
        } catch (Exception ex) {
            getLog()
                    .error(String.format("Contract Jar optimization failed"),
                            ex);
            throw new MojoExecutionException("Contract Jar optimization failed", ex);
        }
    }

    private void backupOriginalJar(Path dAppJarPath) {
        //Take a backup of original jar file.
        try {
            String backupJar = dAppJarPath.getParent().toAbsolutePath() + File.separator + "original-" + dAppJarPath.toFile().getName();
            //Keep a backup of original jar
            Files.copy(dAppJarPath, Paths.get(backupJar));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void postExecuteLocalAvm(Object localAvmInstance) throws MojoExecutionException {

    }

    @Override
    protected Object getLocalAvmImplInstance(ClassLoader avmClassloader) {
        return null;
    }

}
