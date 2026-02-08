package com.nullsquad.CityLink.repository;

import com.nullsquad.CityLink.entity.Review;
import com.nullsquad.CityLink.entity.ReviewStatus;
import com.nullsquad.CityLink.entity.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByUserId(Long userId);
    
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    List<Review> findByStatus(ReviewStatus status);
    
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);
    
    List<Review> findByReviewType(ReviewType reviewType);
    
    @Query("SELECT r FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT r FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByRoute(@Param("routeId") Long routeId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED'")
    Double getAverageRatingByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED'")
    Double getAverageRatingByRoute(@Param("routeId") Long routeId);
    
    @Query("SELECT AVG(r.cleanlinessRating) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' AND r.cleanlinessRating IS NOT NULL")
    Double getAverageCleanlinessRating(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT AVG(r.punctualityRating) FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED' AND r.punctualityRating IS NOT NULL")
    Double getAveragePunctualityRating(@Param("routeId") Long routeId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED'")
    Long countApprovedReviewsByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED'")
    Long countApprovedReviewsByRoute(@Param("routeId") Long routeId);
    
    @Query("SELECT r FROM Review r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Review> findPendingReviews(Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.reportCount >= :threshold AND r.status = 'APPROVED'")
    List<Review> findReportedReviews(@Param("threshold") int threshold);
    
    @Query("SELECT r FROM Review r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(@Param("since") LocalDateTime since, Pageable pageable);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistributionByVehicle(@Param("vehicleId") Long vehicleId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.route.id = :routeId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistributionByRoute(@Param("routeId") Long routeId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.userId = :userId AND r.createdAt >= :since")
    Long countReviewsByUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
