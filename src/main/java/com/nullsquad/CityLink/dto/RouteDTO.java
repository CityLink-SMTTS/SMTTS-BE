package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDTO {
    
    private Long id;
    
    @NotBlank(message = "Route number is required")
    private String routeNumber;
    
    @NotBlank(message = "Route name is required")
    private String routeName;
    
    private String description;
    
    @NotNull(message = "Route type is required")
    private VehicleType routeType;
    
    @NotBlank(message = "Start point is required")
    private String startPoint;
    
    @NotBlank(message = "End point is required")
    private String endPoint;
    
    private BigDecimal startLatitude;
    private BigDecimal startLongitude;
    private BigDecimal endLatitude;
    private BigDecimal endLongitude;
    
    @Positive(message = "Distance must be positive")
    private BigDecimal totalDistanceKm;
    
    @Positive(message = "Duration must be positive")
    private Integer estimatedDurationMinutes;
    
    private LocalTime firstDepartureTime;
    private LocalTime lastDepartureTime;
    
    @Positive(message = "Frequency must be positive")
    private Integer frequencyMinutes;
    
    private Long zoneId;
    private String zoneName;
    
    private List<RouteStopDTO> stops;
    
    @Positive(message = "Base fare must be positive")
    private BigDecimal baseFare;
    
    private Boolean isActive;
    private Boolean isExpress;
}
