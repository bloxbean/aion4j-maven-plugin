package org.aion4j.maven.avm.api;

public class DeployResponse {

    private boolean success;
    private String address;
    private long energyUsed;
    private String statusMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(long energyUsed) {
        this.energyUsed = energyUsed;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
