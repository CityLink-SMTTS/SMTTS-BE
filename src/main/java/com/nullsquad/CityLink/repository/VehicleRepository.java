package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.Vehicle;
import com.nullsquad.CityLink.entity.VehicleStatus;
import com.nullsquad.CityLink.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
    
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    List<Vehicle> findByVehicleType(VehicleType vehicleType);
    
    List<Vehicle> findByStatus(VehicleStatus status);
    
    List<Vehicle> findByVehicleTypeAndStatus(VehicleType vehicleType, VehicleStatus status);
    
    @Query("SELECT v FROM Vehicle v WHERE v.assignedZone.id = :zoneId")
    List<Vehicle> findByZoneId(@Param("zoneId") Long zoneId);
    
    @Query("SELECT v FROM Vehicle v WHERE v.currentRoute.id = :routeId")
    List<Vehicle> findByCurrentRouteId(@Param("routeId") Long routeId);
    
    @Query("SELECT v FROM Vehicle v WHERE v.isEcoFriendly = true AND v.status = 'ACTIVE'")
    List<Vehicle> findActiveEcoFriendlyVehicles();
    
    @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenanceDate <= CURRENT_TIMESTAMP AND v.status != 'MAINTENANCE'")
    List<Vehicle> findVehiclesNeedingMaintenance();
    
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    Long countByStatus(@Param("status") VehicleStatus status);
    
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'ACTIVE' AND v.currentLatitude IS NOT NULL")
    List<Vehicle> findActiveVehiclesWithLocation();
    
    boolean existsByVehicleNumber(String vehicleNumber);
    
    boolean existsByLicensePlate(String licensePlate);
}
