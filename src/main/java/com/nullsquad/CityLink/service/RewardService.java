package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.*;
import com.nullsquad.CityLink.entity.*;
import com.nullsquad.CityLink.exception.ResourceNotFoundException;
import com.nullsquad.CityLink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private final UserRewardRepository userRewardRepository;
    private final RewardTransactionRepository rewardTransactionRepository;

    // Tier thresholds
    private static final int SILVER_THRESHOLD = 500;
    private static final int GOLD_THRESHOLD = 2000;
    private static final int PLATINUM_THRESHOLD = 5000;

    // Green score tier thresholds
    private static final int ECO_WARRIOR_THRESHOLD = 100;
    private static final int ECO_CHAMPION_THRESHOLD = 500;
    private static final int ECO_LEGEND_THRESHOLD = 2000;

    // Carbon saved per trip (estimated kg)
    private static final BigDecimal CARBON_PER_BUS_TRIP = new BigDecimal("2.5");
    private static final BigDecimal CARBON_PER_TRAIN_TRIP = new BigDecimal("5.0");
    private static final BigDecimal CARBON_PER_TAXI_TRIP = new BigDecimal("1.0");

    public UserRewardDTO getUserReward(Long userId) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserReward(userId));
        return toDTO(reward);
    }

    @Transactional
    public UserReward createNewUserReward(Long userId) {
        UserReward reward = UserReward.builder()
                .userId(userId)
                .totalGiftPoints(0)
                .availableGiftPoints(0)
                .redeemedPoints(0)
                .totalGreenScore(BigDecimal.ZERO)
                .currentTier("BRONZE")
                .totalTrips(0)
                .ecoTripsCount(0)
                .carbonSavedKg(BigDecimal.ZERO)
                .consecutiveDays(0)
                .build();
        return userRewardRepository.save(reward);
    }

    @Transactional
    public void earnPoints(Long userId, int points, String referenceType, Long referenceId, String description) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserReward(userId));

        reward.setTotalGiftPoints(reward.getTotalGiftPoints() + points);
        reward.setAvailableGiftPoints(reward.getAvailableGiftPoints() + points);
        
        // Update tier
        updateTier(reward);
        
        userRewardRepository.save(reward);

        // Record transaction
        RewardTransaction transaction = RewardTransaction.builder()
                .userId(userId)
                .transactionType(RewardTransactionType.EARNED)
                .pointsAmount(points)
                .description(description)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusMonths(12))
                .build();
        rewardTransactionRepository.save(transaction);

        log.info("User {} earned {} points for {}", userId, points, referenceType);
    }

    @Transactional
    public void addGreenScore(Long userId, int score, VehicleType vehicleType) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserReward(userId));

        reward.setTotalGreenScore(reward.getTotalGreenScore().add(BigDecimal.valueOf(score)));
        reward.setEcoTripsCount(reward.getEcoTripsCount() + 1);
        reward.setTotalTrips(reward.getTotalTrips() + 1);

        // Calculate carbon saved
        BigDecimal carbonSaved = getCarbonSavedByVehicleType(vehicleType);
        reward.setCarbonSavedKg(reward.getCarbonSavedKg().add(carbonSaved));

        // Update consecutive days
        updateConsecutiveDays(reward);

        userRewardRepository.save(reward);

        // Record green score transaction
        RewardTransaction transaction = RewardTransaction.builder()
                .userId(userId)
                .transactionType(RewardTransactionType.EARNED)
                .pointsAmount(0)
                .greenScoreAmount(score)
                .description("Eco trip - " + vehicleType.name())
                .referenceType("TRIP")
                .transactionDate(LocalDateTime.now())
                .build();
        rewardTransactionRepository.save(transaction);

        log.info("User {} earned {} green score for {} trip", userId, score, vehicleType);
    }

    @Transactional
    public UserRewardDTO redeemPoints(Long userId, int points, String description) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User reward not found"));

        if (reward.getAvailableGiftPoints() < points) {
            throw new IllegalStateException("Insufficient points. Available: " + reward.getAvailableGiftPoints());
        }

        reward.setAvailableGiftPoints(reward.getAvailableGiftPoints() - points);
        reward.setRedeemedPoints(reward.getRedeemedPoints() + points);
        userRewardRepository.save(reward);

        // Record redemption
        RewardTransaction transaction = RewardTransaction.builder()
                .userId(userId)
                .transactionType(RewardTransactionType.REDEEMED)
                .pointsAmount(-points)
                .description(description)
                .referenceType("REDEMPTION")
                .transactionDate(LocalDateTime.now())
                .build();
        rewardTransactionRepository.save(transaction);

        log.info("User {} redeemed {} points", userId, points);
        return toDTO(reward);
    }

    @Transactional
    public void awardBonusPoints(Long userId, int points, String reason) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserReward(userId));

        reward.setTotalGiftPoints(reward.getTotalGiftPoints() + points);
        reward.setAvailableGiftPoints(reward.getAvailableGiftPoints() + points);
        updateTier(reward);
        userRewardRepository.save(reward);

        RewardTransaction transaction = RewardTransaction.builder()
                .userId(userId)
                .transactionType(RewardTransactionType.BONUS)
                .pointsAmount(points)
                .description(reason)
                .referenceType("BONUS")
                .transactionDate(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusMonths(6))
                .build();
        rewardTransactionRepository.save(transaction);

        log.info("User {} awarded {} bonus points for: {}", userId, points, reason);
    }

    public List<RewardTransactionDTO> getUserTransactionHistory(Long userId, int limit) {
        return rewardTransactionRepository.findByUserIdOrderByTransactionDateDesc(
                userId, PageRequest.of(0, limit)).stream()
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
    }

    public GreenScoreDTO getGreenScore(Long userId) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseGet(() -> createNewUserReward(userId));

        // Calculate percentile rank
        Long higherCount = userRewardRepository.countUsersWithHigherGreenScore(reward.getTotalGreenScore());
        Long totalUsers = userRewardRepository.countAllUsersWithGreenScore();
        double percentileRank = totalUsers > 0 ? 
                (1 - (higherCount.doubleValue() / totalUsers.doubleValue())) * 100 : 50.0;

        // Calculate trees equivalent (1 tree absorbs ~21 kg CO2/year)
        BigDecimal treesEquivalent = reward.getCarbonSavedKg() != null ?
                reward.getCarbonSavedKg().divide(new BigDecimal("21"), 1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Calculate eco trip percentage
        double ecoTripPercentage = reward.getTotalTrips() > 0 ?
                (reward.getEcoTripsCount().doubleValue() / reward.getTotalTrips().doubleValue()) * 100 : 0;

        return GreenScoreDTO.builder()
                .userId(userId)
                .totalGreenScore(reward.getTotalGreenScore())
                .greenTier(calculateGreenTier(reward.getTotalGreenScore()))
                .ecoTripsCount(reward.getEcoTripsCount())
                .totalTrips(reward.getTotalTrips())
                .ecoTripPercentage(ecoTripPercentage)
                .carbonSavedKg(reward.getCarbonSavedKg())
                .treesEquivalent(treesEquivalent)
                .consecutiveDays(reward.getConsecutiveDays())
                .hasFirstEcoTrip(reward.getEcoTripsCount() >= 1)
                .has10EcoTrips(reward.getEcoTripsCount() >= 10)
                .has50EcoTrips(reward.getEcoTripsCount() >= 50)
                .has100EcoTrips(reward.getEcoTripsCount() >= 100)
                .hasWeekStreak(reward.getConsecutiveDays() >= 7)
                .hasMonthStreak(reward.getConsecutiveDays() >= 30)
                .percentileRank(percentileRank)
                .impactMessage(generateImpactMessage(reward))
                .build();
    }

    // Leaderboards
    public List<UserRewardDTO> getGreenScoreLeaderboard(int limit) {
        return userRewardRepository.findTopGreenScoreUsers(PageRequest.of(0, limit)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserRewardDTO> getPointsLeaderboard(int limit) {
        return userRewardRepository.findTopPointsUsers(PageRequest.of(0, limit)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserRewardDTO> getEcoTravelersLeaderboard(int limit) {
        return userRewardRepository.findTopEcoTravelers(PageRequest.of(0, limit)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Global sustainability stats
    public Map<String, Object> getGlobalSustainabilityStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCarbonSavedKg", userRewardRepository.getTotalCarbonSaved());
        stats.put("averageGreenScore", userRewardRepository.getAverageGreenScore());
        stats.put("totalUsers", userRewardRepository.count());
        return stats;
    }

    // Admin function to adjust points
    @Transactional
    public UserRewardDTO adjustPoints(Long userId, int adjustment, String reason) {
        UserReward reward = userRewardRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User reward not found"));

        reward.setTotalGiftPoints(reward.getTotalGiftPoints() + adjustment);
        reward.setAvailableGiftPoints(reward.getAvailableGiftPoints() + adjustment);
        
        if (reward.getAvailableGiftPoints() < 0) {
            reward.setAvailableGiftPoints(0);
        }
        
        updateTier(reward);
        userRewardRepository.save(reward);

        RewardTransaction transaction = RewardTransaction.builder()
                .userId(userId)
                .transactionType(RewardTransactionType.ADJUSTED)
                .pointsAmount(adjustment)
                .description("Admin adjustment: " + reason)
                .referenceType("ADJUSTMENT")
                .transactionDate(LocalDateTime.now())
                .build();
        rewardTransactionRepository.save(transaction);

        log.info("Admin adjusted {} points for user {}: {}", adjustment, userId, reason);
        return toDTO(reward);
    }

    // Helper methods
    private void updateTier(UserReward reward) {
        int totalPoints = reward.getTotalGiftPoints();
        if (totalPoints >= PLATINUM_THRESHOLD) {
            reward.setCurrentTier("PLATINUM");
        } else if (totalPoints >= GOLD_THRESHOLD) {
            reward.setCurrentTier("GOLD");
        } else if (totalPoints >= SILVER_THRESHOLD) {
            reward.setCurrentTier("SILVER");
        } else {
            reward.setCurrentTier("BRONZE");
        }
    }

    private String calculateGreenTier(BigDecimal greenScore) {
        if (greenScore.intValue() >= ECO_LEGEND_THRESHOLD) return "ECO_LEGEND";
        if (greenScore.intValue() >= ECO_CHAMPION_THRESHOLD) return "ECO_CHAMPION";
        if (greenScore.intValue() >= ECO_WARRIOR_THRESHOLD) return "ECO_WARRIOR";
        return "ECO_STARTER";
    }

    private BigDecimal getCarbonSavedByVehicleType(VehicleType vehicleType) {
        switch (vehicleType) {
            case TRAIN: return CARBON_PER_TRAIN_TRIP;
            case BUS: return CARBON_PER_BUS_TRIP;
            case TAXI: return CARBON_PER_TAXI_TRIP;
            default: return BigDecimal.ZERO;
        }
    }

    private void updateConsecutiveDays(UserReward reward) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastTrip = reward.getLastTripDate();
        
        if (lastTrip != null) {
            long daysBetween = ChronoUnit.DAYS.between(lastTrip.toLocalDate(), now.toLocalDate());
            if (daysBetween == 1) {
                reward.setConsecutiveDays(reward.getConsecutiveDays() + 1);
            } else if (daysBetween > 1) {
                reward.setConsecutiveDays(1);
            }
        } else {
            reward.setConsecutiveDays(1);
        }
        reward.setLastTripDate(now);
    }

    private String generateImpactMessage(UserReward reward) {
        if (reward.getCarbonSavedKg() == null) return "Start your eco journey today!";
        
        BigDecimal kg = reward.getCarbonSavedKg();
        if (kg.compareTo(new BigDecimal("100")) >= 0) {
            return String.format("Amazing! You've saved %.1f kg CO2 - equivalent to planting %d trees!", 
                    kg.doubleValue(), kg.divide(new BigDecimal("21"), 0, RoundingMode.HALF_UP).intValue());
        } else if (kg.compareTo(new BigDecimal("50")) >= 0) {
            return String.format("Great job! You've saved %.1f kg CO2 - keep going!", kg.doubleValue());
        } else if (kg.compareTo(new BigDecimal("10")) >= 0) {
            return String.format("Good progress! You've saved %.1f kg CO2!", kg.doubleValue());
        } else {
            return "Every trip counts! Keep using public transit!";
        }
    }

    private int getNextTierThreshold(String currentTier) {
        switch (currentTier) {
            case "BRONZE": return SILVER_THRESHOLD;
            case "SILVER": return GOLD_THRESHOLD;
            case "GOLD": return PLATINUM_THRESHOLD;
            default: return PLATINUM_THRESHOLD;
        }
    }

    private String getNextTier(String currentTier) {
        switch (currentTier) {
            case "BRONZE": return "SILVER";
            case "SILVER": return "GOLD";
            case "GOLD": return "PLATINUM";
            default: return "PLATINUM";
        }
    }

    // Mapping methods
    private UserRewardDTO toDTO(UserReward reward) {
        int pointsToNext = getNextTierThreshold(reward.getCurrentTier()) - reward.getTotalGiftPoints();
        double progress = (reward.getTotalGiftPoints().doubleValue() / getNextTierThreshold(reward.getCurrentTier())) * 100;
        if (progress > 100) progress = 100;

        return UserRewardDTO.builder()
                .id(reward.getId())
                .userId(reward.getUserId())
                .totalGiftPoints(reward.getTotalGiftPoints())
                .availableGiftPoints(reward.getAvailableGiftPoints())
                .redeemedPoints(reward.getRedeemedPoints())
                .totalGreenScore(reward.getTotalGreenScore())
                .currentTier(reward.getCurrentTier())
                .totalTrips(reward.getTotalTrips())
                .ecoTripsCount(reward.getEcoTripsCount())
                .carbonSavedKg(reward.getCarbonSavedKg())
                .consecutiveDays(reward.getConsecutiveDays())
                .lastTripDate(reward.getLastTripDate())
                .pointsToNextTier(Math.max(0, pointsToNext))
                .nextTier(getNextTier(reward.getCurrentTier()))
                .tierProgress(progress)
                .createdAt(reward.getCreatedAt())
                .updatedAt(reward.getUpdatedAt())
                .build();
    }

    private RewardTransactionDTO toTransactionDTO(RewardTransaction transaction) {
        return RewardTransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .transactionType(transaction.getTransactionType())
                .pointsAmount(transaction.getPointsAmount())
                .greenScoreAmount(transaction.getGreenScoreAmount())
                .description(transaction.getDescription())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .transactionDate(transaction.getTransactionDate())
                .expiryDate(transaction.getExpiryDate())
                .isExpired(transaction.getIsExpired())
                .build();
    }
}
