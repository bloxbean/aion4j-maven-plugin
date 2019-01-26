package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.local.LocalAvmNode;
import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;

public abstract class AVMAbstractBaseMojo extends AVMBaseMojo {


    public void execute() throws MojoExecutionException {

        if(isLocal()) {
            executeLocalAVM();
        } else {
            executeRemote();
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
            executeLocalAvm(classLoader, localAvmInstance);

        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException("Avm maven execution failed", ex);
        } finally {

            postExecuteLocalAvm(localAvmInstance);

            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    //Get the maven plugin jar file location to solve parent->child classloader issue
    protected URL getLocalAVMNodeClassJarLocation() {
        CodeSource src = LocalAvmNode.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            return jar;
        }

        return null;
    }

    //This method is used from remote kernel implementation. If the remote implementation wants to use LocalAVMLibraries and call
    //some static methods. Exp: Encoding of method call etc. This method should not be called for local / embedded AVM support.
    protected Class getLocalAVMClass() throws MojoExecutionException{
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
            return localAvmClazz;
        } catch (ClassNotFoundException e) {
            getLog().debug(e);
            return null;
        }
    }

    //Only needed for remote kernel support
    protected void executeRemote() throws MojoExecutionException {

    }

    protected abstract void preexecuteLocalAvm() throws MojoExecutionException;
    protected abstract void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException;
    protected abstract void postExecuteLocalAvm(Object localAvmInstance) throws MojoExecutionException;
    protected abstract Object getLocalAvmImplInstance(ClassLoader avmClassloader);

}
