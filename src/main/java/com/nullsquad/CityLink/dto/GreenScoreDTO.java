package com.nullsquad.CityLink.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenScoreDTO {
    
    private Long userId;
    private BigDecimal totalGreenScore;
    private String greenTier;
    private Integer rank;
    
    private Integer totalTrips;
    private Integer ecoTripsCount;
    private Double ecoTripPercentage;
    
    private BigDecimal carbonSavedKg;
    private BigDecimal treesEquivalent;
    
    private Integer consecutiveDays;
    
    private Boolean hasFirstEcoTrip;
    private Boolean has10EcoTrips;
    private Boolean has50EcoTrips;
    private Boolean has100EcoTrips;
    private Boolean hasWeekStreak;
    private Boolean hasMonthStreak;
    
    private Double percentileRank;
    private String impactMessage;
    
    private List<String> achievements;
    private List<String> tips;
}
