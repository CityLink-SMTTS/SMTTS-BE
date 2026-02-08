package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.FareCalculationRequest;
import com.nullsquad.CityLink.dto.FareCalculationResponse;
import com.nullsquad.CityLink.dto.FareRuleDTO;
import com.nullsquad.CityLink.entity.VehicleType;
import com.nullsquad.CityLink.service.FareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    /**
     * Calculate fare based on stops
     */
    @PostMapping("/calculate")
    public ResponseEntity<FareCalculationResponse> calculateFare(
            @Valid @RequestBody FareCalculationRequest request) {
        return ResponseEntity.ok(fareService.calculateFare(request));
    }

    /**
     * Calculate zonal fare only (simplified)
     */
    @GetMapping("/calculate/zones")
    public ResponseEntity<FareCalculationResponse> calculateZonalFare(
            @RequestParam Long fromZoneId,
            @RequestParam Long toZoneId,
            @RequestParam(required = false, defaultValue = "BUS") VehicleType vehicleType) {
        return ResponseEntity.ok(fareService.calculateZonalFare(fromZoneId, toZoneId, vehicleType));
    }

    /**
     * Get all fare rules
     */
    @GetMapping("/rules")
    public ResponseEntity<List<FareRuleDTO>> getAllFareRules() {
        return ResponseEntity.ok(fareService.getAllFareRules());
    }

    /**
     * Get active fare rules only
     */
    @GetMapping("/rules/active")
    public ResponseEntity<List<FareRuleDTO>> getActiveFareRules() {
        return ResponseEntity.ok(fareService.getActiveFareRules());
    }

    /**
     * Get fare rule by ID
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<FareRuleDTO> getFareRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(fareService.getFareRuleById(id));
    }

    /**
     * Create a new fare rule
     */
    @PostMapping("/rules")
    public ResponseEntity<FareRuleDTO> createFareRule(@Valid @RequestBody FareRuleDTO fareRuleDTO) {
        FareRuleDTO created = fareService.createFareRule(fareRuleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing fare rule
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<FareRuleDTO> updateFareRule(@PathVariable Long id,
                                                       @Valid @RequestBody FareRuleDTO fareRuleDTO) {
        return ResponseEntity.ok(fareService.updateFareRule(id, fareRuleDTO));
    }

    /**
     * Delete a fare rule
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteFareRule(@PathVariable Long id) {
        fareService.deleteFareRule(id);
        return ResponseEntity.noContent().build();
    }
}
