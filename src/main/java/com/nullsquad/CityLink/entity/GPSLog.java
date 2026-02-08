package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gps_logs", indexes = {
    @Index(name = "idx_gps_vehicle_time", columnList = "vehicle_id, recorded_at"),
    @Index(name = "idx_gps_recorded_at", columnList = "recorded_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GPSLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @ToString.Exclude
    private Vehicle vehicle;
    
    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;
    
    @Column(name = "altitude_meters", precision = 10, scale = 2)
    private BigDecimal altitudeMeters;
    
    @Column(name = "speed_kmh", precision = 6, scale = 2)
    private BigDecimal speedKmh;
    
    @Column(name = "heading_degrees", precision = 5, scale = 2)
    private BigDecimal headingDegrees;
    
    @Column(name = "accuracy_meters", precision = 8, scale = 2)
    private BigDecimal accuracyMeters;
    
    @Column(name = "odometer_km", precision = 12, scale = 2)
    private BigDecimal odometerKm;
    
    @Column(name = "is_deviated")
    private Boolean isDeviated;
    
    @Column(name = "is_near_stop")
    private Boolean isNearStop;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearest_stop_id")
    @ToString.Exclude
    private RouteStop nearestStop;
    
    @Column(name = "distance_to_stop_meters", precision = 10, scale = 2)
    private BigDecimal distanceToStopMeters;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) receivedAt = LocalDateTime.now();
        if (isDeviated == null) isDeviated = false;
        if (isNearStop == null) isNearStop = false;
    }
}
