package org.aion4j.maven.avm.mojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nettgryppa.security.HashCash;
import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.maven.avm.faucet.Challenge;
import org.aion4j.maven.avm.faucet.TopupResult;
import org.aion4j.maven.avm.impl.DummyLog;
import org.aion4j.maven.avm.impl.MavenLog;
import org.aion4j.maven.avm.impl.RemoteAvmAdapter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void execute() throws MojoExecutionException {

        String account = getAddress();
        if(account == null || account.trim().length() == 0)
            throw new MojoExecutionException("Address can not be null. Please provide -Daddress=<address> or set the 'address' environment variable.");

        String pk = ConfigUtil.getProperty("pk");
        if(pk == null || pk.trim().length() == 0)
            throw new MojoExecutionException("Private key can not be null. Please provide private key through -Dpk=<private key> option");

        web3rpcUrl = resolveWeb3rpcUrl();

        getLog().info("Start AION topup for address : " + account);
        //Check account's balance

        RemoteAvmAdapter remoteAvmAdapter = new RemoteAvmAdapter(web3rpcUrl, new DummyLog()); //Dummy log as we don't want to show detailed log

        boolean isFaucetWebCall = isFacetWebCallRequired(remoteAvmAdapter, account);

        if(isFaucetWebCall) { //Make faucet web call for new account
            getLog().info("Let's register the address and get some minimum AION coins through Faucet Web");
            allocateInitialBalanceThroughFaucetWeb(account, web3rpcUrl);
        }

//
//        getLog().info("New balance : " + balance.toString());

        //Invoke
        getLog().info("Let's get some coin from the Faucet contract");

        try {
            invokeContractForBalanceTopup(pk, account);
        } catch (Exception e) {
            getLog().error("Account topup failed", e);
        }

        BigInteger balance = remoteAvmAdapter.getBalance(account);

        if(balance == null || BigInteger.ZERO.equals(balance)) {
            getLog().warn("Could not send some initial AION coins to the address");
            throw new MojoExecutionException("Topup registration failed for address : " + account);
        }

        getLog().info("New balance: " + balance);

        return;

    }

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

    }

    private void allocateInitialBalanceThroughFaucetWeb(String account, String web3RpcUrl) throws MojoExecutionException {
        //Get challenge from Faucet server
        Challenge challenge = null;
        try {
            getLog().info("Fetching challenge from the Faucet web server ....");
            challenge = getChallenge();
        } catch (UnirestException ex) {
            getLog()
                    .error(String.format("Get challenge failed"),
                            ex);
            throw new MojoExecutionException("Get challenge failed", ex);
        }

        if(challenge == null)
            throw new MojoExecutionException("Get challenge failed");

        Map<String, List<String>> extensions = new HashMap<>();
        List<String> extensionList = new ArrayList<>();
        extensionList.add(String.valueOf(challenge.getCounter()));
        extensionList.add(account);

        //add counter to extension
        extensions.put("data", extensionList);

        getLog().info("Start genereting proof with value " + challenge.getValue());
        //Start minting hashcash
        long t1 = System.currentTimeMillis();
        HashCash cash = null;
        try {
            cash = HashCash.mintCash(challenge.getMessage(), extensions, challenge.getValue(), 1);
        } catch (NoSuchAlgorithmException e) {
            getLog().error("Error generating proof",e);
            throw new MojoExecutionException("Error generating proof");
        }

        if(cash == null) {
            throw new MojoExecutionException("Error generating proof");
        }

        long t2 = System.currentTimeMillis();

        getLog().info("Time spent in minting proof : " + (t2 - t1) / 1000 + "sec");

        getLog().info("Send hascash proof to server : " + cash.toString());

        TopupResult topupResult = null;
        try {
            topupResult = submitHashCash(account, cash);
            if(topupResult != null) {
                getLog().info("Register result >> " + topupResult);
            } else {
                getLog().error("Account could not be credited");
                throw new MojoExecutionException("Error in crediting account");
            }
        } catch (UnirestException e) {
            getLog().error("Topup failed for address : " + account,e);

            throw new MojoExecutionException("Topup failed for address : " + account);
        }

        //Let's try to get receipt
        AVMGetReceiptMojo.startGetReceipt(web3RpcUrl, topupResult.getTxHash(), "tail", "silent", getCache(), getLog());
    }

    private void invokeContractForBalanceTopup(String pk, String account) throws MojoExecutionException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class localAvmClazz = getLocalAVMClass();
        //Lets do method call encoding

        Method encodeMethodCallMethod = localAvmClazz.getMethod("encodeMethodCall", String.class, Object[].class);

        String encodedMethodCall = (String)encodeMethodCallMethod.invoke(null, "topUp", new Object[0]);

        getLog().info("Encoded method call data: " + encodedMethodCall);

        RemoteAVMNode remoteAVMNode = new RemoteAVMNode(web3rpcUrl, MavenLog.getLog(getLog()));

        String retData = null;

        retData = remoteAVMNode.sendRawTransaction(FAUCET_CONTRACT_ADDRESS, pk, encodedMethodCall, BigInteger.ZERO, defaultGas , defaultGasPrice);

        if(retData != null) {
            //Let's try to get receipt
            AVMGetReceiptMojo.startGetReceipt(web3rpcUrl, retData, "tail", "silent", getCache(), getLog());
        }

    }

    //This is needed if the account is a new account with balance zero
    private boolean isFacetWebCallRequired(RemoteAvmAdapter remoteAvmAdapter, String address) {
        BigInteger balance = remoteAvmAdapter.getBalance(address);

        getLog().info("Fetched existing balance for the account : " + balance);

        if(balance == null || BigInteger.ZERO.equals(balance)) {
            getLog().debug("Address balance is null. Let's try to get some minimum balance for the account through Faucet Web.");
            return true;
        } else {
            return false;
        }
    }

    private TopupResult submitHashCash(String account, HashCash hashCash) throws UnirestException {
        HttpResponse<TopupResult> httpResponse =  Unirest.post(FAUCET_WEB_URL + "/register")
                .header("Content-Type", "text/plain")
                .body(hashCash.toString())
                .asObject(TopupResult.class);

        if(httpResponse.getStatus() != 200) {
            return null;
        } else {
            return httpResponse.getBody();
        }

    }

    private Challenge getChallenge() throws UnirestException {
        return Unirest.get(FAUCET_WEB_URL + "/challenge")
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .asObject(Challenge.class).getBody();
    }
}
