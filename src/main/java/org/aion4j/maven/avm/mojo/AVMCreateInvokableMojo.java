/*
 * MIT License
 *
 * Copyright (c) 2019 Aion4j Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.aion4j.maven.avm.mojo;

import org.aion4j.avm.helper.remote.RemoteAvmAdapter;
import org.aion4j.avm.helper.signing.SignedInvokableTransactionBuilder;
import org.aion4j.avm.helper.signing.SignedTransactionBuilder;
import org.aion4j.avm.helper.util.ConfigUtil;
import org.aion4j.avm.helper.util.HexUtil;
import org.aion4j.avm.helper.util.StringUtils;
import org.aion4j.maven.avm.adapter.LocalAvmAdapter;
import org.aion4j.maven.avm.impl.MavenLog;
import org.aion4j.maven.avm.ipc.IPCResultWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Mojo(name = "create-invokable", aggregator = true)
public class AVMCreateInvokableMojo extends AVMLocalRuntimeBaseMojo {
    private final static String HEX_PREFIX = "0x";

    @Override
    protected void executeRemote() throws MojoExecutionException {
        doExecute(null);
    }

    @Override
    protected void executeLocalAvm(ClassLoader avmClassloader, Object localAvmInstance) throws MojoExecutionException {
        LocalAvmAdapter localAvmAdapter = new LocalAvmAdapter(localAvmInstance);
        doExecute(localAvmAdapter);
    }

    @Override
    protected void preexecuteLocalAvm() throws MojoExecutionException {

    }

    public void doExecute(LocalAvmAdapter localAvmAdapter) throws MojoExecutionException {

        String pk = ConfigUtil.getProperty("pk");
        if(pk == null || pk.isEmpty()) {
            printHelp();
            throw new MojoExecutionException("Private key can not be null. Please provide private key through -Dpk=<private key> option or set web3rpc_url as an environment variable");
        }

        //Parse arguments
        String to = ConfigUtil.getProperty("to");
        String executor = ConfigUtil.getProperty("executor");
        String nonce = ConfigUtil.getProperty("nonce");
        String valueStr = ConfigUtil.getProperty("value");
        boolean fetchNonce = ConfigUtil.getAvmConfigurationBooleanProps("fetchNonce", false);

        String method = ConfigUtil.getProperty("method");
        String args = ConfigUtil.getProperty("args");
        String data = ConfigUtil.getProperty("data");

        if(!fetchNonce && (nonce == null || nonce.isEmpty())) {
            printHelp();
            throw new MojoExecutionException("Invalid nonce. Please provide nonce through -Dnonce=<sender_nonce>");
        }

        BigInteger value = null;
        if(valueStr != null && !valueStr.isEmpty())
            value = new BigInteger(valueStr.trim());

        BigInteger senderNonce = null;
        if(fetchNonce) {
            if(isLocal()) {
                senderNonce = getLocalNonce(localAvmAdapter, pk);
                getLog().info("Sender Nonce fetched from kernel: " + senderNonce);
            } else {
                senderNonce = getRemoteNonce(pk);
                getLog().info("Sender Nonce fetched from kernel: " + senderNonce);
            }
        } else {
            senderNonce = new BigInteger(nonce);
        }

        if(!StringUtils.isEmpty(method) && !StringUtils.isEmpty(data)) {
            printHelp();
            throw new MojoExecutionException("-Ddata and -Dmethod both can not be used together. Use only one option.");
        }

        if(StringUtils.isEmpty(args))
            args = null;

        String output = ConfigUtil.getProperty("output"); //only required for ipc call. For exp: IDE to maven process
        //Parsing ends
        if(!StringUtils.isEmpty(method)) {
            data = encodeMethodCallData(method, args);
        }

        SignedInvokableTransactionBuilder signedInvokableTransactionBuilder = new SignedInvokableTransactionBuilder();
        try {
            byte[] invokable = signedInvokableTransactionBuilder.privateKey(pk)
                    .destination(to)
                    .executor(executor)
                    .data(data)
                    .senderNonce(senderNonce)
                    .value(value)
                    .buildSignedInvokableTransaction();

            String hexValue = HexUtil.bytesToHexString(invokable);
            if(hexValue != null && !hexValue.startsWith(HEX_PREFIX))
                hexValue = HEX_PREFIX + hexValue;

            getLog().info("Invokable Transaction (Hex) : " + hexValue);

            try {
                if (!StringUtils.isEmpty(output)) { //If it's an ipc call, write the result json
                    IPCResultWriter.saveOutput(hexValue, output, getLog());
                }
            }catch (Exception e) {}
        } catch (Exception e) {
            throw new MojoExecutionException("Invokable transaction signing failed", e);
        }
    }

    private String encodeMethodCallData(String method, String args) throws MojoExecutionException {
        try {
            Class localAvmClazz = getLocalAVMClass();
            //Lets do method call encoding
            Method enocodeCallMethodWithArgsStr = localAvmClazz.getMethod("encodeMethodCallWithArgsString", String.class, String.class);
            String encodedMethodCall = (String) enocodeCallMethodWithArgsStr.invoke(null, method, args);

            return encodedMethodCall;
        } catch (Exception e) {
            throw new MojoExecutionException("Error encoding method call data", e);
        }
    }

    private BigInteger getLocalNonce(LocalAvmAdapter localAvmAdapter, String pk) throws MojoExecutionException {
        String senderAddress = null;
        try {
            senderAddress = SignedTransactionBuilder.getAionAddress(pk);
        } catch (InvalidKeySpecException e) {
            throw new MojoExecutionException("Unable to get aion address from private key.", e);
        }

        return localAvmAdapter.getNonce(senderAddress);
    }

    private BigInteger getRemoteNonce(String pk) throws MojoExecutionException {
        String web3RpcUrl = resolveWeb3rpcUrl();
        if(StringUtils.isEmpty(web3RpcUrl)) {
            throw new MojoExecutionException("web3rpc.url can not be null in remote mode or when -DfetchNonce option is selected");
        }

        String senderAddress = null;
        try {
            senderAddress = SignedTransactionBuilder.getAionAddress(pk);
        } catch (InvalidKeySpecException e) {
            throw new MojoExecutionException("Unable to get aion address from private key.", e);
        }

        RemoteAvmAdapter remoteAVMNode = new RemoteAvmAdapter(web3RpcUrl, MavenLog.getLog(getLog()));
        BigInteger nonce = remoteAVMNode.getNonce(senderAddress);

        getLog().debug("Sender Address: " + senderAddress);
        getLog().debug("Fetched nonce : " + nonce);
        return nonce != null? nonce: BigInteger.ZERO;
    }

    private void printHelp() {
        getLog().error("Usage:");
        getLog().error("mvn aion4j:create-invokable [-Dpk=<private_key>] [-Dto=<destination_address>]-Dexecutor=<executor_address> [-Dnonce=<sender_nonce>] [-DfetchNonce] [-Dvalue=<value>] [-Ddata=<data>] [-Dmethod=<method_name>] [-Dargs=<args>]");
    }
}
