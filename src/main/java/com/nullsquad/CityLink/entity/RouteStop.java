package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @ToString.Exclude
    private Route route;
    
    @Column(name = "stop_name", nullable = false, length = 200)
    private String stopName;
    
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;
    
    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;
    
    @Column(name = "distance_from_start_km", precision = 10, scale = 2)
    private BigDecimal distanceFromStartKm;
    
    @Column(name = "estimated_arrival_minutes")
    private Integer estimatedArrivalMinutes;
    
    @Column(name = "is_major_stop")
    private Boolean isMajorStop;
    
    @Column(name = "has_shelter")
    private Boolean hasShelter;
    
    @Column(name = "is_accessible")
    private Boolean isAccessible;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    @ToString.Exclude
    private Zone zone;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isMajorStop == null) isMajorStop = false;
        if (hasShelter == null) hasShelter = false;
        if (isAccessible == null) isAccessible = true;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
