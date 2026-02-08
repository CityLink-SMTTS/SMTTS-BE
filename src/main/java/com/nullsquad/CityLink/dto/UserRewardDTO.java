package com.nullsquad.CityLink.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRewardDTO {
    
    private Long id;
    private Long userId;
    private String userName;
    
    private Integer totalGiftPoints;
    private Integer availableGiftPoints;
    private Integer redeemedPoints;
    
    private BigDecimal totalGreenScore;
    private String currentTier;
    private String nextTier;
    private Double tierProgress;
    private Integer pointsToNextTier;
    
    private Integer totalTrips;
    private Integer ecoTripsCount;
    private BigDecimal carbonSavedKg;
    private Integer consecutiveDays;
    private LocalDateTime lastTripDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
