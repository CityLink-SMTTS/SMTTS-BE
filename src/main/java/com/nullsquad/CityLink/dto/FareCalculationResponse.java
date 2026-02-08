package com.nullsquad.CityLink.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareCalculationResponse {
    
    private Long fromStopId;
    private String fromStopName;
    private Long fromZoneId;
    private String fromZoneName;
    
    private Long toStopId;
    private String toStopName;
    private Long toZoneId;
    private String toZoneName;
    
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal zoneCharge;
    private BigDecimal timeMultiplier;
    private BigDecimal discountAmount;
    private String discountReason;
    private BigDecimal subtotal;
    private BigDecimal totalFare;
    
    private BigDecimal distanceKm;
    private Integer zonesCrossed;
    private Integer estimatedDurationMinutes;
    private String fareRuleApplied;
    private Integer estimatedGiftPoints;
    private Integer estimatedGreenScore;
    
    private Boolean isPeakHour;
    private Boolean isNightFare;
    private Boolean isWeekend;
}
