package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.RouteDTO;
import com.nullsquad.CityLink.dto.RouteStopDTO;
import com.nullsquad.CityLink.entity.Route;
import com.nullsquad.CityLink.entity.RouteStop;
import com.nullsquad.CityLink.entity.VehicleType;
import com.nullsquad.CityLink.entity.Zone;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.RouteRepository;
import com.nullsquad.CityLink.repository.RouteStopRepository;
import com.nullsquad.CityLink.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final ZoneRepository zoneRepository;

    public List<RouteDTO> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteDTO> getActiveRoutes() {
        return routeRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RouteDTO getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return toDTO(route);
    }

    public RouteDTO getRouteByNumber(String routeNumber) {
        Route route = routeRepository.findByRouteNumber(routeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with number: " + routeNumber));
        return toDTO(route);
    }

    @Transactional
    public RouteDTO createRoute(RouteDTO dto) {
        if (routeRepository.existsByRouteNumber(dto.getRouteNumber())) {
            throw new IllegalArgumentException("Route number already exists: " + dto.getRouteNumber());
        }

        Route route = toEntity(dto);
        route = routeRepository.save(route);

        // Create stops if provided
        if (dto.getStops() != null && !dto.getStops().isEmpty()) {
            Route finalRoute = route;
            List<RouteStop> stops = dto.getStops().stream()
                    .map(stopDto -> toStopEntity(stopDto, finalRoute))
                    .collect(Collectors.toList());
            routeStopRepository.saveAll(stops);
            route.setStops(stops);
        }

        log.info("Created route: {}", route.getRouteNumber());
        return toDTO(route);
    }

    @Transactional
    public RouteDTO updateRoute(Long id, RouteDTO dto) {
        Route existing = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        updateEntityFromDTO(existing, dto);
        existing = routeRepository.save(existing);
        log.info("Updated route: {}", existing.getRouteNumber());
        return toDTO(existing);
    }

    @Transactional
    public void deleteRoute(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route not found with id: " + id);
        }
        routeRepository.deleteById(id);
        log.info("Deleted route with id: {}", id);
    }

    public List<RouteDTO> getRoutesByType(VehicleType type) {
        return routeRepository.findByRouteType(type).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteDTO> getExpressRoutes() {
        return routeRepository.findByIsExpressTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteDTO> getRoutesByZone(Long zoneId) {
        return routeRepository.findByZoneId(zoneId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteDTO> searchRoutesByPoint(String point) {
        return routeRepository.findByStartOrEndPoint(point).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteDTO> searchRoutesByStop(String stopName) {
        return routeRepository.findByStopName(stopName).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<RouteDTO> getRoutesWithinFare(BigDecimal maxFare) {
        return routeRepository.findActiveRoutesWithinFare(maxFare).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RouteDTO toggleRouteActive(Long id, boolean active) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        route.setIsActive(active);
        route = routeRepository.save(route);
        log.info("Route {} is now {}", route.getRouteNumber(), active ? "active" : "inactive");
        return toDTO(route);
    }

    // Stop management
    public List<RouteStopDTO> getRouteStops(Long routeId) {
        return routeStopRepository.findByRouteIdOrderByStopOrderAsc(routeId).stream()
                .map(this::toStopDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RouteStopDTO addStop(Long routeId, RouteStopDTO dto) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        RouteStop stop = toStopEntity(dto, route);
        stop = routeStopRepository.save(stop);
        log.info("Added stop {} to route {}", stop.getStopName(), route.getRouteNumber());
        return toStopDTO(stop);
    }

    @Transactional
    public RouteStopDTO updateStop(Long stopId, RouteStopDTO dto) {
        RouteStop stop = routeStopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        updateStopEntityFromDTO(stop, dto);
        stop = routeStopRepository.save(stop);
        log.info("Updated stop: {}", stop.getStopName());
        return toStopDTO(stop);
    }

    @Transactional
    public void deleteStop(Long stopId) {
        if (!routeStopRepository.existsById(stopId)) {
            throw new ResourceNotFoundException("Stop not found with id: " + stopId);
        }
        routeStopRepository.deleteById(stopId);
        log.info("Deleted stop with id: {}", stopId);
    }

    public List<RouteStopDTO> findNearbyStops(BigDecimal lat, BigDecimal lon, double radiusKm) {
        return routeStopRepository.findStopsNearLocation(lat, lon, radiusKm).stream()
                .map(this::toStopDTO)
                .collect(Collectors.toList());
    }

    public List<RouteStopDTO> getMajorStops() {
        return routeStopRepository.findMajorStops().stream()
                .map(this::toStopDTO)
                .collect(Collectors.toList());
    }

    // Mapping methods
    private RouteDTO toDTO(Route route) {
        List<RouteStopDTO> stopDTOs = new ArrayList<>();
        if (route.getStops() != null) {
            stopDTOs = route.getStops().stream()
                    .map(this::toStopDTO)
                    .collect(Collectors.toList());
        }

        return RouteDTO.builder()
                .id(route.getId())
                .routeNumber(route.getRouteNumber())
                .routeName(route.getRouteName())
                .description(route.getDescription())
                .routeType(route.getRouteType())
                .startPoint(route.getStartPoint())
                .endPoint(route.getEndPoint())
                .startLatitude(route.getStartLatitude())
                .startLongitude(route.getStartLongitude())
                .endLatitude(route.getEndLatitude())
                .endLongitude(route.getEndLongitude())
                .totalDistanceKm(route.getTotalDistanceKm())
                .estimatedDurationMinutes(route.getEstimatedDurationMinutes())
                .firstDepartureTime(route.getFirstDepartureTime())
                .lastDepartureTime(route.getLastDepartureTime())
                .frequencyMinutes(route.getFrequencyMinutes())
                .zoneId(route.getZone() != null ? route.getZone().getId() : null)
                .zoneName(route.getZone() != null ? route.getZone().getZoneName() : null)
                .stops(stopDTOs)
                .baseFare(route.getBaseFare())
                .isActive(route.getIsActive())
                .isExpress(route.getIsExpress())
                .build();
    }

    private Route toEntity(RouteDTO dto) {
        Route route = Route.builder()
                .routeNumber(dto.getRouteNumber())
                .routeName(dto.getRouteName())
                .description(dto.getDescription())
                .routeType(dto.getRouteType())
                .startPoint(dto.getStartPoint())
                .endPoint(dto.getEndPoint())
                .startLatitude(dto.getStartLatitude())
                .startLongitude(dto.getStartLongitude())
                .endLatitude(dto.getEndLatitude())
                .endLongitude(dto.getEndLongitude())
                .totalDistanceKm(dto.getTotalDistanceKm())
                .estimatedDurationMinutes(dto.getEstimatedDurationMinutes())
                .firstDepartureTime(dto.getFirstDepartureTime())
                .lastDepartureTime(dto.getLastDepartureTime())
                .frequencyMinutes(dto.getFrequencyMinutes())
                .baseFare(dto.getBaseFare())
                .isActive(dto.getIsActive())
                .isExpress(dto.getIsExpress())
                .build();

        if (dto.getZoneId() != null) {
            zoneRepository.findById(dto.getZoneId())
                    .ifPresent(route::setZone);
        }
        return route;
    }

    private void updateEntityFromDTO(Route route, RouteDTO dto) {
        if (dto.getRouteName() != null) route.setRouteName(dto.getRouteName());
        if (dto.getDescription() != null) route.setDescription(dto.getDescription());
        if (dto.getRouteType() != null) route.setRouteType(dto.getRouteType());
        if (dto.getStartPoint() != null) route.setStartPoint(dto.getStartPoint());
        if (dto.getEndPoint() != null) route.setEndPoint(dto.getEndPoint());
        if (dto.getStartLatitude() != null) route.setStartLatitude(dto.getStartLatitude());
        if (dto.getStartLongitude() != null) route.setStartLongitude(dto.getStartLongitude());
        if (dto.getEndLatitude() != null) route.setEndLatitude(dto.getEndLatitude());
        if (dto.getEndLongitude() != null) route.setEndLongitude(dto.getEndLongitude());
        if (dto.getTotalDistanceKm() != null) route.setTotalDistanceKm(dto.getTotalDistanceKm());
        if (dto.getEstimatedDurationMinutes() != null) route.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        if (dto.getFirstDepartureTime() != null) route.setFirstDepartureTime(dto.getFirstDepartureTime());
        if (dto.getLastDepartureTime() != null) route.setLastDepartureTime(dto.getLastDepartureTime());
        if (dto.getFrequencyMinutes() != null) route.setFrequencyMinutes(dto.getFrequencyMinutes());
        if (dto.getBaseFare() != null) route.setBaseFare(dto.getBaseFare());
        if (dto.getIsActive() != null) route.setIsActive(dto.getIsActive());
        if (dto.getIsExpress() != null) route.setIsExpress(dto.getIsExpress());

        if (dto.getZoneId() != null) {
            zoneRepository.findById(dto.getZoneId())
                    .ifPresent(route::setZone);
        }
    }

    private RouteStopDTO toStopDTO(RouteStop stop) {
        return RouteStopDTO.builder()
                .id(stop.getId())
                .routeId(stop.getRoute().getId())
                .stopName(stop.getStopName())
                .stopOrder(stop.getStopOrder())
                .latitude(stop.getLatitude())
                .longitude(stop.getLongitude())
                .distanceFromStartKm(stop.getDistanceFromStartKm())
                .estimatedArrivalMinutes(stop.getEstimatedArrivalMinutes())
                .isMajorStop(stop.getIsMajorStop())
                .hasShelter(stop.getHasShelter())
                .isAccessible(stop.getIsAccessible())
                .zoneId(stop.getZone() != null ? stop.getZone().getId() : null)
                .zoneName(stop.getZone() != null ? stop.getZone().getZoneName() : null)
                .build();
    }

    private RouteStop toStopEntity(RouteStopDTO dto, Route route) {
        RouteStop stop = RouteStop.builder()
                .route(route)
                .stopName(dto.getStopName())
                .stopOrder(dto.getStopOrder())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .distanceFromStartKm(dto.getDistanceFromStartKm())
                .estimatedArrivalMinutes(dto.getEstimatedArrivalMinutes())
                .isMajorStop(dto.getIsMajorStop())
                .hasShelter(dto.getHasShelter())
                .isAccessible(dto.getIsAccessible())
                .build();

        if (dto.getZoneId() != null) {
            zoneRepository.findById(dto.getZoneId())
                    .ifPresent(stop::setZone);
        }
        return stop;
    }

    private void updateStopEntityFromDTO(RouteStop stop, RouteStopDTO dto) {
        if (dto.getStopName() != null) stop.setStopName(dto.getStopName());
        if (dto.getStopOrder() != null) stop.setStopOrder(dto.getStopOrder());
        if (dto.getLatitude() != null) stop.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) stop.setLongitude(dto.getLongitude());
        if (dto.getDistanceFromStartKm() != null) stop.setDistanceFromStartKm(dto.getDistanceFromStartKm());
        if (dto.getEstimatedArrivalMinutes() != null) stop.setEstimatedArrivalMinutes(dto.getEstimatedArrivalMinutes());
        if (dto.getIsMajorStop() != null) stop.setIsMajorStop(dto.getIsMajorStop());
        if (dto.getHasShelter() != null) stop.setHasShelter(dto.getHasShelter());
        if (dto.getIsAccessible() != null) stop.setIsAccessible(dto.getIsAccessible());

        if (dto.getZoneId() != null) {
            zoneRepository.findById(dto.getZoneId())
                    .ifPresent(stop::setZone);
        }
    }
}
