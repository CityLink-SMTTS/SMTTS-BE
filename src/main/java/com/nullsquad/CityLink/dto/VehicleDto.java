package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.VehicleStatus;
import com.nullsquad.CityLink.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {
    
    private Long id;
    
    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;
    
    @NotBlank(message = "License plate is required")
    private String licensePlate;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    
    private VehicleStatus status;
    
    @Positive(message = "Capacity must be positive")
    private Integer capacity;
    
    private Integer currentOccupancy;
    
    private String manufacturer;
    private String model;
    private Integer manufactureYear;
    
    private Long currentRouteId;
    private String currentRouteName;
    
    private Long assignedZoneId;
    private String assignedZoneName;
    
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    
    private String fuelType;
    private Boolean isEcoFriendly;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
