package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.*;
import com.nullsquad.CityLink.entity.*;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FareService {

    private final FareRuleRepository fareRuleRepository;
    private final RouteStopRepository routeStopRepository;
    private final ZoneRepository zoneRepository;

    // Peak hours: 7-9 AM and 5-7 PM
    private static final LocalTime PEAK_MORNING_START = LocalTime.of(7, 0);
    private static final LocalTime PEAK_MORNING_END = LocalTime.of(9, 0);
    private static final LocalTime PEAK_EVENING_START = LocalTime.of(17, 0);
    private static final LocalTime PEAK_EVENING_END = LocalTime.of(19, 0);
    
    // Night hours: 10 PM - 6 AM
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(6, 0);
    
    // Default rates
    private static final BigDecimal DEFAULT_BASE_FARE = new BigDecimal("50.00");
    private static final BigDecimal DEFAULT_PER_KM_RATE = new BigDecimal("5.00");
    private static final BigDecimal DEFAULT_PER_ZONE_RATE = new BigDecimal("20.00");
    private static final int POINTS_PER_TRANSACTION = 10;
    private static final int GREEN_SCORE_BASE = 5;

    /**
     * Calculate fare based on stops
     */
    public FareCalculationResponse calculateFare(FareCalculationRequest request) {
        // Get stop details
        RouteStop fromStop = routeStopRepository.findById(request.getFromStopId())
                .orElseThrow(() -> new ResourceNotFoundException("From stop not found"));
        RouteStop toStop = routeStopRepository.findById(request.getToStopId())
                .orElseThrow(() -> new ResourceNotFoundException("To stop not found"));

        // Calculate distance
        BigDecimal distanceKm = calculateDistance(
                fromStop.getLatitude(), fromStop.getLongitude(),
                toStop.getLatitude(), toStop.getLongitude()
        );

        // Get zones
        Zone fromZone = fromStop.getZone();
        Zone toZone = toStop.getZone();
        int zonesCrossed = calculateZonesCrossed(fromZone, toZone);

        // Find applicable fare rule
        VehicleType vehicleType = request.getVehicleType() != null ? request.getVehicleType() : VehicleType.BUS;
        FareRule fareRule = findApplicableFareRule(vehicleType, fromZone, toZone);

        // Calculate base components
        BigDecimal baseFare = fareRule != null ? fareRule.getBaseFare() : DEFAULT_BASE_FARE;
        BigDecimal perKmRate = fareRule != null && fareRule.getPerKmRate() != null ? 
                fareRule.getPerKmRate() : DEFAULT_PER_KM_RATE;
        BigDecimal perZoneRate = fareRule != null && fareRule.getPerZoneRate() != null ? 
                fareRule.getPerZoneRate() : DEFAULT_PER_ZONE_RATE;

        // Calculate distance and zone charges
        BigDecimal distanceCharge = distanceKm.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal zoneCharge = perZoneRate.multiply(BigDecimal.valueOf(zonesCrossed)).setScale(2, RoundingMode.HALF_UP);

        // Apply zone multipliers if available
        if (fromZone != null && fromZone.getFareMultiplier() != null) {
            distanceCharge = distanceCharge.multiply(fromZone.getFareMultiplier());
        }

        // Calculate time-based multipliers
        LocalDateTime now = LocalDateTime.now();
        BigDecimal timeMultiplier = calculateTimeMultiplier(now, fareRule);
        boolean isPeakHour = isPeakHour(now.toLocalTime());
        boolean isNightFare = isNightTime(now.toLocalTime());
        boolean isWeekend = isWeekend(now);

        // Calculate subtotal
        BigDecimal subtotal = baseFare.add(distanceCharge).add(zoneCharge);
        subtotal = subtotal.multiply(timeMultiplier).setScale(2, RoundingMode.HALF_UP);

        // Apply discounts
        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountReason = null;

        if (request.getPassengerType() != null) {
            switch (request.getPassengerType().toUpperCase()) {
                case "STUDENT":
                    if (fareRule != null && fareRule.getStudentDiscountPercent() != null) {
                        discountAmount = subtotal.multiply(fareRule.getStudentDiscountPercent())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        discountReason = "Student discount";
                    }
                    break;
                case "SENIOR":
                    if (fareRule != null && fareRule.getSeniorDiscountPercent() != null) {
                        discountAmount = subtotal.multiply(fareRule.getSeniorDiscountPercent())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        discountReason = "Senior citizen discount";
                    }
                    break;
            }
        }

        // Calculate total fare
        BigDecimal totalFare = subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        // Apply min/max constraints
        if (fareRule != null) {
            if (fareRule.getMinimumFare() != null && totalFare.compareTo(fareRule.getMinimumFare()) < 0) {
                totalFare = fareRule.getMinimumFare();
            }
            if (fareRule.getMaximumFare() != null && totalFare.compareTo(fareRule.getMaximumFare()) > 0) {
                totalFare = fareRule.getMaximumFare();
            }
        }

        // Estimate duration and rewards
        int estimatedDuration = estimateDuration(distanceKm);
        int estimatedGiftPoints = POINTS_PER_TRANSACTION + (int) (totalFare.doubleValue() / 10);
        int estimatedGreenScore = calculateGreenScore(vehicleType, distanceKm);

        return FareCalculationResponse.builder()
                .fromStopId(fromStop.getId())
                .fromStopName(fromStop.getStopName())
                .fromZoneId(fromZone != null ? fromZone.getId() : null)
                .fromZoneName(fromZone != null ? fromZone.getZoneName() : null)
                .toStopId(toStop.getId())
                .toStopName(toStop.getStopName())
                .toZoneId(toZone != null ? toZone.getId() : null)
                .toZoneName(toZone != null ? toZone.getZoneName() : null)
                .distanceKm(distanceKm)
                .zonesCrossed(zonesCrossed)
                .baseFare(baseFare)
                .distanceCharge(distanceCharge)
                .zoneCharge(zoneCharge)
                .timeMultiplier(timeMultiplier)
                .discountAmount(discountAmount)
                .discountReason(discountReason)
                .subtotal(subtotal)
                .totalFare(totalFare)
                .estimatedDurationMinutes(estimatedDuration)
                .estimatedGiftPoints(estimatedGiftPoints)
                .estimatedGreenScore(estimatedGreenScore)
                .fareRuleApplied(fareRule != null ? fareRule.getRuleName() : "Default")
                .isPeakHour(isPeakHour)
                .isNightFare(isNightFare)
                .isWeekend(isWeekend)
                .build();
    }

    /**
     * Calculate fare between zones only
     */
    public FareCalculationResponse calculateZonalFare(Long fromZoneId, Long toZoneId, VehicleType vehicleType) {
        Zone fromZone = zoneRepository.findById(fromZoneId)
                .orElseThrow(() -> new ResourceNotFoundException("From zone not found"));
        Zone toZone = zoneRepository.findById(toZoneId)
                .orElseThrow(() -> new ResourceNotFoundException("To zone not found"));

        int zonesCrossed = calculateZonesCrossed(fromZone, toZone);
        FareRule fareRule = findApplicableFareRule(vehicleType, fromZone, toZone);

        BigDecimal baseFare = fareRule != null ? fareRule.getBaseFare() : DEFAULT_BASE_FARE;
        BigDecimal perZoneRate = fareRule != null && fareRule.getPerZoneRate() != null ?
                fareRule.getPerZoneRate() : DEFAULT_PER_ZONE_RATE;

        BigDecimal zoneCharge = perZoneRate.multiply(BigDecimal.valueOf(zonesCrossed));
        BigDecimal totalFare = baseFare.add(zoneCharge);

        // Apply zone multipliers
        if (fromZone.getFareMultiplier() != null) {
            totalFare = totalFare.multiply(fromZone.getFareMultiplier());
        }
        if (toZone.getFareMultiplier() != null && !toZone.getId().equals(fromZone.getId())) {
            totalFare = totalFare.multiply(toZone.getFareMultiplier());
        }

        return FareCalculationResponse.builder()
                .fromZoneId(fromZone.getId())
                .fromZoneName(fromZone.getZoneName())
                .toZoneId(toZone.getId())
                .toZoneName(toZone.getZoneName())
                .zonesCrossed(zonesCrossed)
                .baseFare(baseFare)
                .zoneCharge(zoneCharge)
                .totalFare(totalFare.setScale(2, RoundingMode.HALF_UP))
                .fareRuleApplied(fareRule != null ? fareRule.getRuleName() : "Default")
                .build();
    }

    // Fare Rule Management
    public List<FareRuleDTO> getAllFareRules() {
        return fareRuleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<FareRuleDTO> getActiveFareRules() {
        return fareRuleRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public FareRuleDTO getFareRuleById(Long id) {
        FareRule fareRule = fareRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fare rule not found"));
        return toDTO(fareRule);
    }

    @Transactional
    public FareRuleDTO createFareRule(FareRuleDTO dto) {
        if (fareRuleRepository.existsByRuleName(dto.getRuleName())) {
            throw new IllegalArgumentException("Fare rule name already exists: " + dto.getRuleName());
        }

        FareRule fareRule = toEntity(dto);
        fareRule = fareRuleRepository.save(fareRule);
        log.info("Created fare rule: {}", fareRule.getRuleName());
        return toDTO(fareRule);
    }

    @Transactional
    public FareRuleDTO updateFareRule(Long id, FareRuleDTO dto) {
        FareRule existing = fareRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fare rule not found"));

        updateEntityFromDTO(existing, dto);
        existing = fareRuleRepository.save(existing);
        log.info("Updated fare rule: {}", existing.getRuleName());
        return toDTO(existing);
    }

    @Transactional
    public void deleteFareRule(Long id) {
        if (!fareRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fare rule not found");
        }
        fareRuleRepository.deleteById(id);
        log.info("Deleted fare rule with id: {}", id);
    }

    // Helper methods
    private FareRule findApplicableFareRule(VehicleType vehicleType, Zone fromZone, Zone toZone) {
        if (fromZone != null && toZone != null) {
            List<FareRule> rules = fareRuleRepository.findApplicableRules(
                    vehicleType, fromZone.getId(), toZone.getId());
            if (!rules.isEmpty()) {
                return rules.get(0);
            }
        }
        
        // Fallback to vehicle type only
        List<FareRule> vehicleTypeRules = fareRuleRepository.findByVehicleTypeAndIsActiveTrue(vehicleType);
        return vehicleTypeRules.isEmpty() ? null : vehicleTypeRules.get(0);
    }

    private BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) *
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(earthRadiusKm * c).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateZonesCrossed(Zone fromZone, Zone toZone) {
        if (fromZone == null || toZone == null) {
            return 0;
        }
        if (fromZone.getId().equals(toZone.getId())) {
            return 0;
        }
        // Calculate based on tier difference
        int fromTier = fromZone.getZoneTier() != null ? fromZone.getZoneTier() : 1;
        int toTier = toZone.getZoneTier() != null ? toZone.getZoneTier() : 1;
        return Math.abs(toTier - fromTier) + 1;
    }

    private BigDecimal calculateTimeMultiplier(LocalDateTime dateTime, FareRule fareRule) {
        LocalTime time = dateTime.toLocalTime();
        BigDecimal multiplier = BigDecimal.ONE;

        if (fareRule == null) {
            return multiplier;
        }

        if (isPeakHour(time) && fareRule.getPeakHourMultiplier() != null) {
            multiplier = fareRule.getPeakHourMultiplier();
        } else if (isNightTime(time) && fareRule.getNightMultiplier() != null) {
            multiplier = fareRule.getNightMultiplier();
        }

        if (isWeekend(dateTime) && fareRule.getWeekendMultiplier() != null) {
            multiplier = multiplier.multiply(fareRule.getWeekendMultiplier());
        }

        return multiplier;
    }

    private boolean isPeakHour(LocalTime time) {
        return (time.isAfter(PEAK_MORNING_START) && time.isBefore(PEAK_MORNING_END)) ||
               (time.isAfter(PEAK_EVENING_START) && time.isBefore(PEAK_EVENING_END));
    }

    private boolean isNightTime(LocalTime time) {
        return time.isAfter(NIGHT_START) || time.isBefore(NIGHT_END);
    }

    private boolean isWeekend(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private int estimateDuration(BigDecimal distanceKm) {
        // Assume average speed of 25 km/h in urban transit
        return (int) (distanceKm.doubleValue() / 25 * 60) + 5; // +5 min for stops
    }

    private int calculateGreenScore(VehicleType vehicleType, BigDecimal distanceKm) {
        int baseScore = GREEN_SCORE_BASE;
        switch (vehicleType) {
            case TRAIN:
                baseScore *= 3; // Trains are most eco-friendly
                break;
            case BUS:
                baseScore *= 2;
                break;
            case TAXI:
                baseScore *= 1;
                break;
        }
        return baseScore + (int) distanceKm.doubleValue();
    }

    // Mapping methods
    private FareRuleDTO toDTO(FareRule fareRule) {
        return FareRuleDTO.builder()
                .id(fareRule.getId())
                .ruleName(fareRule.getRuleName())
                .description(fareRule.getDescription())
                .vehicleType(fareRule.getVehicleType())
                .fromZoneId(fareRule.getFromZone() != null ? fareRule.getFromZone().getId() : null)
                .fromZoneName(fareRule.getFromZone() != null ? fareRule.getFromZone().getZoneName() : null)
                .toZoneId(fareRule.getToZone() != null ? fareRule.getToZone().getId() : null)
                .toZoneName(fareRule.getToZone() != null ? fareRule.getToZone().getZoneName() : null)
                .baseFare(fareRule.getBaseFare())
                .perKmRate(fareRule.getPerKmRate())
                .perZoneRate(fareRule.getPerZoneRate())
                .minimumFare(fareRule.getMinimumFare())
                .maximumFare(fareRule.getMaximumFare())
                .peakHourMultiplier(fareRule.getPeakHourMultiplier())
                .nightMultiplier(fareRule.getNightMultiplier())
                .weekendMultiplier(fareRule.getWeekendMultiplier())
                .studentDiscountPercent(fareRule.getStudentDiscountPercent())
                .seniorDiscountPercent(fareRule.getSeniorDiscountPercent())
                .isActive(fareRule.getIsActive())
                .effectiveFrom(fareRule.getEffectiveFrom())
                .effectiveTo(fareRule.getEffectiveTo())
                .build();
    }

    private FareRule toEntity(FareRuleDTO dto) {
        FareRule fareRule = new FareRule();
        fareRule.setRuleName(dto.getRuleName());
        fareRule.setDescription(dto.getDescription());
        fareRule.setVehicleType(dto.getVehicleType());
        fareRule.setBaseFare(dto.getBaseFare());
        fareRule.setPerKmRate(dto.getPerKmRate());
        fareRule.setPerZoneRate(dto.getPerZoneRate());
        fareRule.setMinimumFare(dto.getMinimumFare());
        fareRule.setMaximumFare(dto.getMaximumFare());
        fareRule.setPeakHourMultiplier(dto.getPeakHourMultiplier());
        fareRule.setNightMultiplier(dto.getNightMultiplier());
        fareRule.setWeekendMultiplier(dto.getWeekendMultiplier());
        fareRule.setStudentDiscountPercent(dto.getStudentDiscountPercent());
        fareRule.setSeniorDiscountPercent(dto.getSeniorDiscountPercent());
        fareRule.setIsActive(dto.getIsActive());
        fareRule.setEffectiveFrom(dto.getEffectiveFrom());
        fareRule.setEffectiveTo(dto.getEffectiveTo());

        if (dto.getFromZoneId() != null) {
            zoneRepository.findById(dto.getFromZoneId())
                    .ifPresent(fareRule::setFromZone);
        }
        if (dto.getToZoneId() != null) {
            zoneRepository.findById(dto.getToZoneId())
                    .ifPresent(fareRule::setToZone);
        }

        return fareRule;
    }

    private void updateEntityFromDTO(FareRule fareRule, FareRuleDTO dto) {
        if (dto.getRuleName() != null) fareRule.setRuleName(dto.getRuleName());
        if (dto.getDescription() != null) fareRule.setDescription(dto.getDescription());
        if (dto.getVehicleType() != null) fareRule.setVehicleType(dto.getVehicleType());
        if (dto.getBaseFare() != null) fareRule.setBaseFare(dto.getBaseFare());
        if (dto.getPerKmRate() != null) fareRule.setPerKmRate(dto.getPerKmRate());
        if (dto.getPerZoneRate() != null) fareRule.setPerZoneRate(dto.getPerZoneRate());
        if (dto.getMinimumFare() != null) fareRule.setMinimumFare(dto.getMinimumFare());
        if (dto.getMaximumFare() != null) fareRule.setMaximumFare(dto.getMaximumFare());
        if (dto.getPeakHourMultiplier() != null) fareRule.setPeakHourMultiplier(dto.getPeakHourMultiplier());
        if (dto.getNightMultiplier() != null) fareRule.setNightMultiplier(dto.getNightMultiplier());
        if (dto.getWeekendMultiplier() != null) fareRule.setWeekendMultiplier(dto.getWeekendMultiplier());
        if (dto.getStudentDiscountPercent() != null) fareRule.setStudentDiscountPercent(dto.getStudentDiscountPercent());
        if (dto.getSeniorDiscountPercent() != null) fareRule.setSeniorDiscountPercent(dto.getSeniorDiscountPercent());
        if (dto.getIsActive() != null) fareRule.setIsActive(dto.getIsActive());
        if (dto.getEffectiveFrom() != null) fareRule.setEffectiveFrom(dto.getEffectiveFrom());
        if (dto.getEffectiveTo() != null) fareRule.setEffectiveTo(dto.getEffectiveTo());

        if (dto.getFromZoneId() != null) {
            zoneRepository.findById(dto.getFromZoneId())
                    .ifPresent(fareRule::setFromZone);
        }
        if (dto.getToZoneId() != null) {
            zoneRepository.findById(dto.getToZoneId())
                    .ifPresent(fareRule::setToZone);
        }
    }
}
