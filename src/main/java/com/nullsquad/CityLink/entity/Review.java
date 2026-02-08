package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_name")
    private String userName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @ToString.Exclude
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @ToString.Exclude
    private Route route;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @ToString.Exclude
    private Transaction transaction;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;
    
    @Column(name = "punctuality_rating")
    private Integer punctualityRating;
    
    @Column(name = "driver_behavior_rating")
    private Integer driverBehaviorRating;
    
    @Column(name = "comfort_rating")
    private Integer comfortRating;
    
    @Column(name = "safety_rating")
    private Integer safetyRating;
    
    @Column(name = "is_anonymous")
    private Boolean isAnonymous;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReviewStatus status;
    
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;
    
    @Column(name = "admin_response_at")
    private LocalDateTime adminResponseAt;
    
    @Column(name = "helpful_count")
    private Integer helpfulCount;
    
    @Column(name = "report_count")
    private Integer reportCount;
    
    @Column(name = "gift_points_awarded")
    private Integer giftPointsAwarded;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ReviewStatus.PENDING;
        if (isAnonymous == null) isAnonymous = false;
        if (helpfulCount == null) helpfulCount = 0;
        if (reportCount == null) reportCount = 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
