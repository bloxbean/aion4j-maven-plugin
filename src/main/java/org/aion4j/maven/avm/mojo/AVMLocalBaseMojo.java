package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.local.LocalAvmNode;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

public abstract class AVMLocalBaseMojo extends AVMBaseMojo {


    public void execute() throws MojoExecutionException {

        if(isLocal()) {
            executeLocalAVM();
        }

    }

    private void executeLocalAVM() throws MojoExecutionException {
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

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = new URLClassLoader(new URL[]{urlsForClassLoader, pluginJar});

        Thread.currentThread().setContextClassLoader(classLoader);

        Method shutDownMethod = null;
        Object localAvmInstance = null;
        try {
            localAvmInstance = getLocalAvmImplInstance(classLoader);

            //call abstract method
            execute(classLoader, localAvmInstance);

        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException("Avm maven execution failed", ex);
        } finally {

            postExecute(localAvmInstance);

            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    //Get the maven plugin jar file location to solve parent->child classloader issue
    private URL getLocalAVMNodeClassJarLocation() {
        CodeSource src = LocalAvmNode.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            return jar;
        }

        return null;
    }

    protected abstract void preexecute() throws MojoExecutionException;
    protected abstract void execute(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException;
    protected abstract void postExecute(Object localAvmInstance) throws MojoExecutionException;
    protected abstract Object getLocalAvmImplInstance(ClassLoader avmClassloader);

}
