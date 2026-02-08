package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRewardRepository extends JpaRepository<UserReward, Long> {
    
    Optional<UserReward> findByUserId(Long userId);
    
    List<UserReward> findByCurrentTier(String tier);
    
    @Query("SELECT ur FROM UserReward ur ORDER BY ur.totalGreenScore DESC")
    List<UserReward> findTopGreenScoreUsers(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT ur FROM UserReward ur ORDER BY ur.totalGiftPoints DESC")
    List<UserReward> findTopPointsUsers(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT ur FROM UserReward ur ORDER BY ur.ecoTripsCount DESC")
    List<UserReward> findTopEcoTravelers(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COUNT(ur) FROM UserReward ur WHERE ur.totalGreenScore > :score")
    Long countUsersWithHigherGreenScore(@Param("score") BigDecimal score);
    
    @Query("SELECT COUNT(ur) FROM UserReward ur WHERE ur.totalGreenScore IS NOT NULL")
    Long countAllUsersWithGreenScore();
    
    @Query("SELECT AVG(ur.totalGreenScore) FROM UserReward ur")
    Double getAverageGreenScore();
    
    @Query("SELECT SUM(ur.carbonSavedKg) FROM UserReward ur")
    BigDecimal getTotalCarbonSaved();
    
    boolean existsByUserId(Long userId);
}
