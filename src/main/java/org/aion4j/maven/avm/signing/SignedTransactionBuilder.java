package org.aion4j.maven.avm.signing;

import static net.i2p.crypto.eddsa.Utils.bytesToHex;
import static net.i2p.crypto.eddsa.Utils.hexToBytes;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.aion.rlp.RLP;

/**
 * A convenience class for building an Aion transaction and signing it locally (offline) using a
 * private key.
 * <p>
 * In general, if a specific method is invoked multiple times before building the transaction, then
 * the last invocation takes precedence.
 * <p>
 * The builder can be used to construct additional transactions after each build, and the previous
 * build settings will apply.
 * <p>
 * The builder provides a {@code reset} method that will clear the build back to its initial state.
 * <p>
 * The sender of the transaction will be the Aion account that corresponds to the provided private
 * key.
 */
public final class SignedTransactionBuilder {
    private static final byte AION_ADDRESS_PREFIX = (byte) 0xa0;

    // Required fields.
    private String privateKey = null;
    private BigInteger nonce = null;
    private long energyLimit = -1;

    // Fields we provide default values for.
    private BigInteger value = null;
    private String destination = null;
    private String data = null;
    private long energyPrice = -1;
    private byte type = 0x1;

    /**
     * The private key used to sign the transaction with.
     *
     * <b>This field must be set.</b>
     *
     * @param privateKey The private key.
     * @return this builder.
     */
    public SignedTransactionBuilder privateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    /**
     * The destination address of the transaction.
     *
     * <b>This field must be set.</b>
     *
     * @param destination The destination.
     * @return this builder.
     */
    public SignedTransactionBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * The amount of value to transfer from the sender to the destination.
     *
     * @param value The amount of value to transfer.
     * @return this builder.
     */
    public SignedTransactionBuilder value(BigInteger value) {
        this.value = value;
        return this;
    }

