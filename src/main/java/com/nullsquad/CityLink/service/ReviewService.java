package com.nullsquad.CityLink.service;

import com.nullsquad.CityLink.dto.ReviewDTO;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final TransactionRepository transactionRepository;
    private final RewardService rewardService;

    // Points awarded for submitting a review
    private static final int REVIEW_POINTS = 20;
    private static final int REVIEW_WITH_COMMENT_BONUS = 10;

    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return toDTO(review);
    }

    public List<ReviewDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ReviewDTO> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::toDTO);
    }

    public List<ReviewDTO> getReviewsByVehicle(Long vehicleId) {
        return reviewRepository.findApprovedReviewsByVehicle(vehicleId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewDTO> getReviewsByRoute(Long routeId) {
        return reviewRepository.findApprovedReviewsByRoute(routeId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO dto) {
        // Rate limiting: max 3 reviews per day per user
        Long reviewCountToday = reviewRepository.countReviewsByUserSince(
                dto.getUserId(), LocalDateTime.now().minusDays(1));
        if (reviewCountToday >= 3) {
            throw new IllegalStateException("Maximum 3 reviews allowed per day");
        }

        Review review = toEntity(dto);
        review = reviewRepository.save(review);

        // Award points for review
        int points = REVIEW_POINTS;
        if (dto.getComment() != null && !dto.getComment().trim().isEmpty()) {
            points += REVIEW_WITH_COMMENT_BONUS;
        }
        
        rewardService.earnPoints(dto.getUserId(), points, "REVIEW", review.getId(),
                "Review submitted for " + dto.getReviewType());
        review.setGiftPointsAwarded(points);
        review = reviewRepository.save(review);

        log.info("Created review {} by user {}", review.getId(), dto.getUserId());
        return toDTO(review);
    }

    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO dto) {
        Review existing = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Only allow updates if not yet approved
        if (existing.getStatus() == ReviewStatus.APPROVED) {
            throw new IllegalStateException("Cannot update approved review");
        }

        updateEntityFromDTO(existing, dto);
        existing = reviewRepository.save(existing);
        log.info("Updated review: {}", id);
        return toDTO(existing);
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
        log.info("Deleted review: {}", id);
    }

    // Admin moderation endpoints
    public List<ReviewDTO> getPendingReviews(int limit) {
        return reviewRepository.findPendingReviews(PageRequest.of(0, limit)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ReviewDTO> getReviewsByStatus(ReviewStatus status, Pageable pageable) {
        return reviewRepository.findByStatus(status, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public ReviewDTO approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setStatus(ReviewStatus.APPROVED);
        review = reviewRepository.save(review);
        log.info("Approved review: {}", id);
        return toDTO(review);
    }

    @Transactional
    public ReviewDTO rejectReview(Long id, String reason) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setStatus(ReviewStatus.REJECTED);
        review.setAdminResponse(reason);
        review.setAdminResponseAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        log.info("Rejected review: {}", id);
        return toDTO(review);
    }

    @Transactional
    public ReviewDTO respondToReview(Long id, String response) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setAdminResponse(response);
        review.setAdminResponseAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        log.info("Responded to review: {}", id);
        return toDTO(review);
    }

    @Transactional
    public void markReviewHelpful(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Transactional
    public void reportReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setReportCount(review.getReportCount() + 1);
        
        // Auto-flag if report count exceeds threshold
        if (review.getReportCount() >= 5) {
            review.setStatus(ReviewStatus.FLAGGED);
        }
        reviewRepository.save(review);
        log.info("Reported review: {}", id);
    }

    public List<ReviewDTO> getReportedReviews() {
        return reviewRepository.findReportedReviews(3).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Rating statistics
    public Map<String, Object> getVehicleRatingStats(Long vehicleId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewRepository.getAverageRatingByVehicle(vehicleId));
        stats.put("averageCleanlinessRating", reviewRepository.getAverageCleanlinessRating(vehicleId));
        stats.put("totalReviews", reviewRepository.countApprovedReviewsByVehicle(vehicleId));
        stats.put("ratingDistribution", getRatingDistribution(vehicleId, true));
        return stats;
    }

    public Map<String, Object> getRouteRatingStats(Long routeId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewRepository.getAverageRatingByRoute(routeId));
        stats.put("averagePunctualityRating", reviewRepository.getAveragePunctualityRating(routeId));
        stats.put("totalReviews", reviewRepository.countApprovedReviewsByRoute(routeId));
        stats.put("ratingDistribution", getRatingDistribution(routeId, false));
        return stats;
    }

    private Map<Integer, Long> getRatingDistribution(Long id, boolean isVehicle) {
        List<Object[]> distribution = isVehicle ? 
                reviewRepository.getRatingDistributionByVehicle(id) :
                reviewRepository.getRatingDistributionByRoute(id);
        
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            result.put(i, 0L);
        }
        for (Object[] row : distribution) {
            result.put((Integer) row[0], (Long) row[1]);
        }
        return result;
    }

    // Mapping methods
    private ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .userName(review.getIsAnonymous() ? "Anonymous" : review.getUserName())
                .vehicleId(review.getVehicle() != null ? review.getVehicle().getId() : null)
                .vehicleNumber(review.getVehicle() != null ? review.getVehicle().getVehicleNumber() : null)
                .routeId(review.getRoute() != null ? review.getRoute().getId() : null)
                .routeName(review.getRoute() != null ? review.getRoute().getRouteName() : null)
                .transactionId(review.getTransaction() != null ? review.getTransaction().getId() : null)
                .rating(review.getRating())
                .reviewType(review.getReviewType())
                .title(review.getTitle())
                .comment(review.getComment())
                .cleanlinessRating(review.getCleanlinessRating())
                .punctualityRating(review.getPunctualityRating())
                .driverBehaviorRating(review.getDriverBehaviorRating())
                .comfortRating(review.getComfortRating())
                .safetyRating(review.getSafetyRating())
                .isAnonymous(review.getIsAnonymous())
                .status(review.getStatus())
                .adminResponse(review.getAdminResponse())
                .adminResponseAt(review.getAdminResponseAt())
                .helpfulCount(review.getHelpfulCount())
                .reportCount(review.getReportCount())
                .giftPointsAwarded(review.getGiftPointsAwarded())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private Review toEntity(ReviewDTO dto) {
        Review review = Review.builder()
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .rating(dto.getRating())
                .reviewType(dto.getReviewType())
                .title(dto.getTitle())
                .comment(dto.getComment())
                .cleanlinessRating(dto.getCleanlinessRating())
                .punctualityRating(dto.getPunctualityRating())
                .driverBehaviorRating(dto.getDriverBehaviorRating())
                .comfortRating(dto.getComfortRating())
                .safetyRating(dto.getSafetyRating())
                .isAnonymous(dto.getIsAnonymous())
                .status(ReviewStatus.PENDING)
                .build();

        if (dto.getVehicleId() != null) {
            vehicleRepository.findById(dto.getVehicleId())
                    .ifPresent(review::setVehicle);
        }
        if (dto.getRouteId() != null) {
            routeRepository.findById(dto.getRouteId())
                    .ifPresent(review::setRoute);
        }
        if (dto.getTransactionId() != null) {
            transactionRepository.findById(dto.getTransactionId())
                    .ifPresent(review::setTransaction);
        }

        return review;
    }

    private void updateEntityFromDTO(Review review, ReviewDTO dto) {
        if (dto.getRating() != null) review.setRating(dto.getRating());
        if (dto.getTitle() != null) review.setTitle(dto.getTitle());
        if (dto.getComment() != null) review.setComment(dto.getComment());
        if (dto.getCleanlinessRating() != null) review.setCleanlinessRating(dto.getCleanlinessRating());
        if (dto.getPunctualityRating() != null) review.setPunctualityRating(dto.getPunctualityRating());
        if (dto.getDriverBehaviorRating() != null) review.setDriverBehaviorRating(dto.getDriverBehaviorRating());
        if (dto.getComfortRating() != null) review.setComfortRating(dto.getComfortRating());
        if (dto.getSafetyRating() != null) review.setSafetyRating(dto.getSafetyRating());
        if (dto.getIsAnonymous() != null) review.setIsAnonymous(dto.getIsAnonymous());
    }
}
