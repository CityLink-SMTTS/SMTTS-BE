package com.nullsquad.CityLink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneDTO {
    
    private Long id;
    
    @NotBlank(message = "Zone code is required")
    private String zoneCode;
    
    @NotBlank(message = "Zone name is required")
    private String zoneName;
    
    private String description;
    
    @NotNull(message = "Center latitude is required")
    private BigDecimal centerLatitude;
    
    @NotNull(message = "Center longitude is required")
    private BigDecimal centerLongitude;
    
    @Positive(message = "Radius must be positive")
    private BigDecimal radiusKm;
    
    @Positive(message = "Fare multiplier must be positive")
    private BigDecimal fareMultiplier;
    
    private Integer zoneTier;
    
    private Boolean isActive;
    
    private Integer routeCount;
    private Integer vehicleCount;
}
