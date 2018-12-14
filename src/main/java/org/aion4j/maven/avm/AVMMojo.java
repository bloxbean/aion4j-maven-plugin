package org.aion4j.maven.avm;

import org.aion.cli.AvmCLI;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "avm-deploy")
public class AVMMojo extends AbstractMojo {

    @Parameter(property = "dapp-jar",defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    private String dappJar;

    public void execute()
        throws MojoExecutionException {
        getLog().info("Executing avm-deploy : ");

        String[] args = {"deploy", dappJar};
        AvmCLI.main(args);
    }

    public String getDappJar() {
        return dappJar;
    }

    public void setDappJar(String dappJar) {
        this.dappJar = dappJar;
    }
}
