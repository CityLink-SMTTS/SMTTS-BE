package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.ZoneDTO;
import com.nullsquad.CityLink.entity.Zone;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {

    private final ZoneRepository zoneRepository;

    public List<ZoneDTO> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ZoneDTO> getActiveZones() {
        return zoneRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ZoneDTO> getZonesOrderedByTier() {
        return zoneRepository.findAllOrderedByTier().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ZoneDTO getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        return toDTO(zone);
    }

    public ZoneDTO getZoneByCode(String zoneCode) {
        Zone zone = zoneRepository.findByZoneCode(zoneCode)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with code: " + zoneCode));
        return toDTO(zone);
    }

    @Transactional
    public ZoneDTO createZone(ZoneDTO dto) {
        if (zoneRepository.existsByZoneCode(dto.getZoneCode())) {
            throw new IllegalArgumentException("Zone code already exists: " + dto.getZoneCode());
        }

        Zone zone = toEntity(dto);
        zone = zoneRepository.save(zone);
        log.info("Created zone: {}", zone.getZoneCode());
        return toDTO(zone);
    }

    @Transactional
    public ZoneDTO updateZone(Long id, ZoneDTO dto) {
        Zone existing = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        updateEntityFromDTO(existing, dto);
        existing = zoneRepository.save(existing);
        log.info("Updated zone: {}", existing.getZoneCode());
        return toDTO(existing);
    }

    @Transactional
    public void deleteZone(Long id) {
        if (!zoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Zone not found with id: " + id);
        }
        zoneRepository.deleteById(id);
        log.info("Deleted zone with id: {}", id);
    }

    public List<ZoneDTO> getZonesByTier(Integer tier) {
        return zoneRepository.findByZoneTier(tier).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ZoneDTO> getZonesWithMaxMultiplier(BigDecimal maxMultiplier) {
        return zoneRepository.findByMaxFareMultiplier(maxMultiplier).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ZoneDTO updateFareMultiplier(Long id, BigDecimal multiplier) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        zone.setFareMultiplier(multiplier);
        zone = zoneRepository.save(zone);
        log.info("Updated fare multiplier for zone {} to {}", zone.getZoneCode(), multiplier);
        return toDTO(zone);
    }

    @Transactional
    public ZoneDTO toggleZoneActive(Long id, boolean active) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        zone.setIsActive(active);
        zone = zoneRepository.save(zone);
        log.info("Zone {} is now {}", zone.getZoneCode(), active ? "active" : "inactive");
        return toDTO(zone);
    }

    // Mapping methods
    private ZoneDTO toDTO(Zone zone) {
        int routeCount = zone.getRoutes() != null ? zone.getRoutes().size() : 0;
        int vehicleCount = zone.getVehicles() != null ? zone.getVehicles().size() : 0;

        return ZoneDTO.builder()
                .id(zone.getId())
                .zoneCode(zone.getZoneCode())
                .zoneName(zone.getZoneName())
                .description(zone.getDescription())
                .centerLatitude(zone.getCenterLatitude())
                .centerLongitude(zone.getCenterLongitude())
                .radiusKm(zone.getRadiusKm())
                .fareMultiplier(zone.getFareMultiplier())
                .zoneTier(zone.getZoneTier())
                .isActive(zone.getIsActive())
                .routeCount(routeCount)
                .vehicleCount(vehicleCount)
                .build();
    }

    private Zone toEntity(ZoneDTO dto) {
        return Zone.builder()
                .zoneCode(dto.getZoneCode())
                .zoneName(dto.getZoneName())
                .description(dto.getDescription())
                .centerLatitude(dto.getCenterLatitude())
                .centerLongitude(dto.getCenterLongitude())
                .radiusKm(dto.getRadiusKm())
                .fareMultiplier(dto.getFareMultiplier())
                .zoneTier(dto.getZoneTier())
                .isActive(dto.getIsActive())
                .build();
    }

    private void updateEntityFromDTO(Zone zone, ZoneDTO dto) {
        if (dto.getZoneName() != null) zone.setZoneName(dto.getZoneName());
        if (dto.getDescription() != null) zone.setDescription(dto.getDescription());
        if (dto.getCenterLatitude() != null) zone.setCenterLatitude(dto.getCenterLatitude());
        if (dto.getCenterLongitude() != null) zone.setCenterLongitude(dto.getCenterLongitude());
        if (dto.getRadiusKm() != null) zone.setRadiusKm(dto.getRadiusKm());
        if (dto.getFareMultiplier() != null) zone.setFareMultiplier(dto.getFareMultiplier());
        if (dto.getZoneTier() != null) zone.setZoneTier(dto.getZoneTier());
        if (dto.getIsActive() != null) zone.setIsActive(dto.getIsActive());
    }
}
