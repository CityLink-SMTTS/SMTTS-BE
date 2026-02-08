package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.*;
import com.nullsquad.CityLink.entity.*;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final ZoneRepository zoneRepository;
    private final FareService fareService;

    // Points configuration
    private static final int BASE_GIFT_POINTS = 10;
    private static final int BASE_GREEN_SCORE = 5;

    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return toDTO(transaction);
    }

    public TransactionDTO getTransactionByRef(String transactionRef) {
        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ref: " + transactionRef));
        return toDTO(transaction);
    }

    public List<TransactionDTO> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<TransactionDTO> getTransactionsByUser(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable)
                .map(this::toDTO);
    }

    public List<TransactionDTO> getRecentTransactions(Long userId, int limit) {
        return transactionRepository.findRecentTransactionsByUser(userId, PageRequest.of(0, limit)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByDateRange(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new transaction with fare calculation
     */
    @Transactional
    public TransactionDTO createTransaction(TransactionDTO dto) {
        // Generate transaction reference
        String transactionRef = generateTransactionRef();
        
        // Build transaction entity
        Transaction transaction = Transaction.builder()
                .transactionRef(transactionRef)
                .userId(dto.getUserId())
                .baseFare(dto.getBaseFare())
                .distanceFare(dto.getDistanceFare())
                .zoneFare(dto.getZoneFare())
                .surgeMultiplier(dto.getSurgeMultiplier())
                .discountAmount(dto.getDiscountAmount())
                .discountReason(dto.getDiscountReason())
                .totalFare(dto.getTotalFare())
                .paymentMethod(dto.getPaymentMethod())
                .status(TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .isOfflineTransaction(false)
                .distanceKm(dto.getDistanceKm())
                .zonesCrossed(dto.getZonesCrossed())
                .build();

        // Set relationships
        if (dto.getVehicleId() != null) {
            vehicleRepository.findById(dto.getVehicleId())
                    .ifPresent(transaction::setVehicle);
        }
        if (dto.getRouteId() != null) {
            routeRepository.findById(dto.getRouteId())
                    .ifPresent(transaction::setRoute);
        }
        if (dto.getFromStopId() != null) {
            routeStopRepository.findById(dto.getFromStopId())
                    .ifPresent(transaction::setFromStop);
        }
        if (dto.getToStopId() != null) {
            routeStopRepository.findById(dto.getToStopId())
                    .ifPresent(transaction::setToStop);
        }
        if (dto.getFromZoneId() != null) {
            zoneRepository.findById(dto.getFromZoneId())
                    .ifPresent(transaction::setFromZone);
        }
        if (dto.getToZoneId() != null) {
            zoneRepository.findById(dto.getToZoneId())
                    .ifPresent(transaction::setToZone);
        }

        // Calculate reward points
        transaction.setGiftPointsEarned(calculateGiftPoints(dto.getTotalFare()));
        transaction.setGreenScoreEarned(calculateGreenScore(transaction.getVehicle()));

        transaction = transactionRepository.save(transaction);
        log.info("Created transaction: {}", transactionRef);
        return toDTO(transaction);
    }

    /**
     * Complete a pending transaction
     */
    @Transactional
    public TransactionDTO completeTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in PENDING status");
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        
        log.info("Completed transaction: {}", transaction.getTransactionRef());
        return toDTO(transaction);
    }

    /**
     * Cancel a transaction
     */
    @Transactional
    public TransactionDTO cancelTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed transaction");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transaction = transactionRepository.save(transaction);
        
        log.info("Cancelled transaction: {}", transaction.getTransactionRef());
        return toDTO(transaction);
    }

    /**
     * Refund a completed transaction
     */
    @Transactional
    public TransactionDTO refundTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Only completed transactions can be refunded");
        }

        transaction.setStatus(TransactionStatus.REFUNDED);
        // Reset earned points
        transaction.setGiftPointsEarned(0);
        transaction.setGreenScoreEarned(0);
        transaction = transactionRepository.save(transaction);
        
        log.info("Refunded transaction: {}", transaction.getTransactionRef());
        return toDTO(transaction);
    }

    /**
     * Sync offline transactions
     */
    @Transactional
    public Map<String, Object> syncOfflineTransactions(OfflineTransactionSyncDTO syncDTO) {
        List<String> successfulSyncs = new ArrayList<>();
        List<String> failedSyncs = new ArrayList<>();

        for (OfflineTransactionSyncDTO.OfflineTransactionData data : syncDTO.getTransactions()) {
            try {
                Transaction transaction = Transaction.builder()
                        .transactionRef(generateTransactionRef())
                        .userId(data.getUserId())
                        .totalFare(data.getTotalFare())
                        .paymentMethod(data.getPaymentMethod())
                        .status(TransactionStatus.COMPLETED)
                        .isOfflineTransaction(true)
                        .deviceId(syncDTO.getDeviceId())
                        .transactionDate(data.getTransactionDate())
                        .syncedAt(LocalDateTime.now())
                        .completedAt(data.getTransactionDate())
                        .build();

                // Set relationships
                if (data.getVehicleId() != null) {
                    vehicleRepository.findById(data.getVehicleId())
                            .ifPresent(transaction::setVehicle);
                }
                if (data.getRouteId() != null) {
                    routeRepository.findById(data.getRouteId())
                            .ifPresent(transaction::setRoute);
                }
                if (data.getFromStopId() != null) {
                    routeStopRepository.findById(data.getFromStopId())
                            .ifPresent(transaction::setFromStop);
                }
                if (data.getToStopId() != null) {
                    routeStopRepository.findById(data.getToStopId())
                            .ifPresent(transaction::setToStop);
                }

                // Calculate points
                transaction.setGiftPointsEarned(calculateGiftPoints(data.getTotalFare()));
                transaction.setGreenScoreEarned(calculateGreenScore(transaction.getVehicle()));

                transactionRepository.save(transaction);
                successfulSyncs.add(data.getLocalTransactionId());
                
            } catch (Exception e) {
                log.error("Failed to sync offline transaction: {}", data.getLocalTransactionId(), e);
                failedSyncs.add(data.getLocalTransactionId());
            }
        }

        log.info("Synced {} offline transactions from device {}", successfulSyncs.size(), syncDTO.getDeviceId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("successful", successfulSyncs);
        result.put("failed", failedSyncs);
        result.put("totalProcessed", syncDTO.getTransactions().size());
        return result;
    }

    /**
     * Get pending sync transactions
     */
    public List<TransactionDTO> getPendingSyncTransactions() {
        return transactionRepository.findPendingSyncTransactions().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Analytics methods
    public Map<String, Object> getUserTransactionStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalTransactions", transactionRepository.countCompletedTransactionsByUser(userId));
        stats.put("totalSpent", transactionRepository.sumTotalFareByUserSince(userId, 
                LocalDateTime.now().minusMonths(12)));
        stats.put("totalGiftPoints", transactionRepository.sumGiftPointsByUser(userId));
        stats.put("totalGreenScore", transactionRepository.sumGreenScoreByUser(userId));
        
        return stats;
    }

    public List<Map<String, Object>> getMostUsedRoutes(int limit) {
        List<Object[]> results = transactionRepository.findMostUsedRoutes(
                LocalDateTime.now().minusMonths(1), PageRequest.of(0, limit));
        
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("routeId", row[0]);
            map.put("tripCount", row[1]);
            return map;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRevenueByVehicleType() {
        List<Object[]> results = transactionRepository.sumRevenueByVehicleType(
                LocalDateTime.now().minusMonths(1));
        
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("vehicleType", row[0]);
            map.put("revenue", row[1]);
            return map;
        }).collect(Collectors.toList());
    }

    // Helper methods
    private String generateTransactionRef() {
        return "TXN" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private int calculateGiftPoints(BigDecimal totalFare) {
        return BASE_GIFT_POINTS + (int) (totalFare.doubleValue() / 10);
    }

    private int calculateGreenScore(Vehicle vehicle) {
        if (vehicle == null) {
            return BASE_GREEN_SCORE;
        }
        
        int score = BASE_GREEN_SCORE;
        switch (vehicle.getVehicleType()) {
            case TRAIN:
                score *= 3;
                break;
            case BUS:
                score *= 2;
                break;
            case TAXI:
                score *= 1;
                break;
        }
        
        if (Boolean.TRUE.equals(vehicle.getIsEcoFriendly())) {
            score += 5;
        }
        
        return score;
    }

    // Mapping methods
    private TransactionDTO toDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .transactionRef(transaction.getTransactionRef())
                .userId(transaction.getUserId())
                .vehicleId(transaction.getVehicle() != null ? transaction.getVehicle().getId() : null)
                .vehicleNumber(transaction.getVehicle() != null ? transaction.getVehicle().getVehicleNumber() : null)
                .routeId(transaction.getRoute() != null ? transaction.getRoute().getId() : null)
                .routeName(transaction.getRoute() != null ? transaction.getRoute().getRouteName() : null)
                .fromStopId(transaction.getFromStop() != null ? transaction.getFromStop().getId() : null)
                .fromStopName(transaction.getFromStop() != null ? transaction.getFromStop().getStopName() : null)
                .toStopId(transaction.getToStop() != null ? transaction.getToStop().getId() : null)
                .toStopName(transaction.getToStop() != null ? transaction.getToStop().getStopName() : null)
                .fromZoneId(transaction.getFromZone() != null ? transaction.getFromZone().getId() : null)
                .fromZoneName(transaction.getFromZone() != null ? transaction.getFromZone().getZoneName() : null)
                .toZoneId(transaction.getToZone() != null ? transaction.getToZone().getId() : null)
                .toZoneName(transaction.getToZone() != null ? transaction.getToZone().getZoneName() : null)
                .distanceKm(transaction.getDistanceKm())
                .zonesCrossed(transaction.getZonesCrossed())
                .baseFare(transaction.getBaseFare())
                .distanceFare(transaction.getDistanceFare())
                .zoneFare(transaction.getZoneFare())
                .surgeMultiplier(transaction.getSurgeMultiplier())
                .discountAmount(transaction.getDiscountAmount())
                .discountReason(transaction.getDiscountReason())
                .totalFare(transaction.getTotalFare())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .isOfflineTransaction(transaction.getIsOfflineTransaction())
                .syncedAt(transaction.getSyncedAt())
                .deviceId(transaction.getDeviceId())
                .transactionDate(transaction.getTransactionDate())
                .completedAt(transaction.getCompletedAt())
                .giftPointsEarned(transaction.getGiftPointsEarned())
                .greenScoreEarned(transaction.getGreenScoreEarned())
                .build();
    }
}
