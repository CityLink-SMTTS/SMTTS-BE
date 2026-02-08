package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.RewardTransaction;
import com.nullsquad.CityLink.entity.RewardTransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    
    List<RewardTransaction> findByUserId(Long userId);
    
    List<RewardTransaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);
    
    List<RewardTransaction> findByUserIdAndTransactionType(Long userId, RewardTransactionType transactionType);
    
    @Query("SELECT rt FROM RewardTransaction rt WHERE rt.userId = :userId AND rt.transactionDate BETWEEN :startDate AND :endDate")
    List<RewardTransaction> findByUserAndDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(rt.pointsAmount) FROM RewardTransaction rt WHERE rt.userId = :userId AND rt.transactionType = 'EARNED'")
    Integer sumEarnedPointsByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(rt.pointsAmount) FROM RewardTransaction rt WHERE rt.userId = :userId AND rt.transactionType = 'REDEEMED'")
    Integer sumRedeemedPointsByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(rt.greenScoreAmount) FROM RewardTransaction rt WHERE rt.userId = :userId")
    Integer sumGreenScoreByUser(@Param("userId") Long userId);
    
    @Query("SELECT rt FROM RewardTransaction rt WHERE rt.expiryDate <= :date AND rt.isExpired = false")
    List<RewardTransaction> findExpiringTransactions(@Param("date") LocalDateTime date);
    
    @Query("SELECT rt.referenceType, SUM(rt.pointsAmount) FROM RewardTransaction rt WHERE rt.userId = :userId AND rt.transactionType = 'EARNED' GROUP BY rt.referenceType")
    List<Object[]> getPointsBreakdownBySource(@Param("userId") Long userId);
}
