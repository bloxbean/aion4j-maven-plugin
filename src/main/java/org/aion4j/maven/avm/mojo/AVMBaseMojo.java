package org.aion4j.maven.avm.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AVMBaseMojo extends AbstractMojo {

    @Parameter(property = "avm-lib-dir", defaultValue = "${project.basedir}/lib")
    protected String avmLibDir;

    @Parameter(property = "dapp-jar", defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    protected String dappJar;

//    @Parameter(property = "mode", defaultValue = "EMBED")
//    protected Mode mode;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "storage-path", defaultValue = "${project.build.directory}/storage")
    protected String storagePath;


    public String getAvmLibDir() {
        return avmLibDir;
    }

    public void setAvmLibDir(String avmLibDir) {
        this.avmLibDir = avmLibDir;
    }

    public String getDappJar() {
        return dappJar;
    }

    public void setDappJar(String dappJar) {
        this.dappJar = dappJar;
    }

//    public Mode getMode() {
//        return mode;
//    }
//
//    public void setMode(Mode mode) {
//        this.mode = mode;
//    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
}
