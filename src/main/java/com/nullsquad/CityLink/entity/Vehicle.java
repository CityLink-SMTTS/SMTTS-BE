package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "vehicle_number", unique = true, nullable = false, length = 20)
    private String vehicleNumber;
    
    @Column(name = "license_plate", unique = true, nullable = false, length = 20)
    private String licensePlate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VehicleStatus status;
    
    @Column(name = "capacity")
    private Integer capacity;
    
    @Column(name = "current_occupancy")
    private Integer currentOccupancy;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;
    
    @Column(name = "is_eco_friendly")
    private Boolean isEcoFriendly;
    
    @Column(name = "current_latitude", precision = 10, scale = 8)
    private BigDecimal currentLatitude;
    
    @Column(name = "current_longitude", precision = 11, scale = 8)
    private BigDecimal currentLongitude;
    
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_route_id")
    @ToString.Exclude
    private Route currentRoute;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_zone_id")
    @ToString.Exclude
    private Zone assignedZone;
    
    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;
    
    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = VehicleStatus.ACTIVE;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
