package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.PaymentMethod;
import com.nullsquad.CityLink.entity.Transaction;
import com.nullsquad.CityLink.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionRef(String transactionRef);
    
    List<Transaction> findByUserId(Long userId);
    
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByPaymentMethod(PaymentMethod paymentMethod);
    
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactionsByUser(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndDateRange(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.isOfflineTransaction = true AND t.status = 'PENDING_SYNC'")
    List<Transaction> findPendingSyncTransactions();
    
    @Query("SELECT t FROM Transaction t WHERE t.deviceId = :deviceId AND t.isOfflineTransaction = true")
    List<Transaction> findOfflineTransactionsByDevice(@Param("deviceId") String deviceId);
    
    @Query("SELECT SUM(t.totalFare) FROM Transaction t WHERE t.userId = :userId AND t.status = 'COMPLETED' AND t.transactionDate >= :since")
    BigDecimal sumTotalFareByUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = 'COMPLETED'")
    Long countCompletedTransactionsByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(t.giftPointsEarned) FROM Transaction t WHERE t.userId = :userId AND t.status = 'COMPLETED'")
    Integer sumGiftPointsByUser(@Param("userId") Long userId);
    
    @Query("SELECT SUM(t.greenScoreEarned) FROM Transaction t WHERE t.userId = :userId AND t.status = 'COMPLETED'")
    Integer sumGreenScoreByUser(@Param("userId") Long userId);
    
    @Query("SELECT t.route.id, COUNT(t) as count FROM Transaction t WHERE t.transactionDate >= :since GROUP BY t.route.id ORDER BY count DESC")
    List<Object[]> findMostUsedRoutes(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT t.vehicle.vehicleType, SUM(t.totalFare) FROM Transaction t WHERE t.transactionDate >= :since AND t.status = 'COMPLETED' GROUP BY t.vehicle.vehicleType")
    List<Object[]> sumRevenueByVehicleType(@Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(t.transactionDate), SUM(t.totalFare) FROM Transaction t WHERE t.transactionDate >= :since AND t.status = 'COMPLETED' GROUP BY DATE(t.transactionDate) ORDER BY DATE(t.transactionDate)")
    List<Object[]> getDailyRevenue(@Param("since") LocalDateTime since);
    
    boolean existsByTransactionRef(String transactionRef);
}
