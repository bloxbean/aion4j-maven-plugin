package org.aion4j.maven.avm.util;

import java.io.*;
import java.util.Properties;

public class DeployResultConfig {

    public static String statusFileName = ".aion4j.conf";

    public static String DEPLOY_ADDRESS = "deploy.address";

    public static void updateDeployAddress(String targetFolder, String address) {
        Properties props = DeployResultConfig.readResults(targetFolder);

        if(props == null)
            props = new Properties();

        props.setProperty(DEPLOY_ADDRESS, address);

        writeResults(targetFolder, props);
    }

    public static String getLastDeployedAddress(String targetFolder) {
        Properties props = DeployResultConfig.readResults(targetFolder);

        if(props == null)
            props = new Properties();

        return props.getProperty(DEPLOY_ADDRESS);
    }

    public static void writeResults(String targetFolder, Properties props) {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream(new File(targetFolder, statusFileName));

            props.store(output, null);

        } catch (Exception io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static Properties readResults(String targetFolder) {
        InputStream input = null;

        try {

            File deployResultFile = new File(targetFolder, statusFileName);

            if(!deployResultFile.exists())
                return new Properties();

            input = new FileInputStream(new File(targetFolder, statusFileName));

            Properties properties = new Properties();
            properties.load(input);

            return properties;

        } catch (Exception io) {
            io.printStackTrace();
            return new Properties();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
