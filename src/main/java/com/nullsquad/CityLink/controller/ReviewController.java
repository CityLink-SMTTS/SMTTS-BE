package com.nullsquad.CityLink.controller;

import com.nullsquad.CityLink.dto.ReviewDTO;
import com.nullsquad.CityLink.entity.ReviewStatus;
import com.nullsquad.CityLink.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByUserPaged(
            @PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId, pageable));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByVehicle(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(reviewService.getReviewsByVehicle(vehicleId));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(reviewService.getReviewsByRoute(routeId));
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewDTO dto) {
        ReviewDTO created = reviewService.createReview(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long id,
                                                   @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<Void> markReviewHelpful(@PathVariable Long id) {
        reviewService.markReviewHelpful(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<Void> reportReview(@PathVariable Long id) {
        reviewService.reportReview(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vehicle/{vehicleId}/stats")
    public ResponseEntity<Map<String, Object>> getVehicleRatingStats(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(reviewService.getVehicleRatingStats(vehicleId));
    }

    @GetMapping("/route/{routeId}/stats")
    public ResponseEntity<Map<String, Object>> getRouteRatingStats(@PathVariable Long routeId) {
        return ResponseEntity.ok(reviewService.getRouteRatingStats(routeId));
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<List<ReviewDTO>> getPendingReviews(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(reviewService.getPendingReviews(limit));
    }

    @GetMapping("/admin/status/{status}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByStatus(
            @PathVariable ReviewStatus status, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByStatus(status, pageable));
    }

    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<ReviewDTO> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approveReview(id));
    }

    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<ReviewDTO> rejectReview(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(reviewService.rejectReview(id, body.get("reason")));
    }

    @PostMapping("/admin/{id}/respond")
    public ResponseEntity<ReviewDTO> respondToReview(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(reviewService.respondToReview(id, body.get("response")));
    }

    @GetMapping("/admin/reported")
    public ResponseEntity<List<ReviewDTO>> getReportedReviews() {
        return ResponseEntity.ok(reviewService.getReportedReviews());
    }
}
