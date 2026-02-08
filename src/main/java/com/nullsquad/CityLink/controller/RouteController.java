package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.RouteDTO;
import com.nullsquad.CityLink.dto.RouteStopDTO;
import com.nullsquad.CityLink.entity.VehicleType;
import com.nullsquad.CityLink.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/active")
    public ResponseEntity<List<RouteDTO>> getActiveRoutes() {
        return ResponseEntity.ok(routeService.getActiveRoutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDTO> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/number/{routeNumber}")
    public ResponseEntity<RouteDTO> getRouteByNumber(@PathVariable String routeNumber) {
        return ResponseEntity.ok(routeService.getRouteByNumber(routeNumber));
    }

    @PostMapping
    public ResponseEntity<RouteDTO> createRoute(@Valid @RequestBody RouteDTO routeDTO) {
        RouteDTO created = routeService.createRoute(routeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteDTO> updateRoute(@PathVariable Long id,
                                                 @Valid @RequestBody RouteDTO routeDTO) {
        return ResponseEntity.ok(routeService.updateRoute(id, routeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<RouteDTO>> getRoutesByType(@PathVariable VehicleType type) {
        return ResponseEntity.ok(routeService.getRoutesByType(type));
    }

    @GetMapping("/express")
    public ResponseEntity<List<RouteDTO>> getExpressRoutes() {
        return ResponseEntity.ok(routeService.getExpressRoutes());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<RouteDTO>> getRoutesByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(routeService.getRoutesByZone(zoneId));
    }

    @GetMapping("/search/point")
    public ResponseEntity<List<RouteDTO>> searchRoutesByPoint(@RequestParam String point) {
        return ResponseEntity.ok(routeService.searchRoutesByPoint(point));
    }

    @GetMapping("/search/stop")
    public ResponseEntity<List<RouteDTO>> searchRoutesByStop(@RequestParam String stopName) {
        return ResponseEntity.ok(routeService.searchRoutesByStop(stopName));
    }

    @GetMapping("/fare/max")
    public ResponseEntity<List<RouteDTO>> getRoutesWithinFare(@RequestParam BigDecimal maxFare) {
        return ResponseEntity.ok(routeService.getRoutesWithinFare(maxFare));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<RouteDTO> toggleRouteActive(@PathVariable Long id,
                                                       @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(routeService.toggleRouteActive(id, body.get("active")));
    }

    @GetMapping("/{routeId}/stops")
    public ResponseEntity<List<RouteStopDTO>> getRouteStops(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeService.getRouteStops(routeId));
    }

    @PostMapping("/{routeId}/stops")
    public ResponseEntity<RouteStopDTO> addStop(@PathVariable Long routeId,
                                                 @Valid @RequestBody RouteStopDTO stopDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.addStop(routeId, stopDTO));
    }

    @PutMapping("/stops/{stopId}")
    public ResponseEntity<RouteStopDTO> updateStop(@PathVariable Long stopId,
                                                    @Valid @RequestBody RouteStopDTO stopDTO) {
        return ResponseEntity.ok(routeService.updateStop(stopId, stopDTO));
    }

    @DeleteMapping("/stops/{stopId}")
    public ResponseEntity<Void> deleteStop(@PathVariable Long stopId) {
        routeService.deleteStop(stopId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stops/nearby")
    public ResponseEntity<List<RouteStopDTO>> findNearbyStops(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            @RequestParam(defaultValue = "1.0") double radiusKm) {
        return ResponseEntity.ok(routeService.findNearbyStops(lat, lon, radiusKm));
    }

    @GetMapping("/stops/major")
    public ResponseEntity<List<RouteStopDTO>> getMajorStops() {
        return ResponseEntity.ok(routeService.getMajorStops());
    }
}
