package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.exception.LocalAVMException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "class-verifier", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.COMPILE)
public class AVMClassVerifierMojo extends AVMLocalBaseMojo {

    @Override
    protected void preexecute() throws MojoExecutionException {

    }

    @Override
    protected void execute(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

        try {
            Method verifyMethod = localAvmInstance.getClass().getMethod("verify", String.class, String.class);

            String outputDir = project.getBuild().getOutputDirectory();

            getLog().debug("Output folder : " + outputDir);

            Path source = Paths.get(outputDir);
            List<Path> paths = Files.walk(source).filter(f -> f.toFile().getName().endsWith(".class")).collect(Collectors.toList());

            for(Path path: paths) {
                String fullPath = path.toAbsolutePath().toString();
                String fileName = path.toFile().getName();
                String className = fileName.substring(0, fileName.length()-6);

                getLog().debug("Let's verify class : " + className);

                try {
                    verifyMethod.invoke(className, fullPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    //throw new MojoExecutionException("Avm verification failed for class : " + path, e);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void postExecute(Object localAvmInstance) throws MojoExecutionException {

    }

    @Override
    protected Object getLocalAvmImplInstance(ClassLoader avmClassloader)  {
        try{
            Class clazz = avmClassloader.loadClass("org.aion4j.maven.avm.local.AVMClassVerifier");
            Constructor localAvmVerifierConstructor = clazz.getConstructor();

            Object localAvmVerifierInstance = localAvmVerifierConstructor.newInstance();
            return localAvmVerifierInstance;
        } catch (Exception e) {
            getLog().debug("Error creating LocalAvmNode instance", e);
            throw new LocalAVMException(e);
        }
    }
}
