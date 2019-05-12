package org.aion4j.maven.avm.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "help")
public class AVMHelpMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        boolean isLocal = Boolean.parseBoolean(System.getProperty("local"));
        boolean isRemote = Boolean.parseBoolean(System.getProperty("remote"));
        boolean isCommon = Boolean.parseBoolean(System.getProperty("common"));
        String command = System.getProperty("command");

        System.out.println("");
        System.out.println("############ Aion4j Maven Plugin Help #############");

        if(isCommon  ||  (!isLocal && !isRemote && !isCommon))
            commonCommands();

        if(isLocal  ||  (!isLocal && !isRemote && !isCommon))
            printLocalCommands(command);

        if(isRemote  ||  (!isLocal && !isRemote && !isCommon))
            printRemoteCommands(command);

    }

    public void commonCommands() {
        System.out.println("");
        System.out.println("Common commands");

        printCommonCommands();
    }

    public void printLocalCommands(String command) {
        System.out.println("");
        System.out.println("Embedded AVM commands");

        if(command == null || "create-account".equals(command)) {
            createAccountLocal();
        }

        if(command == null || "get-balance".equals(command)) {
            getBalanceLocal();
        }

        if(command == null || "deploy".equals(command)) {
            deployLocal();
        }

        if(command == null || "call".equals(command)) {
            callLocal();
        }
    }

    private void callLocal() {
        System.out.println("\t- Call contract method");
        System.out.println("\t\t$> mvn aion4j:call -Dmethod=<method_name> [-Dargs=\"<arg1_type> <arg1_value> ... <argn_type> <argn_value>\"]" +
                " [-Dvalue=<value>] [-Daddress=<sender>] [-Dcontract=<contract_address>]");
        System.out.println("\t\tExamples:");
        System.out.println("\t\t\t$> mvn aion4j:call -Dmethod=setData -Dargs=\"-T 'Hello World'\" -I 4");
        System.out.println("\t\t\t$> mvn aion4j:call -Dmethod=someMethod -Dargs=\"-T hello -I 6 8 -I[] 5 8 -I[][] 5,6,7 11,22,43\"");
    }

    private void deployLocal() {
        System.out.println("\t- Deploy contract jar");
        System.out.println("\t\t$> mvn aion4j:deploy [-Dargs=\"<arg1_type> <arg1_value> ... <argn_type> <argn_value>\"] [-Dvalue=<value>]");
        System.out.println("\t\tExamples:");
        System.out.println("\t\t$> mvn aion4j:deploy");
        System.out.println("\t\t- Deploy contract jar with deployment args");
        System.out.println("\t\t$> mvn aion4j:deploy [-Dargs=\"<arg1_type> <arg1_value> ... <argn_type> <argn_value>\"]");
        System.out.println("\t\tExamples:");
        System.out.println("\t\t\t$> mvn aion4j:deploy -Dargs=\"-T 'Hello World'-I 4\"");
        System.out.println("\t\t\t$> mvn aion4j:deploy -Dargs=\"-T hello -I 6 8 -I[] 5 8 -I[][] 5,6,7 11,22,43\"");
    }

    private void getBalanceLocal() {
        System.out.println("\t- Get balance of the default address");
        System.out.println("\t\t$> mvn aion4j:get-balance");

        System.out.println("\t- Get balance of the specified address");
        System.out.println("\t\t$> mvn aion4j:get-balance -Daddress=<address>");
    }

    private void createAccountLocal() {
        System.out.println("\t- Create a new aion address");
        System.out.println("\t  Return: New address and Private Key");
        System.out.println("\t\t$> mvn aion4j:create-account");

        System.out.println("\t- Create a new aion address and set balance");
        System.out.println("\t\t$> mvn aion4j:create-account -Dbalance=<balance>");

        System.out.println("\t- Set balance to a provided address");
        System.out.println("\t\t$> mvn aion4j:create-account -Daddress=<address> -Dbalance=<balance>");
    }

    private void printCommonCommands() {
        System.out.println("\t- Aion4j help");
        System.out.println("\t\t$> mvn aion4j:help [-D<local|remote|common>] [-Dcommand=<command>]");
        System.out.println("\t\tExample:");
        System.out.println("\t\t\t$> mvn aion4j:help  - Get all helps ");
        System.out.println("\t\t\t$> mvn aion4j:help -Dlocal  - Get help for all embedded AVM commands");
        System.out.println("\t\t\t$> mvn aion4j:help -Dremote  - Get help for all remote kernel commands");

        System.out.println("\t- To initialize a project with avm jars");
        System.out.println("\t\t$> mvn initialize");
        System.out.println("\t- To initialize a project with avm jars. Overwrite any existing jars.");
        System.out.println("\t\t$> mvn initialize -DforceCopy");
    }


    private void printRemoteCommands(String command) {
        System.out.println("");
        System.out.println("Remote Kernel Commands");
        System.out.println("\t- Usage");
        System.out.println("\t\t$> mvn aion4j:<command> [-Dweb3rpc.url=<web3rpc.url>] [-Dpk=<private_key>] [-Dgas=<gas>] [-DgasPrice=<gasPrice>] <args...> -P<remote_profile> ");
        System.out.println("\t\t*web3rpc.url properties can be :");
        System.out.println("\t\t\t- set in pom.xml's configuration section of aion4j-maven-plugin");
        System.out.println("\t\t\t- set as web3rpc_url environment variable");
        System.out.println("\t\t\t- passed as -Dweb3prc.url in the command line");
        System.out.println("\t\t*pk properties can be :");
        System.out.println("\t\t\t- set as \"pk\" environment variable");
        System.out.println("\t\t\t- passed as -Dpk in the command line");
        System.out.println("\t\t*The following properties can also be set as environment variable whereever applicable");
        System.out.println("\t\t\t- address : Aion address");
        System.out.println("\t\t\t- gas : Nrg limit");
        System.out.println("\t\t\t- gasPrice : Nrg price");
        System.out.println("\t\t\t- contract : Contract address");
        System.out.println("\t\t*web3rpc.url, pk arguments are ommitted in the below commands.");
        System.out.println("\t\t*By default, remote_profile = remote");
        System.out.println("");

        if(command == null || "get-balance".equals(command)) {
            getBalanceRemote();
        }

        if(command == null || "deploy".equals(command)) {
            deployRemote();
        }

        if(command == null || "get-receipt".equals(command)) {
            getReceiptRemote();
        }

        if(command == null || "call".equals(command)) {
            callRemote();
        }

        if(command == null || "contract-txn".equals(command)) {
            contractTxnRemote();
        }

        if(command == null || "transfer".equals(command)) {
            transferRemote();
        }

        if(command == null || "get-logs".equals(command)) {
            getLogsRemote();
        }

    }

    private void getLogsRemote() {
        System.out.println("\t- Get Logs");
        System.out.println("\t\t$> mvn aion4j:get-logs [-DfromBlock=<block num>] [-DtoBlock=<block num>] [-Daddress=<address1, ...>]" +
                " [-Dtopics=<topic1, ...>] [-Dblockhash=<block hash>] -P<remote_profile>");
    }

    private void transferRemote() {
        System.out.println("\t- Transfer");
        System.out.println("\t\t$> mvn [-Dpk=<pk>] -Dfrom=<from> -Dto=<to> -Dvalue=<value> [-Dgas=<gas>] [-DgasPrice=<gasPrice>] [-Dwait] -Premote");
    }

    private void contractTxnRemote() {
        System.out.println("\t- Send contract transaction");
        System.out.println("\t\t$> mvn aion4j:contract-txn -Dmethod=<method_name> [-Dargs=\"<arg1_type> <arg1_value> ... <argn_type> <argn_value>\"]" +
                " [-Dvalue=<value>] [-Daddress=<sender>] [-Dcontract=<contract_address>] [-Dgas=<gas>] [-DgasPrice=<gasPrice>] [-Dwait] -P<remote_profile>");
        System.out.println("\t\tExamples:");
        System.out.println("\t\t\t$> mvn aion4j:contract-txn -Dmethod=setData -Dargs=\"-T 'Hello World' -I 4\" -Dwait -Premote");
        System.out.println("\t\t\t$> mvn aion4j:contract-txn -Dmethod=someMethod -Dargs=\"-T hello -I 6 8 -I[] 5 8 -I[][] 5,6,7 11,22,43\" -Premote");
    }

    private void callRemote() {
        System.out.println("\t- Call contract method");
        System.out.println("\t\t$> mvn aion4j:call -Dmethod=<method_name> [-Dargs=\"<arg1_type> <arg1_value> ... <argn_type> <argn_value>\"]" +
                " [-Dvalue=<value>] [-Daddress=<sender>] [-Dcontract=<contract_address>] -P<remote_profile>");
        System.out.println("\t\t If contract address is not specified, the recently deployed contract's address is used.");
        System.out.println("\t\tExamples:");
        System.out.println("\t\t\t$> mvn aion4j:call -Dmethod=setData -Dargs=\"-T 'Hello World'\" -I 4 -Premote");
        System.out.println("\t\t\t$> mvn aion4j:call -Dmethod=someMethod -Dargs=\"-T hello -I 6 8 -I[] 5 8 -I[][] 5,6,7 11,22,43\" -Premote");
    }

    private void getReceiptRemote() {
        System.out.println("\t- Get transaction receipt by txHash");
        System.out.println("\t\t$> mvn aion4j:get-receipt -DtxHash=<tx_hash> [-Dtail] [-Dsilent] -P<remote_profile>");

        System.out.println("\t- Get transaction receipt for the last txHash");
        System.out.println("\t\t$> mvn aion4j:get-receipt [-Dtail] [-Dsilent] -P<remote_profile>");
    }

    private void deployRemote() {
        System.out.println("\t- Deploy contract jar");
        System.out.println("\t\t$> mvn aion4j:deploy [-Dargs=\"<arg1_type> <arg1_value> ... <argn_type> <argn_value>\"] [-Dvalue=<value>]" +
                " [-Dgas=<gas>] [-DgasPrice=<gasPrice>] [-Dwait] -P<remote_profile>");
        System.out.println("\t\tExamples:");
        System.out.println("\t\t\t$> mvn aion4j:deploy -Dargs=\"-T 'Hello World'-I 4\" -Dwait -Premote");
        System.out.println("\t\t\t$> mvn aion4j:deploy -Dargs=\"-T 'Hello World'-I 4\" -Dwait -Premote");
        System.out.println("\t\t\t$> mvn aion4j:deploy -Dargs=\"-T hello -I 6 8 -I[] 5 8 -I[][] 5,6,7 11,22,43\" -Premote");
    }

    private void getBalanceRemote() {
        System.out.println("\t- Get balance of the specified address");
        System.out.println("\t\t$> mvn aion4j:get-balance [-Daddress=<address>] -P<remote_profile>");

        System.out.println("\t- Get balance of the default address set in the environment.");
        System.out.println("\t\t$> mvn aion4j:get-balance -P<remote_profile>");
    }

}
