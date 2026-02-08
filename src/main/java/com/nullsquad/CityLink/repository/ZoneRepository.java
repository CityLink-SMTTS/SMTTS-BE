package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    
    Optional<Zone> findByZoneCode(String zoneCode);
    
    List<Zone> findByIsActiveTrue();
    
    List<Zone> findByZoneTier(Integer zoneTier);
    
    @Query("SELECT z FROM Zone z WHERE z.fareMultiplier <= :maxMultiplier")
    List<Zone> findByMaxFareMultiplier(@Param("maxMultiplier") BigDecimal maxMultiplier);
    
    @Query("SELECT z FROM Zone z ORDER BY z.zoneTier ASC")
    List<Zone> findAllOrderedByTier();
    
    boolean existsByZoneCode(String zoneCode);
}
