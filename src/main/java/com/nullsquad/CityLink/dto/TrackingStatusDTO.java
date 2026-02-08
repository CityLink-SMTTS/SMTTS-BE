package com.nullsquad.CityLink.dto;

public class TrackingStatusDTO {
    private String vehicleNumber;
    private boolean isNearStop;
    private boolean isDeviated;
    private double currentSpeedKmh;
    private String statusMessage;

    public TrackingStatusDTO() {}

    public TrackingStatusDTO(String vehicleNumber, boolean isNearStop, boolean isDeviated, double currentSpeedKmh, String statusMessage) {
        this.vehicleNumber = vehicleNumber;
        this.isNearStop = isNearStop;
        this.isDeviated = isDeviated;
        this.currentSpeedKmh = currentSpeedKmh;
        this.statusMessage = statusMessage;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public boolean isNearStop() { return isNearStop; }
    public void setNearStop(boolean nearStop) { isNearStop = nearStop; }
    public boolean isDeviated() { return isDeviated; }
    public void setDeviated(boolean deviated) { isDeviated = deviated; }
    public double getCurrentSpeedKmh() { return currentSpeedKmh; }
    public void setCurrentSpeedKmh(double currentSpeedKmh) { this.currentSpeedKmh = currentSpeedKmh; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
