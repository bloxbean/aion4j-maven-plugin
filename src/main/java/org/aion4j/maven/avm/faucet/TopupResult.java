package org.aion4j.maven.avm.faucet;

public class TopupResult {
    private String txHash;
    private String error;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "Result{" +
                "txHash='" + txHash + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}