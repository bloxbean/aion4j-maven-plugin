package org.aion4j.maven.avm.mojo;

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
            throw new MojoExecutionException(String.format("Contract jar file doesn't exist : %s \n"
                    + "Please make sure you have built the project.", dappJar));
        }

        backupOriginalJar(Paths.get(getDappJar()));
        abiCompile(getLocalAVMClass());
    }

    private void abiCompile(Class localAvmClazz) throws MojoExecutionException {

        try {
            getLog().info("Post processing the jar >> " + dappJar);

            Method compileJarBytesMethod = localAvmClazz.getMethod("compileJarBytes", byte[].class);

            Path dAppJarPath = Paths.get(getDappJar());
            byte[] jarBytes = Files.readAllBytes(dAppJarPath);

            byte[] compiledBytes = (byte[]) compileJarBytesMethod.invoke(null, jarBytes );

            Path compileJarPath = Paths.get(getDappJar());
            Files.write(compileJarPath, compiledBytes);
        } catch (Exception ex) {
            getLog()
                    .error(String.format("Contract Jar post processing failed"),
                            ex);
            throw new MojoExecutionException("Contract Jar post processing failed", ex);
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
