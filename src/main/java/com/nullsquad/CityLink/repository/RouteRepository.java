package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.Route;
import com.nullsquad.CityLink.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    Optional<Route> findByRouteNumber(String routeNumber);
    
    List<Route> findByRouteType(VehicleType routeType);
    
    List<Route> findByIsActiveTrue();
    
    List<Route> findByIsExpressTrue();
    
    List<Route> findByRouteTypeAndIsActiveTrue(VehicleType routeType);
    
    @Query("SELECT r FROM Route r WHERE r.zone.id = :zoneId")
    List<Route> findByZoneId(@Param("zoneId") Long zoneId);
    
    @Query("SELECT r FROM Route r WHERE r.startPoint LIKE %:point% OR r.endPoint LIKE %:point%")
    List<Route> findByStartOrEndPoint(@Param("point") String point);
    
    @Query("SELECT r FROM Route r WHERE r.baseFare <= :maxFare AND r.isActive = true")
    List<Route> findActiveRoutesWithinFare(@Param("maxFare") java.math.BigDecimal maxFare);
    
    @Query("SELECT r FROM Route r JOIN r.stops s WHERE s.stopName LIKE %:stopName%")
    List<Route> findByStopName(@Param("stopName") String stopName);
    
    @Query("SELECT DISTINCT r FROM Route r JOIN r.stops s WHERE s.zone.id = :zoneId")
    List<Route> findRoutesPassingThroughZone(@Param("zoneId") Long zoneId);
    
    boolean existsByRouteNumber(String routeNumber);
}
