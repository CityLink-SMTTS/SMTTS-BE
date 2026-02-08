package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.GpsPingDTO;
import com.nullsquad.CityLink.dto.TrackingStatusDTO;
import com.nullsquad.CityLink.entity.GPSLog;
import com.nullsquad.CityLink.entity.RouteStop;
import com.nullsquad.CityLink.entity.Vehicle;
import com.nullsquad.CityLink.entity.VehicleStatus;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.GPSLogRepository;
import com.nullsquad.CityLink.repository.RouteStopRepository;
import com.nullsquad.CityLink.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final VehicleRepository vehicleRepository;
    private final GPSLogRepository gpsLogRepository;
    private final RouteStopRepository routeStopRepository;

    // Constants for tracking logic
    private static final double NEAR_STOP_THRESHOLD_KM = 0.1; // 100 meters
    private static final double ROUTE_DEVIATION_THRESHOLD_KM = 0.5; // 500 meters
    private static final int SPEED_CALCULATION_MINUTES = 5;

    @Transactional
    public TrackingStatusDTO processGpsPing(GpsPingDTO pingDTO) {
        // Find vehicle by vehicle number
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(pingDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + pingDTO.getVehicleId()));

        // Get previous GPS log for speed calculation
        Optional<GPSLog> previousLog = gpsLogRepository.findLatestByVehicleId(vehicle.getId());

        // Calculate speed based on previous location
        double speedKmh = calculateSpeed(previousLog, pingDTO);

        // Check if vehicle is near a stop
        boolean isNearStop = false;
        RouteStop nearestStop = null;
        BigDecimal distanceToStop = null;

        if (vehicle.getCurrentRoute() != null) {
            List<RouteStop> nearbyStops = routeStopRepository.findStopsNearLocation(
                    pingDTO.getLatitude(), pingDTO.getLongitude(), NEAR_STOP_THRESHOLD_KM);
            
            if (!nearbyStops.isEmpty()) {
                nearestStop = nearbyStops.get(0);
                distanceToStop = calculateDistance(
                        pingDTO.getLatitude(), pingDTO.getLongitude(),
                        nearestStop.getLatitude(), nearestStop.getLongitude());
                isNearStop = distanceToStop.doubleValue() <= NEAR_STOP_THRESHOLD_KM;
            }
        }

        // Check for route deviation
        boolean isDeviated = checkRouteDeviation(vehicle, pingDTO.getLatitude(), pingDTO.getLongitude());

        // Create and save GPS log
        GPSLog gpsLog = GPSLog.builder()
                .vehicle(vehicle)
                .latitude(pingDTO.getLatitude())
                .longitude(pingDTO.getLongitude())
                .speedKmh(BigDecimal.valueOf(speedKmh))
                .isNearStop(isNearStop)
                .isDeviated(isDeviated)
                .nearestStop(nearestStop)
                .distanceToStopMeters(distanceToStop != null ? distanceToStop.multiply(BigDecimal.valueOf(1000)) : null)
                .recordedAt(LocalDateTime.now())
                .build();

        gpsLogRepository.save(gpsLog);

        // Update vehicle's current location
        vehicle.setCurrentLatitude(pingDTO.getLatitude());
        vehicle.setCurrentLongitude(pingDTO.getLongitude());
        
        // Update vehicle status based on movement
        if (speedKmh > 0 && vehicle.getStatus() != VehicleStatus.ACTIVE) {
            vehicle.setStatus(VehicleStatus.ACTIVE);
        }
        
        vehicleRepository.save(vehicle);

        // Build status message
        String statusMessage = buildStatusMessage(vehicle, isNearStop, isDeviated, nearestStop, speedKmh);

        log.debug("Processed GPS ping for vehicle {}: speed={} km/h, nearStop={}, deviated={}",
                vehicle.getVehicleNumber(), speedKmh, isNearStop, isDeviated);

        return new TrackingStatusDTO(
                vehicle.getVehicleNumber(),
                isNearStop,
                isDeviated,
                speedKmh,
                statusMessage
        );
    }

    public TrackingStatusDTO getVehicleTrackingStatus(String vehicleNumber) {
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleNumber));

        Optional<GPSLog> latestLog = gpsLogRepository.findLatestByVehicleNumber(vehicleNumber);
        
        if (latestLog.isEmpty()) {
            return new TrackingStatusDTO(vehicleNumber, false, false, 0.0, "No GPS data available");
        }

        GPSLog log = latestLog.get();
        Double avgSpeed = gpsLogRepository.findAverageSpeedByVehicle(
                vehicle.getId(), LocalDateTime.now().minusMinutes(SPEED_CALCULATION_MINUTES));

        return new TrackingStatusDTO(
                vehicleNumber,
                log.getIsNearStop(),
                log.getIsDeviated(),
                avgSpeed != null ? avgSpeed : 0.0,
                buildStatusMessage(vehicle, log.getIsNearStop(), log.getIsDeviated(), 
                        log.getNearestStop(), log.getSpeedKmh().doubleValue())
        );
    }

    public List<GPSLog> getVehicleLocationHistory(Long vehicleId, LocalDateTime since) {
        return gpsLogRepository.findLogsByVehicleAndTimeRange(vehicleId, since);
    }

    public List<GPSLog> getVehicleLocationHistory(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        return gpsLogRepository.findByVehicleAndDateRange(vehicleId, start, end);
    }

    public List<GPSLog> getRecentDeviations(int minutes) {
        return gpsLogRepository.findDeviatedLogsSince(LocalDateTime.now().minusMinutes(minutes));
    }

    public Double getAverageSpeed(Long vehicleId, int minutes) {
        return gpsLogRepository.findAverageSpeedByVehicle(vehicleId, LocalDateTime.now().minusMinutes(minutes));
    }

    // Helper methods
    private double calculateSpeed(Optional<GPSLog> previousLog, GpsPingDTO currentPing) {
        if (previousLog.isEmpty()) {
            return 0.0;
        }

        GPSLog prevLog = previousLog.get();
        BigDecimal distance = calculateDistance(
                prevLog.getLatitude(), prevLog.getLongitude(),
                currentPing.getLatitude(), currentPing.getLongitude()
        );

        long timeDiffSeconds = ChronoUnit.SECONDS.between(prevLog.getRecordedAt(), LocalDateTime.now());
        if (timeDiffSeconds <= 0) {
            return 0.0;
        }

        // Convert to km/h: distance(km) / time(hours)
        double hours = timeDiffSeconds / 3600.0;
        return distance.doubleValue() / hours;
    }

    private BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        // Haversine formula for distance calculation
        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) * 
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(earthRadiusKm * c);
    }

    private boolean checkRouteDeviation(Vehicle vehicle, BigDecimal lat, BigDecimal lon) {
        if (vehicle.getCurrentRoute() == null) {
            return false;
        }

        // Find stops near the current location
        List<RouteStop> nearbyStops = routeStopRepository.findStopsNearLocation(
                lat, lon, ROUTE_DEVIATION_THRESHOLD_KM);

        // If no stops found within threshold, vehicle may be deviated
        if (nearbyStops.isEmpty()) {
            return true;
        }

        // Check if any of the nearby stops belong to the current route
        return nearbyStops.stream()
                .noneMatch(stop -> stop.getRoute().getId().equals(vehicle.getCurrentRoute().getId()));
    }

    private String buildStatusMessage(Vehicle vehicle, boolean isNearStop, boolean isDeviated, 
                                       RouteStop nearestStop, double speedKmh) {
        StringBuilder message = new StringBuilder();
        message.append("Vehicle ").append(vehicle.getVehicleNumber());

        if (isDeviated) {
            message.append(" - WARNING: Route deviation detected!");
        } else if (isNearStop && nearestStop != null) {
            message.append(" - Approaching stop: ").append(nearestStop.getStopName());
        } else if (speedKmh < 5) {
            message.append(" - Status: Idle/Stopped");
        } else {
            message.append(" - Status: In transit");
        }

        if (vehicle.getStatus() == VehicleStatus.DELAYED) {
            message.append(" [DELAYED]");
        }

        return message.toString();
    }

    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        gpsLogRepository.deleteByRecordedAtBefore(cutoff);
        log.info("Cleaned up GPS logs older than {} days", daysToKeep);
    }
}
