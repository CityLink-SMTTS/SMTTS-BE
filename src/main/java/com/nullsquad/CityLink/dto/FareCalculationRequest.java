package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.PaymentMethod;
import com.nullsquad.CityLink.entity.VehicleType;
import jakarta.validation.constraints.NotNull;

public class FareCalculationRequest {
    
    @NotNull(message = "From stop ID is required")
    private Long fromStopId;
    
    @NotNull(message = "To stop ID is required")
    private Long toStopId;
    
    private VehicleType vehicleType;
    private String passengerType;
    private PaymentMethod paymentMethod;
    
    public FareCalculationRequest() {}

    // Getters and Setters
    public Long getFromStopId() { return fromStopId; }
    public void setFromStopId(Long fromStopId) { this.fromStopId = fromStopId; }

    public Long getToStopId() { return toStopId; }
    public void setToStopId(Long toStopId) { this.toStopId = toStopId; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getPassengerType() { return passengerType; }
    public void setPassengerType(String passengerType) { this.passengerType = passengerType; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
