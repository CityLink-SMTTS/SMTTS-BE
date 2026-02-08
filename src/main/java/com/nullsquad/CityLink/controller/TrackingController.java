package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.GpsPingDTO;
import com.nullsquad.CityLink.dto.TrackingStatusDTO;
import com.nullsquad.CityLink.entity.GPSLog;
import com.nullsquad.CityLink.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/gps")
    public ResponseEntity<TrackingStatusDTO> updateLocation(@Valid @RequestBody GpsPingDTO pingDTO) {
        TrackingStatusDTO status = trackingService.processGpsPing(pingDTO);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/gps/batch")
    public ResponseEntity<Map<String, Integer>> updateLocationBatch(@Valid @RequestBody List<GpsPingDTO> pings) {
        int successCount = 0;
        int failCount = 0;

        for (GpsPingDTO ping : pings) {
            try {
                trackingService.processGpsPing(ping);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", successCount,
                "failed", failCount,
                "total", pings.size()
        ));
    }

    @GetMapping("/status/{vehicleNumber}")
    public ResponseEntity<TrackingStatusDTO> getVehicleStatus(@PathVariable String vehicleNumber) {
        return ResponseEntity.ok(trackingService.getVehicleTrackingStatus(vehicleNumber));
    }

    @GetMapping("/history/{vehicleId}")
    public ResponseEntity<List<GPSLog>> getLocationHistory(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ResponseEntity.ok(trackingService.getVehicleLocationHistory(vehicleId, since));
    }

    @GetMapping("/history/{vehicleId}/range")
    public ResponseEntity<List<GPSLog>> getLocationHistoryRange(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(trackingService.getVehicleLocationHistory(vehicleId, start, end));
    }

    @GetMapping("/deviations")
    public ResponseEntity<List<GPSLog>> getRecentDeviations(
            @RequestParam(defaultValue = "30") int minutes) {
        return ResponseEntity.ok(trackingService.getRecentDeviations(minutes));
    }

    @GetMapping("/speed/{vehicleId}")
    public ResponseEntity<Map<String, Object>> getAverageSpeed(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "30") int minutes) {
        Double avgSpeed = trackingService.getAverageSpeed(vehicleId, minutes);
        return ResponseEntity.ok(Map.of(
                "vehicleId", vehicleId,
                "averageSpeedKmh", avgSpeed != null ? avgSpeed : 0.0,
                "periodMinutes", minutes
        ));
    }

    @DeleteMapping("/logs/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldLogs(
            @RequestParam(defaultValue = "30") int daysToKeep) {
        trackingService.cleanupOldLogs(daysToKeep);
        return ResponseEntity.ok(Map.of("message", "Cleaned up logs older than " + daysToKeep + " days"));
    }
}
