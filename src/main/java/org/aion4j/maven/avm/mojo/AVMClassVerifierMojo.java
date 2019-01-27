package org.aion4j.maven.avm.mojo;

import com.google.common.base.VerifyException;
import org.aion4j.maven.avm.exception.LocalAVMException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "class-verifier", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, defaultPhase = LifecyclePhase.COMPILE)
public class AVMClassVerifierMojo extends AVMAbstractBaseMojo {

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object classVerfierImpl) throws MojoExecutionException {

        try {
            Method verifyMethod = classVerfierImpl.getClass().getMethod("verify", String.class, String.class);

            String outputDir = project.getBuild().getOutputDirectory();

            getLog().debug("Output folder : " + outputDir);

            Path source = Paths.get(outputDir);
            List<Path> paths = Files.walk(source).filter(f -> f.toFile().getName().endsWith(".class")).collect(Collectors.toList());

            List<VerificationError> errors = new ArrayList<>();

            for(Path path: paths) {
                String fullPath = path.toAbsolutePath().toString();
                String fileName = path.toFile().getName();
                String className = fileName.substring(0, fileName.length()-6);

                getLog().debug("Let's verify class : " + className);


                try {
                    verifyMethod.invoke(classVerfierImpl, className, fullPath);
                } catch (Exception e) {
                    errors.add(new VerificationError(e, fullPath));
                    //throw new MojoExecutionException("AVM erfication failed for class : " + fullPath, e);
                }
            }

            if(errors.size() > 0) {

                getLog().error("AVM verification failed with the following reasons:");

                for(VerificationError ve: errors) {
                    try {
                        getLog().error(String.format("Verification failed for %s, error: %s", ve.file, ve.exception.getCause().getMessage()));
                        getLog().debug(String.format("Verification failed for %s", ve.file), ve.exception);
                    } catch (Exception e) {
                        getLog().error(ve.exception); //Just for safer side, incase ve.exception.getCause() returns null
                    }
                }

                throw new MojoExecutionException("AVM Verification failed");
            }

        } catch (Exception e) {
            throw new MojoExecutionException("[Verification Error]", e);
        }

    }

    @Override
    protected void postExecuteLocalAvm(Object localAvmInstance) throws MojoExecutionException {

    }

    @Override
    protected Object getLocalAvmImplInstance(ClassLoader avmClassloader)  {
        try{
            Class clazz = avmClassloader.loadClass("org.aion4j.maven.avm.local.AVMClassVerifier");
            Constructor localAvmVerifierConstructor = clazz.getConstructor(boolean.class);

            Object classVerifierInstance = localAvmVerifierConstructor.newInstance(isLocal());
            return classVerifierInstance;
        } catch (Exception e) {
            getLog().debug("Error creating LocalAvmNode instance", e);
            throw new LocalAVMException(e);
        }
    }

    class VerificationError {
        public Exception exception;
        public String file;

        public VerificationError(Exception ex, String file) {
            this.exception = ex;
            this.file = file;
        }
    }
}
