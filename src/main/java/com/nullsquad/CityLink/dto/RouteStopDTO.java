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
public class RouteStopDTO {
    
    private Long id;
    
    private Long routeId;
    
    @NotBlank(message = "Stop name is required")
    private String stopName;
    
    @NotNull(message = "Stop order is required")
    @Positive(message = "Stop order must be positive")
    private Integer stopOrder;
    
    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;
    
    private BigDecimal distanceFromStartKm;
    
    private Integer estimatedArrivalMinutes;
    
    private Boolean isMajorStop;
    private Boolean hasShelter;
    private Boolean isAccessible;
    
    private Long zoneId;
    private String zoneName;
}
