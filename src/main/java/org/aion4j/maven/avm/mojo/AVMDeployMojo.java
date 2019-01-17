package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.local.LocalAvmNode;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

@Mojo(name = "deploy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AVMDeployMojo extends AVMBaseMojo {

    private final static String SUCCESS_TEXT = "Result status: SUCCESS";

    public void execute() throws MojoExecutionException {

        getLog().info("Executing avm-deploy : ");

        //check if dAppJar exists
        Path path = Paths.get(getDappJar());
        if (!Files.exists(path)) {
            throw new MojoExecutionException(String.format("Dapp jar file doesn't exist : %s \n"
                + "Please make sure you have built the project.", dappJar));
        }

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
        if(pluginJar != null) {
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        // IMPORTANT: Save the old System.out!
        PrintStream oldPs = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);

        try {

            //Class clazz = classLoader.loadClass("org.aion.cli.AvmCLI");
            //final Method method = clazz.getMethod("main", String[].class);

            Class clazz = classLoader.loadClass("org.aion4j.maven.avm.local.LocalAvmNode");
            final Method method = clazz.getMethod("deploy", String.class);

            final Object[] args = new Object[1];
            args[0] = new String[]{dappJar};

            Object instance = clazz.newInstance();

            getLog().info(String.format("Deploying %s to the embedded AVM ...", getDappJar()));
            Object response = method.invoke(instance, dappJar);

            Method getAddressMethod = response.getClass().getMethod("getAddress");
            Method getEnergyUsed = response.getClass().getMethod("getEnergyUsed");

            getLog().info("Dapp address: " + getAddressMethod.invoke(response));
            getLog().info("Energy used: " + getEnergyUsed.invoke(response));

            getLog()
                .info(String.format("%s deployed successfully to the embedded AVM.", getDappJar()));

        } catch (Exception ex) {
            getLog()
                .error(String.format("%s could not be deployed to the embedded AVM.", getDappJar()),
                ex);
            throw new MojoExecutionException("Dapp jar deployment failed", ex);
        } finally {
            System.setOut(oldPs);
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }

    }

    //Get the maven plugin jar file location to solve parent->child classloader issue
    private URL getLocalAVMNodeClassJarLocation() {
        CodeSource src = LocalAvmNode.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            return jar;
        }

        return null;
    }

    private boolean isError(String result) throws MojoExecutionException {

        if(result != null) {
            if(!result.contains(SUCCESS_TEXT)) {
                return true;
            } else
                return false;
        } else
            return false;
    }

   /* private ClassLoader getClassLoader(MavenProject project, URL avmUrl)
    {
        try
        {
            List classpathElements = project.getCompileClasspathElements();
            classpathElements.add( project.getBuild().getOutputDirectory() );
            classpathElements.add( project.getBuild().getTestOutputDirectory() );

            List<URL> urls = new ArrayList();
//            URL urls[] = new URL[classpathElements.size()];
            for ( int i = 0; i < classpathElements.size(); ++i )
            {
                urls.add(new File( (String) classpathElements.get( i ) ).toURL());
            }

            urls.add(avmUrl);

            return new URLClassLoader( urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader() );
        }
        catch ( Exception e )
        {
            getLog().debug( "Couldn't get the classloader." );
            return this.getClass().getClassLoader();
        }
    }*/
}
