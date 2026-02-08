package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.VehicleDTO;
import com.nullsquad.CityLink.entity.Route;
import com.nullsquad.CityLink.entity.Vehicle;
import com.nullsquad.CityLink.entity.VehicleStatus;
import com.nullsquad.CityLink.entity.VehicleType;
import com.nullsquad.CityLink.entity.Zone;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.RouteRepository;
import com.nullsquad.CityLink.repository.VehicleRepository;
import com.nullsquad.CityLink.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final ZoneRepository zoneRepository;

    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VehicleDTO getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return toDTO(vehicle);
    }

    public VehicleDTO getVehicleByNumber(String vehicleNumber) {
        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with number: " + vehicleNumber));
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO createVehicle(VehicleDTO dto) {
        if (vehicleRepository.existsByVehicleNumber(dto.getVehicleNumber())) {
            throw new IllegalArgumentException("Vehicle number already exists: " + dto.getVehicleNumber());
        }
        if (vehicleRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already exists: " + dto.getLicensePlate());
        }

        Vehicle vehicle = toEntity(dto);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Created vehicle: {}", vehicle.getVehicleNumber());
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO updateVehicle(Long id, VehicleDTO dto) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        updateEntityFromDTO(existing, dto);
        existing = vehicleRepository.save(existing);
        log.info("Updated vehicle: {}", existing.getVehicleNumber());
        return toDTO(existing);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
        log.info("Deleted vehicle with id: {}", id);
    }

    public List<VehicleDTO> getVehiclesByType(VehicleType type) {
        return vehicleRepository.findByVehicleType(type).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getActiveVehiclesWithLocation() {
        return vehicleRepository.findActiveVehiclesWithLocation().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getVehiclesByZone(Long zoneId) {
        return vehicleRepository.findByZoneId(zoneId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getVehiclesOnRoute(Long routeId) {
        return vehicleRepository.findByCurrentRouteId(routeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getEcoFriendlyVehicles() {
        return vehicleRepository.findActiveEcoFriendlyVehicles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getVehiclesNeedingMaintenance() {
        return vehicleRepository.findVehiclesNeedingMaintenance().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleDTO updateVehicleStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        vehicle.setStatus(status);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Updated vehicle {} status to {}", vehicle.getVehicleNumber(), status);
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO assignVehicleToRoute(Long vehicleId, Long routeId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        vehicle.setCurrentRoute(route);
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Assigned vehicle {} to route {}", vehicle.getVehicleNumber(), route.getRouteNumber());
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO assignVehicleToZone(Long vehicleId, Long zoneId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));

        vehicle.setAssignedZone(zone);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Assigned vehicle {} to zone {}", vehicle.getVehicleNumber(), zone.getZoneCode());
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO scheduleMaintenance(Long vehicleId, LocalDateTime maintenanceDate) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        
        vehicle.setNextMaintenanceDate(maintenanceDate);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Scheduled maintenance for vehicle {} on {}", vehicle.getVehicleNumber(), maintenanceDate);
        return toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO completeMaintenance(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        
        vehicle.setLastMaintenanceDate(LocalDateTime.now());
        vehicle.setNextMaintenanceDate(null);
        vehicle.setStatus(VehicleStatus.INACTIVE);
        vehicle = vehicleRepository.save(vehicle);
        log.info("Completed maintenance for vehicle {}", vehicle.getVehicleNumber());
        return toDTO(vehicle);
    }

    public Long getVehicleCountByStatus(VehicleStatus status) {
        return vehicleRepository.countByStatus(status);
    }

    // Mapping methods
    private VehicleDTO toDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .licensePlate(vehicle.getLicensePlate())
                .vehicleType(vehicle.getVehicleType())
                .status(vehicle.getStatus())
                .capacity(vehicle.getCapacity())
                .currentOccupancy(vehicle.getCurrentOccupancy())
                .manufacturer(vehicle.getManufacturer())
                .model(vehicle.getModel())
                .manufactureYear(vehicle.getManufactureYear())
                .currentRouteId(vehicle.getCurrentRoute() != null ? vehicle.getCurrentRoute().getId() : null)
                .currentRouteName(vehicle.getCurrentRoute() != null ? vehicle.getCurrentRoute().getRouteName() : null)
                .assignedZoneId(vehicle.getAssignedZone() != null ? vehicle.getAssignedZone().getId() : null)
                .assignedZoneName(vehicle.getAssignedZone() != null ? vehicle.getAssignedZone().getZoneName() : null)
                .currentLatitude(vehicle.getCurrentLatitude())
                .currentLongitude(vehicle.getCurrentLongitude())
                .lastMaintenanceDate(vehicle.getLastMaintenanceDate())
                .nextMaintenanceDate(vehicle.getNextMaintenanceDate())
                .fuelType(vehicle.getFuelType())
                .isEcoFriendly(vehicle.getIsEcoFriendly())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    private Vehicle toEntity(VehicleDTO dto) {
        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber(dto.getVehicleNumber())
                .licensePlate(dto.getLicensePlate())
                .vehicleType(dto.getVehicleType())
                .status(dto.getStatus() != null ? dto.getStatus() : VehicleStatus.INACTIVE)
                .capacity(dto.getCapacity())
                .currentOccupancy(dto.getCurrentOccupancy())
                .manufacturer(dto.getManufacturer())
                .model(dto.getModel())
                .manufactureYear(dto.getManufactureYear())
                .currentLatitude(dto.getCurrentLatitude())
                .currentLongitude(dto.getCurrentLongitude())
                .lastMaintenanceDate(dto.getLastMaintenanceDate())
                .nextMaintenanceDate(dto.getNextMaintenanceDate())
                .fuelType(dto.getFuelType())
                .isEcoFriendly(dto.getIsEcoFriendly())
                .build();

        if (dto.getCurrentRouteId() != null) {
            routeRepository.findById(dto.getCurrentRouteId())
                    .ifPresent(vehicle::setCurrentRoute);
        }
        if (dto.getAssignedZoneId() != null) {
            zoneRepository.findById(dto.getAssignedZoneId())
                    .ifPresent(vehicle::setAssignedZone);
        }
        return vehicle;
    }

    private void updateEntityFromDTO(Vehicle vehicle, VehicleDTO dto) {
        if (dto.getVehicleNumber() != null) vehicle.setVehicleNumber(dto.getVehicleNumber());
        if (dto.getLicensePlate() != null) vehicle.setLicensePlate(dto.getLicensePlate());
        if (dto.getVehicleType() != null) vehicle.setVehicleType(dto.getVehicleType());
        if (dto.getStatus() != null) vehicle.setStatus(dto.getStatus());
        if (dto.getCapacity() != null) vehicle.setCapacity(dto.getCapacity());
        if (dto.getCurrentOccupancy() != null) vehicle.setCurrentOccupancy(dto.getCurrentOccupancy());
        if (dto.getManufacturer() != null) vehicle.setManufacturer(dto.getManufacturer());
        if (dto.getModel() != null) vehicle.setModel(dto.getModel());
        if (dto.getManufactureYear() != null) vehicle.setManufactureYear(dto.getManufactureYear());
        if (dto.getFuelType() != null) vehicle.setFuelType(dto.getFuelType());
        if (dto.getIsEcoFriendly() != null) vehicle.setIsEcoFriendly(dto.getIsEcoFriendly());
        if (dto.getNextMaintenanceDate() != null) vehicle.setNextMaintenanceDate(dto.getNextMaintenanceDate());

        if (dto.getCurrentRouteId() != null) {
            routeRepository.findById(dto.getCurrentRouteId())
                    .ifPresent(vehicle::setCurrentRoute);
        }
        if (dto.getAssignedZoneId() != null) {
            zoneRepository.findById(dto.getAssignedZoneId())
                    .ifPresent(vehicle::setAssignedZone);
        }
    }
}
