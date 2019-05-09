package org.aion4j.maven.avm.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.aion4j.avm.helper.util.ConfigUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "init", defaultPhase = LifecyclePhase.VALIDATE)
public class AVMInitializeMojo extends AVMBaseMojo {

    private final static String AVM_RESOURCE_FOLDER = "/lib/avm";
    private final static String AVM_JAR = "avm.jar";
    private final static String AVM_API_JAR = "org-aion-avm-api.jar";
    private final static String AVM_USERLIB_JAR = "org-aion-avm-userlib.jar";
    private final static String AVM_TOOLING_JAR = "org-aion-avm-tooling.jar";
    private final static String VERSION_FILE = "version";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String libFolderPath = getAvmLibDir();
        File libFolder = new File(libFolderPath);

        boolean forceCopy = ConfigUtil.getAvmConfigurationBooleanProps("forceCopy", false);

        if(!libFolder.exists()) {
            libFolder.mkdirs();

            if(!libFolder.exists()) {
                throw new MojoExecutionException("Unable to create library folder %s. "
                    + "Please check the directory permission and try again.", libFolderPath, null);
            }
        }

        //only copy version file if the libraries are copied from bundled version
        boolean bundledVersion = false;

        if (!checkIfLibExists(AVM_JAR) || forceCopy) {
            getLog().info(String
                .format("Copying default %s to %s folder.", AVM_JAR,
                    getAvmLibDir()));
            copyLibJar(AVM_JAR, AVM_RESOURCE_FOLDER + "/" + AVM_JAR, getAvmLibDir());

            bundledVersion = true;
        }

        if (!checkIfLibExists(AVM_API_JAR) || forceCopy) {
            getLog().info(String
                .format("Copying default %s to %s folder.",
                    AVM_API_JAR, getAvmLibDir()));
            copyLibJar(AVM_API_JAR, AVM_RESOURCE_FOLDER + "/" + AVM_API_JAR,
                getAvmLibDir());
        }

        if (!checkIfLibExists(AVM_USERLIB_JAR) || forceCopy) {
            getLog().info(String
                .format("Copying default %s to %s folder.",
                    AVM_USERLIB_JAR, getAvmLibDir()));
            copyLibJar(AVM_USERLIB_JAR, AVM_RESOURCE_FOLDER + "/" + AVM_USERLIB_JAR,
                getAvmLibDir());
        }

        if (!checkIfLibExists(AVM_TOOLING_JAR) || forceCopy) {
            getLog().info(String
                    .format("Copying default %s to %s folder.",
                            AVM_TOOLING_JAR, getAvmLibDir()));
            copyLibJar(AVM_TOOLING_JAR, AVM_RESOURCE_FOLDER + "/" + AVM_TOOLING_JAR,
                    getAvmLibDir());
        }

        if ((!checkIfLibExists(VERSION_FILE) || forceCopy) && bundledVersion) {
            getLog().info(String
                .format("Copying default %s to %s folder.",
                    VERSION_FILE, getAvmLibDir()));
            copyLibJar(VERSION_FILE, AVM_RESOURCE_FOLDER + "/" + VERSION_FILE,
                getAvmLibDir());
        }
    }

    private boolean checkIfLibExists(String libFileName) {
        File libFile = new File(avmLibDir, libFileName);

        return libFile.exists();
    }

    private String copyLibJar(String jarName, String jarFilePath, String destFolder)
        throws MojoExecutionException {

        if (jarFilePath == null) {
            return null;
        }

        // Grab the file name
        String[] chopped = jarFilePath.split("\\/");
        String fileName = chopped[chopped.length - 1];

        // See if we already have the file
//        if (checkIfLibExists(fileName)) {
//            return null;
//        }

        InputStream fileStream = null;
        OutputStream out = null;
        try {
            // Read the file we're looking for
            fileStream = AVMInitializeMojo.class.getResourceAsStream(jarFilePath);

            if (fileStream == null) {
                throw new RuntimeException(String.format("%s is not found in the plugin jar. ", jarFilePath));
                //return null;
            }

            File targetFile
                = new File(destFolder, fileName);

            out = new FileOutputStream(targetFile);

            // Write the file to the temp file
            byte[] buffer = new byte[1024];
            int len = fileStream.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = fileStream.read(buffer);
            }

            // Return the path of this sweet new file
            return targetFile.getAbsolutePath();

        } catch (IOException e) {
            throw new MojoExecutionException("Error copying " + jarName + "to " + destFolder, e);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
    }
}
