package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fare_rules")
public class FareRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_zone_id")
    private Zone fromZone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_zone_id")
    private Zone toZone;
    
    @Column(name = "base_fare", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseFare;
    
    @Column(name = "per_km_rate", precision = 10, scale = 2)
    private BigDecimal perKmRate;
    
    @Column(name = "per_zone_rate", precision = 10, scale = 2)
    private BigDecimal perZoneRate;
    
    @Column(name = "minimum_fare", precision = 10, scale = 2)
    private BigDecimal minimumFare;
    
    @Column(name = "maximum_fare", precision = 10, scale = 2)
    private BigDecimal maximumFare;
    
    @Column(name = "peak_hour_multiplier", precision = 5, scale = 2)
    private BigDecimal peakHourMultiplier;
    
    @Column(name = "night_multiplier", precision = 5, scale = 2)
    private BigDecimal nightMultiplier;
    
    @Column(name = "weekend_multiplier", precision = 5, scale = 2)
    private BigDecimal weekendMultiplier;
    
    @Column(name = "student_discount_percent", precision = 5, scale = 2)
    private BigDecimal studentDiscountPercent;
    
    @Column(name = "senior_discount_percent", precision = 5, scale = 2)
    private BigDecimal seniorDiscountPercent;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public FareRule() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public Zone getFromZone() { return fromZone; }
    public void setFromZone(Zone fromZone) { this.fromZone = fromZone; }

    public Zone getToZone() { return toZone; }
    public void setToZone(Zone toZone) { this.toZone = toZone; }

    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }

    public BigDecimal getPerKmRate() { return perKmRate; }
    public void setPerKmRate(BigDecimal perKmRate) { this.perKmRate = perKmRate; }

    public BigDecimal getPerZoneRate() { return perZoneRate; }
    public void setPerZoneRate(BigDecimal perZoneRate) { this.perZoneRate = perZoneRate; }

    public BigDecimal getMinimumFare() { return minimumFare; }
    public void setMinimumFare(BigDecimal minimumFare) { this.minimumFare = minimumFare; }

    public BigDecimal getMaximumFare() { return maximumFare; }
    public void setMaximumFare(BigDecimal maximumFare) { this.maximumFare = maximumFare; }

    public BigDecimal getPeakHourMultiplier() { return peakHourMultiplier; }
    public void setPeakHourMultiplier(BigDecimal peakHourMultiplier) { this.peakHourMultiplier = peakHourMultiplier; }

    public BigDecimal getNightMultiplier() { return nightMultiplier; }
    public void setNightMultiplier(BigDecimal nightMultiplier) { this.nightMultiplier = nightMultiplier; }

    public BigDecimal getWeekendMultiplier() { return weekendMultiplier; }
    public void setWeekendMultiplier(BigDecimal weekendMultiplier) { this.weekendMultiplier = weekendMultiplier; }

    public BigDecimal getStudentDiscountPercent() { return studentDiscountPercent; }
    public void setStudentDiscountPercent(BigDecimal studentDiscountPercent) { this.studentDiscountPercent = studentDiscountPercent; }

    public BigDecimal getSeniorDiscountPercent() { return seniorDiscountPercent; }
    public void setSeniorDiscountPercent(BigDecimal seniorDiscountPercent) { this.seniorDiscountPercent = seniorDiscountPercent; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDateTime effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDateTime getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDateTime effectiveTo) { this.effectiveTo = effectiveTo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (peakHourMultiplier == null) peakHourMultiplier = BigDecimal.ONE;
        if (nightMultiplier == null) nightMultiplier = BigDecimal.ONE;
        if (weekendMultiplier == null) weekendMultiplier = BigDecimal.ONE;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
