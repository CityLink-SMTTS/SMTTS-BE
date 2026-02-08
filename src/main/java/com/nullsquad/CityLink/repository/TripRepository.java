package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    @Query("SELECT t FROM Trip t WHERE t.vehicle.id = :vehicleId AND t.endedAt IS NULL")
    Optional<Trip> findActiveTripByVehicleId(Long vehicleId);
}
