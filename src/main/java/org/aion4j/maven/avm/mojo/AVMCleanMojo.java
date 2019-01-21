package org.aion4j.maven.avm.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Mojo(name = "clean", defaultPhase = LifecyclePhase.CLEAN)
public class AVMCleanMojo extends AVMBaseMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String storageDir = project.getBasedir() + File.separator + "storage";

        try {
            Path pathToBeDeleted = Paths.get(storageDir);

            if(Files.exists(pathToBeDeleted)) {
                getLog().info("Deleting storage folder " + storageDir);
                Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to delete folder " + storageDir);
        }
    }


}
