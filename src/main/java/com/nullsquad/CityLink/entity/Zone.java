package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "zone_code", unique = true, nullable = false, length = 10)
    private String zoneCode;
    
    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "center_latitude", precision = 10, scale = 8)
    private BigDecimal centerLatitude;
    
    @Column(name = "center_longitude", precision = 11, scale = 8)
    private BigDecimal centerLongitude;
    
    @Column(name = "radius_km", precision = 10, scale = 2)
    private BigDecimal radiusKm;
    
    @Column(name = "fare_multiplier", precision = 5, scale = 2)
    private BigDecimal fareMultiplier;
    
    @Column(name = "zone_tier")
    private Integer zoneTier;
    
    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Route> routes;
    
    @OneToMany(mappedBy = "assignedZone", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Vehicle> vehicles;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (fareMultiplier == null) fareMultiplier = BigDecimal.ONE;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
