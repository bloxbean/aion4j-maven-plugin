package org.aion4j.maven.avm.it.unit;

import org.aion.cli.AvmCLI;
import org.aion4j.maven.avm.api.DeployResponse;
import org.aion4j.maven.avm.exception.DeploymentFailedException;
import org.aion4j.maven.avm.local.LocalAvmNode;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

public class Test {

    public static void deploy() throws DeploymentFailedException {

       LocalAvmNode localAvmNode = new LocalAvmNode("storage", "0xa0642f8f7eb09b45955c82c5c23b8cf617a2fdda53a6dd469aa85e01b3f21280");
       localAvmNode.deploy("/Users/OsDev/work/avm-quickstarter/target/avm-quickstarter-1.0-SNAPSHOT.jar");

    }
    public static void main(String[] args) throws DeploymentFailedException {
        deploy();
    }
}
