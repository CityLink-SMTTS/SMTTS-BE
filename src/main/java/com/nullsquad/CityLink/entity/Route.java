package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "route_number", unique = true, nullable = false, length = 20)
    private String routeNumber;
    
    @Column(name = "route_name", nullable = false, length = 200)
    private String routeName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "route_type")
    private VehicleType routeType;
    
    @Column(name = "start_point", length = 200)
    private String startPoint;
    
    @Column(name = "end_point", length = 200)
    private String endPoint;
    
    @Column(name = "start_latitude", precision = 10, scale = 8)
    private BigDecimal startLatitude;
    
    @Column(name = "start_longitude", precision = 11, scale = 8)
    private BigDecimal startLongitude;
    
    @Column(name = "end_latitude", precision = 10, scale = 8)
    private BigDecimal endLatitude;
    
    @Column(name = "end_longitude", precision = 11, scale = 8)
    private BigDecimal endLongitude;
    
    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @Column(name = "first_departure_time")
    private LocalTime firstDepartureTime;
    
    @Column(name = "last_departure_time")
    private LocalTime lastDepartureTime;
    
    @Column(name = "frequency_minutes")
    private Integer frequencyMinutes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    @ToString.Exclude
    private Zone zone;
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<RouteStop> stops;
    
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(name = "is_express")
    private Boolean isExpress;
    
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
        if (isExpress == null) isExpress = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
