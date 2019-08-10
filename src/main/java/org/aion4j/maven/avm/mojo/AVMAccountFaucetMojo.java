package org.aion4j.maven.avm.mojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.aion4j.avm.helper.exception.RemoteAvmCallException;
import org.aion4j.avm.helper.faucet.FaucetService;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;

@Mojo(name = "account-topup", aggregator = true)
public class AVMAccountFaucetMojo extends AVMLocalRuntimeBaseMojo {

    private final static String FAUCET_WEB_URL = "http://faucet-web.aion4j.org/";
    private final static String FAUCET_CONTRACT_ADDRESS = "0xa0089bdb72c1472e1b109a48efa4ae640a9d2667eb5ae69221bf18984f8a90a2";

    private long defaultGas = 2000000;
    private long defaultGasPrice = 100000000000L;

    public AVMAccountFaucetMojo() {
        Unirest.setObjectMapper(new ObjectMapper() {
            com.fasterxml.jackson.databind.ObjectMapper mapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public String writeValue(Object value) {
                try {
                    return mapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return mapper.readValue(value, valueType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String account = getAddress();
        if(account == null || account.trim().length() == 0)
            throw new MojoExecutionException("Address can not be null. Please provide -Daddress=<address> or set the 'address' environment variable.");

        String pk = ConfigUtil.getProperty("pk");
        if(pk == null || pk.trim().length() == 0)
            throw new MojoExecutionException("Private key can not be null. Please provide private key through -Dpk=<private key> option");

        web3rpcUrl = resolveWeb3rpcUrl();

        getLog().info("\n");
        getLog().info("Start AION topup for address : " + account);
        getLog().info("##############################################################################################################################");
        getLog().info("You can only send topup request maximum 3 times in 24hrs.");
        getLog().info("Your transaction will fail if you exceed that limit.");
        getLog().info("##############################################################################################################################");
        //Check account's balance

        FaucetService faucetService = new FaucetService(getLocalAVMClass().getClassLoader(), web3rpcUrl, FAUCET_WEB_URL, FAUCET_CONTRACT_ADDRESS, MavenLog.getLog(getLog()));
        faucetService.setDefaultGas(defaultGas);
        faucetService.setDefaultGasPrice(defaultGasPrice);

        try {
            faucetService.topup(account, pk);
        } catch (RemoteAvmCallException e) {
           // getLog().error("Account topup failed", e);
            throw new MojoExecutionException("Account topup failed", e);
        }

    }

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

    }


}
