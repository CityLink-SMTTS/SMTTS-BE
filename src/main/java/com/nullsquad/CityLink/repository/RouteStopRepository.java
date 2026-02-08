package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    
    List<RouteStop> findByRouteIdOrderByStopOrderAsc(Long routeId);
    
    @Query("SELECT rs FROM RouteStop rs WHERE rs.zone.id = :zoneId")
    List<RouteStop> findByZoneId(@Param("zoneId") Long zoneId);
    
    @Query("SELECT rs FROM RouteStop rs WHERE rs.isMajorStop = true")
    List<RouteStop> findMajorStops();
    
    @Query("SELECT rs FROM RouteStop rs WHERE rs.route.id = :routeId AND rs.isMajorStop = true ORDER BY rs.stopOrder")
    List<RouteStop> findMajorStopsByRouteId(@Param("routeId") Long routeId);
    
    @Query("SELECT rs FROM RouteStop rs WHERE rs.isAccessible = true")
    List<RouteStop> findAccessibleStops();
    
    @Query(value = "SELECT rs.* FROM route_stops rs " +
           "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(rs.latitude)) * " +
           "cos(radians(rs.longitude) - radians(:lon)) + sin(radians(:lat)) * " +
           "sin(radians(rs.latitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(rs.latitude)) * " +
           "cos(radians(rs.longitude) - radians(:lon)) + sin(radians(:lat)) * " +
           "sin(radians(rs.latitude)))) ASC", nativeQuery = true)
    List<RouteStop> findStopsNearLocation(@Param("lat") BigDecimal lat, 
                                          @Param("lon") BigDecimal lon, 
                                          @Param("radiusKm") double radiusKm);
    
    @Query("SELECT rs FROM RouteStop rs WHERE rs.stopName LIKE %:name%")
    List<RouteStop> findByStopNameContaining(@Param("name") String name);
}
