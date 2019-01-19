package org.aion4j.maven.avm.mojo;

import java.io.File;
import java.io.IOException;
import org.aion4j.maven.avm.util.ZipUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "avm-prepack", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class AVMPrepackMojo extends AVMBaseMojo {

    private final static String AVM_USERLIB_JAR = "org-aion-avm-userlib.jar";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        extractUserLibJarInClasses();
    }

    private void extractUserLibJarInClasses() throws MojoExecutionException {
        String libFolderPath = getAvmLibDir();
        File libFolder = new File(libFolderPath);

        if(!libFolder.exists()) {
            throw new MojoExecutionException(String.format("Precondition failed. Avm Lib folder not found.\n"
                + " Please check if %s directory exist.\n"
                + "You may want to run 'mvn aion4j:initialize' to copy default avm jars to lib folder", libFolderPath));
        }

        File userLibJarFile = new File(libFolder, AVM_USERLIB_JAR);
        if(!userLibJarFile.exists()) {
            throw new MojoExecutionException(String.format("Precondition failed. Avm userlib jar file is not found in avm lib folder.\n"
                + " Please check if %s directory exist.\n"
                + "You may want to try 'mvn aion4j:initialize' to copy default avm jars to lib folder", libFolderPath));
        }

        String outputDirectory = project.getBuild().getOutputDirectory();

        File classesFolder = new File(outputDirectory);

        try {
            getLog().info(String.format("Extracting  %s in classes folder for packaging", AVM_USERLIB_JAR) );
            ZipUtil.unzipFile(userLibJarFile, classesFolder);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Unable to copy %s content to %s folder for packaging",
                AVM_USERLIB_JAR, outputDirectory), e);
        }
    }

}
