package org.aion4j.maven.avm.mojo;

import org.aion4j.maven.avm.util.ConfigUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AVMBaseMojo extends AbstractMojo {

    @Parameter(property = "avm-lib-dir", defaultValue = "${project.basedir}/lib")
    protected String avmLibDir;

    @Parameter(property = "dapp-jar", defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    protected String dappJar;

    @Parameter(property = "mode", defaultValue = "local")
    protected String mode;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "storage-path", defaultValue = "${project.build.directory}/storage")
    protected String storagePath;

    @Parameter(property = "local-default-address", defaultValue = "a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f725efc06378")
    protected String localDefaultAddress; //Pre-mine address

    @Parameter(property = "web3rpcUrl", defaultValue = "")
    protected String web3rpcUrl;

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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getLocalDefaultAddress() {
        return localDefaultAddress;
    }

    public void setLocalDefaultAddress(String localDefaultAddress) {
        this.localDefaultAddress = localDefaultAddress;
    }

    //This method should not be called directly from goals. Instead resolveWeb3rpcUrl() is the right one.
    public String getWeb3rpcUrl() {
        return web3rpcUrl;
    }

    public void setWeb3rpcUrl(String web3rpcUrl) {
        this.web3rpcUrl = web3rpcUrl;
    }

    public boolean isLocal() {
        if("local".equals(getMode()))
            return true;
        else
            return false;
    }

    //Only used for remote goals
    protected String resolveWeb3rpcUrl() throws MojoExecutionException {
        String web3RpcUrl = ConfigUtil.getPropery("web3rpc.url");

        //check if it's configured in pom.xml
        if(web3RpcUrl == null || web3RpcUrl.isEmpty())
            web3RpcUrl = getWeb3rpcUrl();

        if(web3RpcUrl == null || web3RpcUrl.isEmpty()) {
            getLog().error("web3rpc.url cannot be null. Please set it through -Dweb3rpc.url in maven command line or environment variable as web3rpc_url or " +
                    "in plugin configuration as web3rpcUrl property.");
            throw new MojoExecutionException("Invalid args. web3rpc.url cannot be null. Please set it through -Dweb3rpc.url in maven command line  or environment variable as web3rpc_url or " +
                   "in plugin configuration as web3rpcUrl property.");
        }

        getLog().info("web3rpc.url is set to " + web3RpcUrl);
        return web3RpcUrl;
    }

    protected String getAddress() throws MojoExecutionException {
        return ConfigUtil.getPropery("address");
    }

    protected long getGas() {
        String gasString = ConfigUtil.getPropery("gas");

        try {
            if (gasString != null && !gasString.isEmpty()) {
                return Long.parseLong(gasString.trim());
            } else
                return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    protected long getGasPrice() {
        String getPriceString = ConfigUtil.getPropery("gasPrice");

        try {
            if (getPriceString != null && !getPriceString.isEmpty()) {
                return Long.parseLong(getPriceString.trim());
            } else
                return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    protected String getPrivateKey() {
        if(isLocal()) //Private key is disabled for embedded AVM.
            return null;

        String pk = ConfigUtil.getPropery("pk");
        return pk;
    }
}
