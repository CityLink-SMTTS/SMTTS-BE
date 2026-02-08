package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.FareRule;
import com.nullsquad.CityLink.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    
    List<FareRule> findByIsActiveTrue();
    
    List<FareRule> findByVehicleType(VehicleType vehicleType);
    
    List<FareRule> findByVehicleTypeAndIsActiveTrue(VehicleType vehicleType);
    
    @Query("SELECT fr FROM FareRule fr WHERE fr.fromZone.id = :fromZoneId AND fr.toZone.id = :toZoneId AND fr.isActive = true")
    List<FareRule> findByZones(@Param("fromZoneId") Long fromZoneId, @Param("toZoneId") Long toZoneId);
    
    @Query("SELECT fr FROM FareRule fr WHERE fr.vehicleType = :vehicleType AND " +
           "fr.fromZone.id = :fromZoneId AND fr.toZone.id = :toZoneId AND fr.isActive = true")
    Optional<FareRule> findByVehicleTypeAndZones(@Param("vehicleType") VehicleType vehicleType,
                                                  @Param("fromZoneId") Long fromZoneId,
                                                  @Param("toZoneId") Long toZoneId);
    
    @Query("SELECT fr FROM FareRule fr WHERE fr.isActive = true AND " +
           "(fr.effectiveFrom IS NULL OR fr.effectiveFrom <= :now) AND " +
           "(fr.effectiveTo IS NULL OR fr.effectiveTo >= :now)")
    List<FareRule> findCurrentlyEffectiveRules(@Param("now") LocalDateTime now);
    
    @Query("SELECT fr FROM FareRule fr WHERE fr.vehicleType = :vehicleType AND fr.isActive = true AND " +
           "(fr.fromZone IS NULL OR fr.fromZone.id = :fromZoneId) AND " +
           "(fr.toZone IS NULL OR fr.toZone.id = :toZoneId) " +
           "ORDER BY CASE WHEN fr.fromZone IS NOT NULL AND fr.toZone IS NOT NULL THEN 0 " +
           "WHEN fr.fromZone IS NOT NULL OR fr.toZone IS NOT NULL THEN 1 ELSE 2 END")
    List<FareRule> findApplicableRules(@Param("vehicleType") VehicleType vehicleType,
                                       @Param("fromZoneId") Long fromZoneId,
                                       @Param("toZoneId") Long toZoneId);
    
    Optional<FareRule> findByRuleName(String ruleName);
    
    boolean existsByRuleName(String ruleName);
}
