package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.VehicleDTO;
import com.nullsquad.CityLink.entity.VehicleStatus;
import com.nullsquad.CityLink.entity.VehicleType;
import com.nullsquad.CityLink.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/number/{vehicleNumber}")
    public ResponseEntity<VehicleDTO> getVehicleByNumber(@PathVariable String vehicleNumber) {
        return ResponseEntity.ok(vehicleService.getVehicleByNumber(vehicleNumber));
    }

    @PostMapping
    public ResponseEntity<VehicleDTO> createVehicle(@Valid @RequestBody VehicleDTO vehicleDTO) {
        VehicleDTO created = vehicleService.createVehicle(vehicleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDTO> updateVehicle(@PathVariable Long id, 
                                                     @Valid @RequestBody VehicleDTO vehicleDTO) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, vehicleDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByType(@PathVariable VehicleType type) {
        return ResponseEntity.ok(vehicleService.getVehiclesByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        return ResponseEntity.ok(vehicleService.getVehiclesByStatus(status));
    }

    @GetMapping("/active/with-location")
    public ResponseEntity<List<VehicleDTO>> getActiveVehiclesWithLocation() {
        return ResponseEntity.ok(vehicleService.getActiveVehiclesWithLocation());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(vehicleService.getVehiclesByZone(zoneId));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesOnRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(vehicleService.getVehiclesOnRoute(routeId));
    }

    @GetMapping("/eco-friendly")
    public ResponseEntity<List<VehicleDTO>> getEcoFriendlyVehicles() {
        return ResponseEntity.ok(vehicleService.getEcoFriendlyVehicles());
    }

    @GetMapping("/maintenance/needed")
    public ResponseEntity<List<VehicleDTO>> getVehiclesNeedingMaintenance() {
        return ResponseEntity.ok(vehicleService.getVehiclesNeedingMaintenance());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleDTO> updateVehicleStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        VehicleStatus status = VehicleStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(vehicleService.updateVehicleStatus(id, status));
    }

    @PostMapping("/{vehicleId}/assign/route/{routeId}")
    public ResponseEntity<VehicleDTO> assignVehicleToRoute(@PathVariable Long vehicleId,
                                                            @PathVariable Long routeId) {
        return ResponseEntity.ok(vehicleService.assignVehicleToRoute(vehicleId, routeId));
    }

    @PostMapping("/{vehicleId}/assign/zone/{zoneId}")
    public ResponseEntity<VehicleDTO> assignVehicleToZone(@PathVariable Long vehicleId,
                                                           @PathVariable Long zoneId) {
        return ResponseEntity.ok(vehicleService.assignVehicleToZone(vehicleId, zoneId));
    }

    @PostMapping("/{id}/maintenance/schedule")
    public ResponseEntity<VehicleDTO> scheduleMaintenance(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        LocalDateTime maintenanceDate = LocalDateTime.parse(body.get("maintenanceDate"));
        return ResponseEntity.ok(vehicleService.scheduleMaintenance(id, maintenanceDate));
    }

    @PostMapping("/{id}/maintenance/complete")
    public ResponseEntity<VehicleDTO> completeMaintenance(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.completeMaintenance(id));
    }

    @GetMapping("/stats/count/{status}")
    public ResponseEntity<Map<String, Long>> getVehicleCountByStatus(@PathVariable VehicleStatus status) {
        Long count = vehicleService.getVehicleCountByStatus(status);
        return ResponseEntity.ok(Map.of("count", count, "status", (long) status.ordinal()));
    }
}
