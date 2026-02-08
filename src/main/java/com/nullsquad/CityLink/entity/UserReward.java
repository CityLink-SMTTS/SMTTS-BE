package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    @Column(name = "total_gift_points")
    private Integer totalGiftPoints;
    
    @Column(name = "available_gift_points")
    private Integer availableGiftPoints;
    
    @Column(name = "redeemed_points")
    private Integer redeemedPoints;
    
    @Column(name = "total_green_score", precision = 10, scale = 2)
    private BigDecimal totalGreenScore;
    
    @Column(name = "current_tier", length = 20)
    private String currentTier;
    
    @Column(name = "total_trips")
    private Integer totalTrips;
    
    @Column(name = "eco_trips_count")
    private Integer ecoTripsCount;
    
    @Column(name = "carbon_saved_kg", precision = 10, scale = 2)
    private BigDecimal carbonSavedKg;
    
    @Column(name = "consecutive_days")
    private Integer consecutiveDays;
    
    @Column(name = "last_trip_date")
    private LocalDateTime lastTripDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (totalGiftPoints == null) totalGiftPoints = 0;
        if (availableGiftPoints == null) availableGiftPoints = 0;
        if (redeemedPoints == null) redeemedPoints = 0;
        if (totalGreenScore == null) totalGreenScore = BigDecimal.ZERO;
        if (currentTier == null) currentTier = "BRONZE";
        if (totalTrips == null) totalTrips = 0;
        if (ecoTripsCount == null) ecoTripsCount = 0;
        if (carbonSavedKg == null) carbonSavedKg = BigDecimal.ZERO;
        if (consecutiveDays == null) consecutiveDays = 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
