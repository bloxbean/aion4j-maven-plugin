package org.aion4j.maven.avm.api;

public class CallResponse {

    private boolean success;
    private Object data;
    private long energyUsed;
    private long energyRemaining;
    private String statusMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(long energyUsed) {
        this.energyUsed = energyUsed;
    }

    public long getEnergyRemaining() {
        return energyRemaining;
    }

    public void setEnergyRemaining(long energyRemaining) {
        this.energyRemaining = energyRemaining;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
