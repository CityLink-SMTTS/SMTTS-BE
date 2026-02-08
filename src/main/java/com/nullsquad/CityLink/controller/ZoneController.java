package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.ZoneDTO;
import com.nullsquad.CityLink.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ResponseEntity<List<ZoneDTO>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ZoneDTO>> getActiveZones() {
        return ResponseEntity.ok(zoneService.getActiveZones());
    }

    @GetMapping("/ordered")
    public ResponseEntity<List<ZoneDTO>> getZonesOrderedByTier() {
        return ResponseEntity.ok(zoneService.getZonesOrderedByTier());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneDTO> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getZoneById(id));
    }

    @GetMapping("/code/{zoneCode}")
    public ResponseEntity<ZoneDTO> getZoneByCode(@PathVariable String zoneCode) {
        return ResponseEntity.ok(zoneService.getZoneByCode(zoneCode));
    }

    @PostMapping
    public ResponseEntity<ZoneDTO> createZone(@Valid @RequestBody ZoneDTO zoneDTO) {
        ZoneDTO created = zoneService.createZone(zoneDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneDTO> updateZone(@PathVariable Long id,
                                               @Valid @RequestBody ZoneDTO zoneDTO) {
        return ResponseEntity.ok(zoneService.updateZone(id, zoneDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tier/{tier}")
    public ResponseEntity<List<ZoneDTO>> getZonesByTier(@PathVariable Integer tier) {
        return ResponseEntity.ok(zoneService.getZonesByTier(tier));
    }

    @GetMapping("/fare/max-multiplier")
    public ResponseEntity<List<ZoneDTO>> getZonesWithMaxMultiplier(@RequestParam BigDecimal maxMultiplier) {
        return ResponseEntity.ok(zoneService.getZonesWithMaxMultiplier(maxMultiplier));
    }

    @PatchMapping("/{id}/fare-multiplier")
    public ResponseEntity<ZoneDTO> updateFareMultiplier(@PathVariable Long id,
                                                         @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(zoneService.updateFareMultiplier(id, body.get("multiplier")));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ZoneDTO> toggleZoneActive(@PathVariable Long id,
                                                     @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(zoneService.toggleZoneActive(id, body.get("active")));
    }
}
