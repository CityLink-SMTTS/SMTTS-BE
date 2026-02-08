package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.PaymentMethod;
import com.nullsquad.CityLink.entity.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    
    private Long id;
    private String transactionRef;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Long vehicleId;
    private String vehicleNumber;
    
    private Long routeId;
    private String routeName;
    
    private Long fromStopId;
    private String fromStopName;
    
    private Long toStopId;
    private String toStopName;
    
    private Long fromZoneId;
    private String fromZoneName;
    
    private Long toZoneId;
    private String toZoneName;
    
    private BigDecimal distanceKm;
    private Integer zonesCrossed;
    
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal zoneFare;
    private BigDecimal surgeMultiplier;
    private BigDecimal discountAmount;
    private String discountReason;
    private BigDecimal totalFare;
    
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    
    private Integer giftPointsEarned;
    private Integer greenScoreEarned;
    
    private Boolean isOfflineTransaction;
    private LocalDateTime syncedAt;
    private String deviceId;
    
    private LocalDateTime transactionDate;
    private LocalDateTime completedAt;
}
