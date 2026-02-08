package com.nullsquad.CityLink.dto;

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
public class FareRuleDTO {
    
    private Long id;
    
    @NotBlank(message = "Rule name is required")
    private String ruleName;
    
    private String description;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    
    private Long fromZoneId;
    private String fromZoneName;
    
    private Long toZoneId;
    private String toZoneName;
    
    @NotNull(message = "Base fare is required")
    @Positive(message = "Base fare must be positive")
    private BigDecimal baseFare;
    
    private BigDecimal perKmRate;
    private BigDecimal perZoneRate;
    private BigDecimal minimumFare;
    private BigDecimal maximumFare;
    
    private BigDecimal peakHourMultiplier;
    private BigDecimal nightMultiplier;
    private BigDecimal weekendMultiplier;
    
    private BigDecimal studentDiscountPercent;
    private BigDecimal seniorDiscountPercent;
    
    private Boolean isActive;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}
