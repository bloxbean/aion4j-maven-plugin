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
import org.aion4j.avm.helper.faucet.NetworkHelper;
import org.aion4j.avm.helper.faucet.model.Network;
import org.aion4j.avm.helper.remote.RemoteAvmAdapter;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.avm.helper.util.CryptoUtil;
import org.aion4j.avm.helper.util.StringUtils;
import org.aion4j.maven.avm.adapter.LocalAvmAdapter;
import org.aion4j.maven.avm.impl.DummyLog;
import org.aion4j.maven.avm.impl.MavenLog;
import org.aion4j.maven.avm.ipc.IPCAccount;
import org.aion4j.maven.avm.ipc.IPCResultWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "account", aggregator = true)
public class AVMAccountFaucetMojo extends AVMLocalRuntimeBaseMojo {

    private final static String FAUCET_WEB_URL = "http://faucet-web.aion4j.org/";
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
    public void executeRemote() throws MojoExecutionException {

        boolean isTopUp = ConfigUtil.getAvmConfigurationBooleanProps("topup", false);
        boolean isCreate = ConfigUtil.getAvmConfigurationBooleanProps("create", false);
        boolean isList = ConfigUtil.getAvmConfigurationBooleanProps("list", false);
        boolean isListClear = ConfigUtil.getAvmConfigurationBooleanProps("list-clear", false);
        boolean isListWithBalance = ConfigUtil.getAvmConfigurationBooleanProps("list-with-balance", false);

        getLog().info("Mode : remote (Aion Kernel)");

        if(isListClear) { //If list ignore other commands
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
        boolean ignoreCache = ConfigUtil.getAvmConfigurationBooleanProps("ignore-cache", false);

        if(!ignoreCache) {
            GlobalCache globalCache = getGlobalAccountCache();
            AccountCache accountCache = globalCache.getAccountCache();
            accountCache.addAccount(new Account(address, pk));
            globalCache.setAccountCache(accountCache);
        }
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

        //only required if we need to persist the result. So that the caller can read that from filesystem.
        List<IPCAccount> ipcAccountList = new ArrayList();
        String output = ConfigUtil.getProperty("output"); //only required for ipc call. For exp: IDE to maven process

        if(accountList.size() > 0) {
            getLog().info("Accounts :");
            int index = 0;
            for(Account account: accountList) {
                getLog().info("Account #" + ++index);
                getLog().info("    Address    : " + account.getAddress());
                getLog().info("    Private key: " + account.getPrivateKey());

                IPCAccount ipcAccount = new IPCAccount(account);
                if(showBalance) { //Fetch balance for the account
                    BigInteger balance = null;
                    try {
                        balance = remoteAvmAdapter.getBalance(account.getAddress());
                        BigDecimal aionValue = CryptoUtil.ampToAion(balance);

                        getLog().info(String.format("    Balance    : %s nAmp (%s Aion)", balance, String.format("%.12f",aionValue.floatValue())));
                    }catch (Exception e) {
                        getLog().debug("Unable to fetch balance for account: " + account.getAddress(), e);
                        balance = BigInteger.ZERO;
                    }

                    ipcAccount.setBalance(balance);
                }

                ipcAccountList.add(ipcAccount);
            }

            try {
                if (!StringUtils.isEmpty(output)) { //If it's an ipc call, write the result json
                    IPCResultWriter.saveAccountList(ipcAccountList, output, getLog());
                }
            }catch (Exception e) {}
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
        getLog().info("You can only send topup request maximum 3 times per account in 24hrs.");
        getLog().info("Your transaction will fail if you exceed that limit.");
        getLog().info("##############################################################################################################################");
        //Check account's balance

        FaucetService faucetService = new FaucetService(getLocalAVMClass().getClassLoader(), web3rpcUrl, getFaucetWebUrl(), getFaucetContractAddress(), MavenLog.getLog(getLog()));
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

    /**
     *
     * @return Return faucet contract address if it's set in environment, otherwise null
     * @throws MojoExecutionException
     */
    private String getFaucetContractAddress() {
        String faucetContract = ConfigUtil.getProperty("faucet.contract");
        if(!StringUtils.isEmpty(faucetContract)) {
            getLog().info("Faucet contract address : " + faucetContract);
            return faucetContract;
        } else {
            return null;
        }
    }

    private String getFaucetWebUrl() {
        String faucetWebUrl = ConfigUtil.getProperty("faucet.web.url");
        if(!StringUtils.isEmpty(faucetWebUrl))
            return faucetWebUrl;
        else
            return FAUCET_WEB_URL;
    }

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {
        boolean isTopUp = ConfigUtil.getAvmConfigurationBooleanProps("topup", false);
        boolean isCreate = ConfigUtil.getAvmConfigurationBooleanProps("create", false);
        boolean isList = ConfigUtil.getAvmConfigurationBooleanProps("list", false);
        boolean isListClear = ConfigUtil.getAvmConfigurationBooleanProps("list-clear", false);
        boolean isListWithBalance = ConfigUtil.getAvmConfigurationBooleanProps("list-with-balance", false);
        String balance = ConfigUtil.getProperty("balance");
        String addressToCreate = ConfigUtil.getProperty("address");

        LocalAvmAdapter localAvmAdapter = new LocalAvmAdapter(localAvmInstance);

        getLog().info("Mode : local (Embedded Avm)");

        if(isListClear) { //If list ignore other commands
            clearAccountCache();
            return;
        }

        if(isList) { //If list ignore other commands
            if(isTopUp || isCreate)
                getLog().warn("You can not use other commands with 'list'.");

            showAccountListFromCacheLocal(localAvmAdapter, false);
            return;
        }

        if(isListWithBalance) { //If list ignore other commands
            showAccountListFromCacheLocal(localAvmAdapter, true);
            return;
        }

        if(isTopUp) {
            if(StringUtils.isEmpty(addressToCreate)) {
                throw new MojoExecutionException("Address cannot be null for topup. \nUsage: mvn aion4j:account -Dtopup -Daddress=<address> -Dbalance=<amount>");
            }

            if(StringUtils.isEmpty(balance)) {
                throw new MojoExecutionException("Balance cannot be null for topup. \nUsage: mvn aion4j:account -Dtopup -Daddress=<address> -Dbalance=<amount>");
            }

            getLog().info(String.format("Trying to transfer %s to the address ...", balance));

            boolean response = localAvmAdapter.transfer(addressToCreate, new BigInteger(balance));

            if(response) {
                BigInteger newBal = localAvmAdapter.getBalance(addressToCreate);

                getLog().info("Topup was successful.");
                getLog().info("Address   : " + addressToCreate);
                getLog().info("Balance   : " + newBal);
            }
            return;
        }

        try {
            if(isCreate) {
                getLog().info("Generate a new account");

                Account account = AccountGenerator.newAddress();
                addressToCreate = account.getAddress();

                //Write to cache
                writeAccountToCache(account.getAddress(), account.getPrivateKey());

                getLog().info(String.format("Account creation successful"));
                getLog().info("Address       : " + addressToCreate);

                if (account != null)
                    getLog().info("Private Key   : " + account.getPrivateKey());

                if (!StringUtils.isEmpty(balance)) { //Let's assign some balance in local Avm

                    boolean response = localAvmAdapter.createAccountWithBalance(addressToCreate, new BigInteger(balance));
                    if (response) {
                        getLog().info("Balance(nAmp) : " + balance.trim());
                    } else {
                        getLog().info("Balance allocation failed. Please check if account exists");
                    }
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating account with balance", e);
        }
    }

    private void showAccountListFromCacheLocal(LocalAvmAdapter localAvmAdapter, boolean showBalance) throws MojoExecutionException{

        GlobalCache globalCache = getGlobalAccountCache();

        AccountCache accountCache = globalCache.getAccountCache();
        List<Account> accountList = accountCache.getAccounts();

        //only required if we need to persist the result. So that the caller can read that from filesystem.
        List<IPCAccount> ipcAccountList = new ArrayList();
        String output = ConfigUtil.getProperty("output"); //only required for ipc call. For exp: IDE to maven process

        if(accountList.size() > 0) {
            getLog().info("Accounts :");
            int index = 0;
            for(Account account: accountList) {
                getLog().info("Account #" + ++index);
                getLog().info("    Address    : " + account.getAddress());
                getLog().info("    Private key: " + account.getPrivateKey());

                IPCAccount ipcAccount = new IPCAccount(account);
                if(showBalance) { //Fetch balance for the account
                    BigInteger balance = null;
                    try {
                        balance = localAvmAdapter.getBalance(account.getAddress());
                        BigDecimal aionValue = CryptoUtil.ampToAion(balance);

                        getLog().info(String.format("    Balance    : %s nAmp (%s Aion)", balance, String.format("%.12f",aionValue.floatValue())));
                    }catch (Exception e) {
                        getLog().debug("Unable to fetch balance for account: " + account.getAddress(), e);
                        balance = BigInteger.ZERO;
                    }

                    ipcAccount.setBalance(balance);
                }

                ipcAccountList.add(ipcAccount);
            }

            try {
                if (!StringUtils.isEmpty(output)) { //If it's an ipc call, write the result json
                    IPCResultWriter.saveAccountList(ipcAccountList, output, getLog());
                }
            } catch (Exception e) {}
        } else {
            getLog().info("No account to show");
        }


    }


}
