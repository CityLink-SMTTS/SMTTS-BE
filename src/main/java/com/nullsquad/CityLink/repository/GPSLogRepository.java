package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.GPSLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GPSLogRepository extends JpaRepository<GPSLog, Long> {
    
    @Query("SELECT g FROM GPSLog g WHERE g.vehicle.id = :vehicleId AND g.recordedAt >= :startTime ORDER BY g.recordedAt ASC")
    List<GPSLog> findLogsByVehicleAndTimeRange(@Param("vehicleId") Long vehicleId, 
                                                @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT g FROM GPSLog g WHERE g.vehicle.id = :vehicleId ORDER BY g.recordedAt DESC")
    List<GPSLog> findLatestLogsByVehicle(@Param("vehicleId") Long vehicleId, 
                                          org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT g FROM GPSLog g WHERE g.vehicle.id = :vehicleId ORDER BY g.recordedAt DESC LIMIT 1")
    Optional<GPSLog> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT g FROM GPSLog g WHERE g.vehicle.vehicleNumber = :vehicleNumber ORDER BY g.recordedAt DESC LIMIT 1")
    Optional<GPSLog> findLatestByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);
    
    @Query("SELECT g FROM GPSLog g WHERE g.vehicle.id = :vehicleId AND g.recordedAt BETWEEN :startTime AND :endTime ORDER BY g.recordedAt ASC")
    List<GPSLog> findByVehicleAndDateRange(@Param("vehicleId") Long vehicleId, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT g FROM GPSLog g WHERE g.isDeviated = true AND g.recordedAt >= :since")
    List<GPSLog> findDeviatedLogsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(g.speedKmh) FROM GPSLog g WHERE g.vehicle.id = :vehicleId AND g.recordedAt >= :since")
    Double findAverageSpeedByVehicle(@Param("vehicleId") Long vehicleId, 
                                     @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(g) FROM GPSLog g WHERE g.vehicle.id = :vehicleId AND g.recordedAt >= :since")
    Long countLogsByVehicleSince(@Param("vehicleId") Long vehicleId, 
                                 @Param("since") LocalDateTime since);
    
    void deleteByRecordedAtBefore(LocalDateTime before);
}
