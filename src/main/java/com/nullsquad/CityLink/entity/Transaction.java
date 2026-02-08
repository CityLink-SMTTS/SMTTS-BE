package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_ref", unique = true, nullable = false, length = 50)
    private String transactionRef;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @ToString.Exclude
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @ToString.Exclude
    private Route route;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stop_id")
    @ToString.Exclude
    private RouteStop fromStop;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stop_id")
    @ToString.Exclude
    private RouteStop toStop;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_zone_id")
    @ToString.Exclude
    private Zone fromZone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_zone_id")
    @ToString.Exclude
    private Zone toZone;
    
    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;
    
    @Column(name = "zones_crossed")
    private Integer zonesCrossed;
    
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(name = "distance_fare", precision = 10, scale = 2)
    private BigDecimal distanceFare;
    
    @Column(name = "zone_fare", precision = 10, scale = 2)
    private BigDecimal zoneFare;
    
    @Column(name = "surge_multiplier", precision = 5, scale = 2)
    private BigDecimal surgeMultiplier;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "discount_reason", length = 200)
    private String discountReason;
    
    @Column(name = "total_fare", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalFare;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;
    
    @Column(name = "gift_points_earned")
    private Integer giftPointsEarned;
    
    @Column(name = "green_score_earned")
    private Integer greenScoreEarned;
    
    @Column(name = "is_offline_transaction")
    private Boolean isOfflineTransaction;
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionDate == null) transactionDate = LocalDateTime.now();
        if (status == null) status = TransactionStatus.PENDING;
        if (isOfflineTransaction == null) isOfflineTransaction = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
