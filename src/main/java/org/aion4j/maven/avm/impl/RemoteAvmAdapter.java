package org.aion4j.maven.avm.impl;

import org.aion.base.util.ByteUtil;
import org.aion4j.avm.helper.api.Log;
import org.aion4j.avm.helper.remote.RemoteAVMNode;
import org.aion4j.maven.avm.util.StringUtils;

import java.math.BigInteger;

public class RemoteAvmAdapter {

    private RemoteAVMNode remoteAvmNode;

    public RemoteAvmAdapter(String nodeUrl) {
        this.remoteAvmNode = new RemoteAVMNode(nodeUrl, new DummyLog());
    }

    public RemoteAvmAdapter(String nodeUrl, Log log) {
        this.remoteAvmNode = new RemoteAVMNode(nodeUrl, log);
    }

    public BigInteger getBalance(String address) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }

        String balanceInHex = remoteAvmNode.getBalance(address);

        if (!StringUtils.isEmpty(balanceInHex)) {
            if (balanceInHex.startsWith("0x"))
                balanceInHex = balanceInHex.substring(2);

            BigInteger balance = new BigInteger(balanceInHex, 16);
            return balance;

        } else {
            return null;
        }
    }

    public BigInteger getNonce(String address) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }

        String nonceInHex = remoteAvmNode.getTransactionCount(address);

        if(!StringUtils.isEmpty(nonceInHex)) {
            BigInteger nonce = ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(nonceInHex));
            return nonce;

        } else
            return null;
    }
}
