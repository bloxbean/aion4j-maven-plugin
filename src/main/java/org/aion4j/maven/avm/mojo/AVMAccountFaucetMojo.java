package org.aion4j.maven.avm.mojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.aion4j.avm.helper.cache.global.AccountCache;
import org.aion4j.avm.helper.cache.global.GlobalCache;
import org.aion4j.avm.helper.crypto.Account;
import org.aion4j.avm.helper.crypto.AccountGenerator;
import org.aion4j.avm.helper.exception.RemoteAvmCallException;
import org.aion4j.avm.helper.faucet.FaucetService;
import org.aion4j.avm.helper.remote.RemoteAvmAdapter;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.avm.helper.util.CryptoUtil;
import org.aion4j.maven.avm.impl.DummyLog;
import org.aion4j.maven.avm.impl.MavenLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Mojo(name = "account", aggregator = true)
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

        boolean isTopUp = ConfigUtil.getAvmConfigurationBooleanProps("topup", false);
        boolean isCreate = ConfigUtil.getAvmConfigurationBooleanProps("create", false);
        boolean isList = ConfigUtil.getAvmConfigurationBooleanProps("list", false);
        boolean isListClear = ConfigUtil.getAvmConfigurationBooleanProps("list-clear", false);
        boolean isListWithBalance = ConfigUtil.getAvmConfigurationBooleanProps("list-with-balance", false);

        if(isListClear) { //If list ignore other commands
            if(isTopUp || isCreate)
            clearAccountCache();
            return;
        }

        if(isList) { //If list ignore other commands
            if(isTopUp || isCreate)
                getLog().warn("You can not use other commands with 'list'.");

            showAccountListFromCache(false);
            return;
        }

        if(isListWithBalance) { //If list ignore other commands
            if(isTopUp || isCreate)
                getLog().warn("You can not use other commands with 'list-with-balance'.");

            showAccountListFromCache(true);
            return;
        }

        String address = null;
        String pk = null;

        if(isCreate) {
            //Create a new account
            Account accountObj = generateClientSideAccount();
            address = accountObj.getAddress();
            pk = accountObj.getPrivateKey();

            writeAccountToCache(address, pk);
        }

        if(isTopUp) {
            web3rpcUrl = resolveWeb3rpcUrl();

            if(!isCreate) { //If only topup. Not create, get the address from -Daddress or system environment variable
                address = getAddress();
                if(address == null || address.trim().length() == 0)
                    throw new MojoExecutionException("Address can not be null. Please provide -Daddress=<address> or set the 'address' environment variable.");

                pk = ConfigUtil.getProperty("pk");
                if(pk == null || pk.trim().length() == 0)
                    throw new MojoExecutionException("Private key can not be null. Please provide private key through -Dpk=<private key> option");

            }

            executeTopup(address, pk);
        }

    }

    private void clearAccountCache() {
        GlobalCache globalCache = getGlobalAccountCache();
        globalCache.clearAccountCache();
        getLog().info("Account list was cleared successfully");
    }

    private void writeAccountToCache(String address, String pk) {
        GlobalCache globalCache = getGlobalAccountCache();
        AccountCache accountCache = globalCache.getAccountCache();
        accountCache.addAccount(new Account(address, pk));
        globalCache.setAccountCache(accountCache);
    }

    private void showAccountListFromCache(boolean showBalance) throws MojoExecutionException{
        RemoteAvmAdapter remoteAvmAdapter = null;
        if(showBalance) {//only initialize if show balance
            web3rpcUrl = resolveWeb3rpcUrl();
            remoteAvmAdapter = new RemoteAvmAdapter(web3rpcUrl, new DummyLog());
        }

        GlobalCache globalCache = getGlobalAccountCache();

        AccountCache accountCache = globalCache.getAccountCache();
        List<Account> accountList = accountCache.getAccounts();

        if(accountList.size() > 0) {
            getLog().info("Accounts :");
            int index = 0;
            for(Account account: accountList) {
                getLog().info("Account #" + ++index);
                getLog().info("    Address    : " + account.getAddress());
                getLog().info("    Private key: " + account.getPrivateKey());

                if(showBalance) { //Fetch balance for the account
                    BigInteger balance = null;
                    try {
                        balance = remoteAvmAdapter.getBalance(account.getAddress());
                        Double aionValue = CryptoUtil.convertAmpToAion(balance);

                        getLog().info(String.format("    Balance    : %s nAmp (%s Aion)", balance, String.format("%.12f",aionValue)));
                    }catch (Exception e) {
                        getLog().debug("Unable to fetch balance for account: " + account.getAddress(), e);
                        balance = BigInteger.ZERO;
                    }
                }
            }
        } else {
            getLog().info("No account to show");
        }
    }

    private GlobalCache getGlobalAccountCache() {
        return new GlobalCache(getAccountCacheFolder(), MavenLog.getLog(getLog()));
    }

    private void executeTopup(String account, String pk) throws MojoExecutionException {
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

    private Account generateClientSideAccount() {
        Account account = AccountGenerator.newAddress();

        String address = account.getAddress();

        if(!address.startsWith("0x"))
            address = "0x" + address;

        getLog().info(String.format("Account creation successful"));
        getLog().info("Address : " + address);
        getLog().info("Private Key: " + account.getPrivateKey());

        return account;
    }

    private String getAccountCacheFolder() {
        String home = System.getProperty("user.home");

        File cacheFolder = new File(home);

        if(!cacheFolder.canWrite()) {
            //Let's find temp folder
            String temp = System.getProperty("java.io.tmpdir");
            File tempFolder = new File(temp);

            if(!tempFolder.canWrite()) {
                getLog().warn("Unable to find a writable folder to keep account list cache");
                return null;
            } else {
                cacheFolder = tempFolder;
            }
        }

        File aion4jFolder = new File(cacheFolder, ".aion4j");
        if(!aion4jFolder.exists()) {
            aion4jFolder.mkdirs();
        }

        return aion4jFolder.getAbsolutePath();
    }

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {

    }

}