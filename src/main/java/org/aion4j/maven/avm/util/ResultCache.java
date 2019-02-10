package org.aion4j.maven.avm.util;

import java.io.*;
import java.util.Properties;

public class ResultCache {

    public static String statusFileName = ".aion4j.conf";

    public static String DEPLOY_ADDRESS = "deploy.address";
    public static String DEPLOY_TX_RECEIPT = "deploy.tx.receipt";
    public static String TX_RECEIPT = "last.tx.receipt";

    private String projectName;
    private String targetFolder;

    public ResultCache(String projectName, String targetFolder) {
        this.projectName = projectName;
        this.targetFolder = targetFolder;
    }

    public void updateDeployAddress(String address) {
        updateProperty(DEPLOY_ADDRESS, address);
    }

    public void updateDeployTxnReceipt(String txHash) {
        updateProperty(DEPLOY_TX_RECEIPT, txHash);
        updateProperty(TX_RECEIPT, txHash); //also set lastTxn receipt
        updateProperty(DEPLOY_ADDRESS, ""); //reset deploy address as it's a new deployment.
    }

    public void updateTxnReceipt(String txHash) {
        updateProperty(TX_RECEIPT, txHash);
    }

    private void updateProperty(String propertyName, String address) {
        Properties props = this.readResults(targetFolder);

        if(props == null)
            props = new Properties();

        props.setProperty(resolvePropertyName(projectName, propertyName), address);

        writeResults(targetFolder, props);
    }

    public String getLastDeployedAddress() {
        return getPropertyValue(DEPLOY_ADDRESS);
    }

    public String getLastDeployTxnReceipt() {
        return getPropertyValue(DEPLOY_TX_RECEIPT);
    }

    public String getLastTxnReceipt() {
        return getPropertyValue(TX_RECEIPT);
    }

    private String getPropertyValue(String propertyName) {
        Properties props = this.readResults(targetFolder);

        if(props == null)
            props = new Properties();

        return props.getProperty(resolvePropertyName(projectName, propertyName));
    }

    private void writeResults(String targetFolder, Properties props) {
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

    private Properties readResults(String targetFolder) {
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

    private String resolvePropertyName(String projectName, String propName) {
        return projectName + "." + propName;
    }

}
