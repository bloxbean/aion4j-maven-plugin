package org.aion4j.maven.avm.signing;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.aion.base.util.ByteUtil;
import org.aion.rlp.RLP;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class TxTool {

    private static byte[] addSkPrefix(String skString) {

        if(skString != null && skString.startsWith("0x"))
            skString = skString.substring(2);

        System.out.println("pvt key: " + skString);
        String skEncoded = "302e020100300506032b657004220420" + skString;
        //System.out.println("length: " + hexToBytes(skEncoded).length );
        byte[] bytes = hexToBytes(skEncoded);

        //take 48 byte only.
        if(bytes.length > 48)
            return Arrays.copyOfRange(bytes, 0, 48);
        else
            return bytes;
    }

    private static byte[] blake2b(byte[] msg) {
        Blake2b digest = Blake2b.Digest.newInstance(32);
        digest.update(msg);
        return digest.digest();
    }

    private static byte[] hexToBytes(String s) {
        byte[] biBytes = new BigInteger("10" + s.replaceAll("\\s", ""), 16).toByteArray();
        return Arrays.copyOfRange(biBytes, 1, biBytes.length);
    }

    private static byte hexToByte(String s) {
        return hexToBytes(s)[0];
    }

    private static String bytesToHex(byte[] bytes) {
        BigInteger bigInteger = new BigInteger(1, bytes);
        return bigInteger.toString(16);
    }

    private static byte[] sign(EdDSAPrivateKey privateKey, byte[] data) throws Exception {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAEngine edDSAEngine = new EdDSAEngine(
            MessageDigest.getInstance(spec.getHashAlgorithm()));

        edDSAEngine.initSign(privateKey);

        return edDSAEngine.signOneShot(data);
    }

    public static String signWithPvtKey(String pvtKey, String to, BigInteger value, String callData, String type, BigInteger nonce, long energy, long energyPrice) throws Exception {
            // PRIVATE KEY
            EdDSAPrivateKey privateKey = new EdDSAPrivateKey(
                new PKCS8EncodedKeySpec(addSkPrefix(pvtKey)));

            byte[] publicKey = privateKey.getAbyte();

            byte[] addrBytes = blake2b(publicKey);
            addrBytes [0] = hexToByte("a0");

        System.out.println("Address: " + bytesToHex(addrBytes));

            byte[] toB = null;

            if(to == null || to.isEmpty())
                toB = new byte[0];

            else
                // -TO
                toB = hexToBytes(to);

            // -VALUE

            byte[] valueB = ByteUtil.bigIntegerToBytes(value);

            // -DATA
            byte[] data = hexToBytes(callData);

            // -TYPE
            byte typeB = hexToByte(type);

            // Things we calculate:

            // -NONCE
           // AionAddress acc = new AionAddress(addrBytes);
           // BigInteger nonceBI = BigInteger.ZERO; // api.getChain().getNonce(acc).getObject();
            byte[] nonceB = nonce.toByteArray();

            // -TIMESTAMP
            // (nanos)
            byte[] timestamp = BigInteger.valueOf(System.currentTimeMillis() * 1000).toByteArray();

            // Things we hard-code:
            // -ENERGY
            //long energy = 350_000L;
            // -ENERGY PRICE
            //long energyPrice = 10_000_000_000L;

            // Notes for serialization.
            // 1) NONCE (byte[])
            // 2) TO (byte[])
            // 3) VALUE (byte[])
            // 4) DATA (byte[])
            // 5) TIMESTAMP (byte[])
            // 6) NRG (long)
            // 7) NRG PRICE (long)
            // 8) TYPE (byte)
            // (optional 9) SIGNATURE (byte[]) (HashUtil.h256(encodeList(1-8))
            // FINAL: encode either (1-8) or (1-9) as list

            byte[] nonce_1 = RLP.encodeElement(nonceB);
//            System.out.println(" - NONCE: " + bytesToHex(nonce_1));
            byte[] to_2 = RLP.encodeElement(toB);
//            System.out.println(" - TO: " + bytesToHex(to_2));
            byte[] value_3 = RLP.encodeElement(valueB);
//            System.out.println(" - VALUE: " + bytesToHex(value_3));
            byte[] data_4 = RLP.encodeElement(data);
//            System.out.println(" - DATA: " + bytesToHex(data_4));
            byte[] timestamp_5 = RLP.encodeElement(timestamp);
//            System.out.println(" - TIMESTAMP: " + bytesToHex(timestamp_5));
            byte[] energy_6 = RLP.encodeLong(energy);
//            System.out.println(" - NRG: " + bytesToHex(energy_6));
            byte[] energyPrice_7 = RLP.encodeLong(energyPrice);
//            System.out.println(" - NRGP: " + bytesToHex(energyPrice_7));
            byte[] type_8 = RLP.encodeByte(typeB);
//            System.out.println(" - TYPE: " + bytesToHex(type_8));

            byte[] encodedData = RLP
                .encodeList(nonce_1, to_2, value_3, data_4, timestamp_5, energy_6, energyPrice_7,
                    type_8);

            byte[] rawHash = blake2b(encodedData);

            byte[] signatureOnly = sign(privateKey, rawHash);
            byte[] preEncodeSignature = new byte[publicKey.length + signatureOnly.length];
            System.arraycopy(publicKey, 0, preEncodeSignature, 0, publicKey.length);
            System.arraycopy(signatureOnly, 0, preEncodeSignature, publicKey.length,
                signatureOnly.length);
            byte[] signature_9 = RLP.encodeElement(preEncodeSignature);
            byte[] encodedWithPayload = RLP
                .encodeList(nonce_1, to_2, value_3, data_4, timestamp_5, energy_6, energyPrice_7,
                    type_8, signature_9);

            return bytesToHex(encodedWithPayload);
    }

    public static void main(String[] args) throws Throwable {
        String pvtKey = "777eab95a7f3db95375181c15b239ea27e9b5643862817fe03e1575ad7c20a11873396f0a7f60d03dec72c6550396fd0aeaa52bbdebab1b64ff1ef6548f1c65d";
        pvtKey = "0x5e444d8bf64f9f6d9022cd245341e69d8b51af793367df3894f260b958a0d72aa060fe4538c5ea49dab8ef4ac9fc24933b9b9b4ed37eb20e070e49ce0b05b6f8";
        //String pvtKey = "c7f59254f050cbb0eb8909b9b99426f9998f8925b1178ebf7aab860918d5253eb425b880bd3573e5cf5cf67ebbc802dab555b15edf4a19c4a87f9c925e803fb7";
        //String pvtKey = "e9c9209f750f8be788834c2fb54a310ca77da2d264d27a4e6e46519ef5f2f878";
        //String pvtKey = "777eab95a7f3db95375181c15b239ea27e9b5643862817fe03e1575ad7c20a11";
        //String pvtKey = "538d0a994bbe8ba995cb7597c6fa19309a99e4ba0a1ea50bbae429e99db15356";
        String to = "a0c0cc973a306d31320fe72cad62afaa799d076bbd492ca4d5d5d941adfa12a9";
        String type = "";
        BigInteger value = BigInteger.valueOf(45L);
        String tx = TxTool.signWithPvtKey(pvtKey, to, value, "", "f", BigInteger.ZERO, 100, 1000);

        //CryptoUtil.signEdDSA("test".getBytes(), hexToBytes(pvtKey));

//        System.out.println("Raw tx: " + tx);

       // System.out.println(tx);
    }
}