    /**
     * The nonce of the sender.
     *
     * <b>This field must be set.</b>
     *
     * @param nonce The sender nonce.
     * @return this builder.
     */
    public SignedTransactionBuilder senderNonce(BigInteger nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * The transaction data.
     *
     * @param data The data.
     * @return this builder.
     */
    public SignedTransactionBuilder data(String data) {
        this.data = data;
        return this;
    }

    /**
     * The energy limit of the transaction.
     *
     * <b>This field must be set.</b>
     *
     * @param limit The energy limit.
     * @return this builder.
     */
    public SignedTransactionBuilder energyLimit(long limit) {
        this.energyLimit = limit;
        return this;
    }

    /**
     * The energy price of the transaction.
     *
     * @param price The energy price.
     * @return this builder.
     */
    public SignedTransactionBuilder energyPrice(long price) {
        this.energyPrice = price;
        return this;
    }

    /**
     * Sets the transaction type to be the type used by the AVM.
     *
     * @return this builder.
     */
    public SignedTransactionBuilder useAvmTransactionType() {
        this.type = 0xf;
        return this;
    }

    /**
     * Constructs a transaction whose fields correspond to the fields as they have been set by the
     * provided builder methods, and signs this transaction with the provided private key.
     * <p>
     * The following fields must be set prior to calling this method:
     * - private key
     * - nonce
     * - energy limit
     * <p>
     * The following fields, if not set, will have the following default values:
     * - value: {@link BigInteger#ZERO}
     * - destination: empty array
     * - data: empty array
     * - energy price: {@code 10_000_000_000L} (aka. 10 AMP)
     * - type: {@code 0x1}
     *
     * @return the bytes of the signed transaction.
     */
    public byte[] buildSignedTransaction() throws InvalidKeySpecException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        if (this.privateKey == null) {
            throw new IllegalStateException("No private key specified.");
        }
        if (this.nonce == null) {
            throw new IllegalStateException("No nonce specified.");
        }
        if (this.energyLimit == -1) {
            throw new IllegalStateException("No energy limit specified.");
        }

        EdDSAPrivateKey privateKey = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(this.privateKey)));

        byte[] publicKey = privateKey.getAbyte();
        byte[] addrBytes = blake2b(publicKey);
        addrBytes[0] = AION_ADDRESS_PREFIX;

        byte[] to = (this.destination == null) ? new byte[0] : hexToBytes(this.destination);
        byte[] value = (this.value == null) ? BigInteger.ZERO.toByteArray() : this.value.toByteArray();

        byte[] nonce = this.nonce.toByteArray();
        byte[] timestamp = BigInteger.valueOf(System.currentTimeMillis() * 1000).toByteArray();

        byte[] encodedNonce = RLP.encodeElement(nonce);
        byte[] encodedTo = RLP.encodeElement(to);
        byte[] encodedValue = RLP.encodeElement(value);
        byte[] encodedData = RLP.encodeElement((this.data == null) ? new byte[0] : hexToBytes(this.data));
        byte[] encodedTimestamp = RLP.encodeElement(timestamp);
        byte[] encodedEnergy = RLP.encodeLong(this.energyLimit);
        byte[] encodedEnergyPrice = RLP.encodeLong((this.energyPrice == -1) ? 10_000_000_000L : this.energyPrice);
        byte[] encodedType = RLP.encodeByte(this.type);

        byte[] fullEncoding = RLP.encodeList(encodedNonce, encodedTo, encodedValue, encodedData, encodedTimestamp, encodedEnergy, encodedEnergyPrice, encodedType);

        byte[] rawHash = blake2b(fullEncoding);
        byte[] signatureOnly = sign(privateKey, rawHash);
        byte[] preEncodeSignature = new byte[publicKey.length + signatureOnly.length];
        System.arraycopy(publicKey, 0, preEncodeSignature, 0, publicKey.length);
        System.arraycopy(signatureOnly, 0, preEncodeSignature, publicKey.length, signatureOnly.length);
        byte[] signature = RLP.encodeElement(preEncodeSignature);

        return RLP.encodeList(encodedNonce, encodedTo, encodedValue, encodedData, encodedTimestamp, encodedEnergy, encodedEnergyPrice, encodedType, signature);
    }

    /**
     * Resets the builder so that it is in its initial state.
     * <p>
     * The state of the builder after a call to this method is the same as the state of a newly
     * constructed builder.
     */
    public void reset() {
        this.privateKey = null;
        this.nonce = null;
        this.energyLimit = -1;
        this.value = null;
        this.destination = null;
        this.data = null;
        this.energyPrice = -1;
        this.type = 0x1;
    }

    private static byte[] addSkPrefix(byte[] skString) {
        byte[] skEncoded = hexToBytes("302e020100300506032b657004220420");
        byte[] encoded = Arrays.copyOf(skEncoded, skEncoded.length + skString.length);
        System.arraycopy(skString, 0, encoded, skEncoded.length, skString.length);
        return encoded;
    }

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
        Blake2b.Digest digest = Blake2b.Digest.newInstance(32);
        digest.update(msg);
        return digest.digest();
    }

    private static byte[] sign(EdDSAPrivateKey privateKey, byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        EdDSAEngine edDSAEngine = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        edDSAEngine.initSign(privateKey);
        return edDSAEngine.signOneShot(data);
    }

    private static byte hexToByte(String s) {
        return hexToBytes(s)[0];
    }

    public static String signWithPvtKey(String pvtKey, String to, BigInteger value, String callData, BigInteger nonce, long energy, long energyPrice) throws Exception {

        if(pvtKey != null && pvtKey.startsWith("0x"))
            pvtKey = pvtKey.substring(2);


        if(to != null && to.startsWith("0x"))
            to = to.substring(2);

        if(callData != null && callData.startsWith("0x"))
            callData = callData.substring(2);


        byte[] pb = hexToBytes(pvtKey);

        byte[] pvtKeyB = null;
        //take 32 byte only.
        if (pb.length > 32)
            pvtKeyB = Arrays.copyOfRange(pb, 0, 32);
        else
            pvtKeyB = pb;

        byte[] toB = (to == null) ? new byte[0] : hexToBytes(to);
        byte[] data = (callData == null) ? new byte[0] : hexToBytes(callData);

        if(nonce == null)
            nonce = BigInteger.ZERO;

        SignedTransactionBuilder builder = new SignedTransactionBuilder();
        byte[] signedTx = builder.privateKey(pvtKey)
                .destination(to)
                .value(value)
                .data(callData)
                .senderNonce(nonce)
                .useAvmTransactionType()
                .energyLimit(energy)
                .energyPrice(energyPrice)
                .buildSignedTransaction();

        return bytesToHex(signedTx);

    }

    public static String getAionAddress(String pvtKey) throws InvalidKeySpecException {
        byte[] pb = hexToBytes(pvtKey);

        byte[] pvtKeyB = null;
        //take 32 byte only.
        if (pb.length > 32)
            pvtKeyB = Arrays.copyOfRange(pb, 0, 32);
        else
            pvtKeyB = pb;

        EdDSAPrivateKey privateKey = new EdDSAPrivateKey(
                new PKCS8EncodedKeySpec(addSkPrefix(pvtKey)));

        byte[] publicKey = privateKey.getAbyte();

        byte[] addrBytes = blake2b(publicKey);
        addrBytes [0] = hexToByte("a0");

        return bytesToHex(addrBytes);
    }


    public static void main(String[] args) throws Throwable {
        //String pvtKey = "777eab95a7f3db95375181c15b239ea27e9b5643862817fe03e1575ad7c20a11873396f0a7f60d03dec72c6550396fd0aeaa52bbdebab1b64ff1ef6548f1c65d";
        String pvtKey = "e9c9209f750f8be788834c2fb54a310ca77da2d264d27a4e6e46519ef5f2f878";

        String to = "a0c0cc973a306d31320fe72cad62afaa799d076bbd492ca4d5d5d941adfa12a9";
        String type = "";
        BigInteger value = BigInteger.valueOf(45L);
        String callData = " ";

        String tx = SignedTransactionBuilder.signWithPvtKey(pvtKey, to, new BigInteger("45"), "0x", null,1000, 100000);
        System.out.println(tx);

    }
}
