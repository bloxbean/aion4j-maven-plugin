package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.exception.LocalAVMException;
import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class AVMLocalRuntimeBaseMojo extends AVMAbstractBaseMojo {

    @Override
    protected Object getLocalAvmImplInstance(ClassLoader avmClassloader) {

        try {
            Class clazz = avmClassloader.loadClass("org.aion4j.avm.helper.local.LocalAvmNode");
            Constructor localAvmConstructor = clazz.getConstructor(String.class, String.class);

            //String address = getAddress();
            //If address is not passed as -D or set as env variable use default address
            //if(address == null || address.trim().isEmpty())
            String address = getLocalDefaultAddress(); //default init is only for localDefaultAddress

            Object localAvmInstance = localAvmConstructor.newInstance(getStoragePath(), address);
            return localAvmInstance;
        } catch (Exception e) {
            getLog().debug("Error creating LocalAvmNode instance", e);
            throw new LocalAVMException(e);
        }

    }

    protected void postExecuteLocalAvm(Object localAvmInstance) throws MojoExecutionException {

        try {
            Method shutDownMethod = localAvmInstance.getClass().getMethod("shutdown");

            if (localAvmInstance != null && shutDownMethod != null) {

                shutDownMethod.invoke(localAvmInstance);
            }
        } catch (Exception e) {
            e.printStackTrace();
            getLog().debug("Error in postExecution", e);
        }
    }

}
