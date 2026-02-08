package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.ReviewStatus;
import com.nullsquad.CityLink.entity.ReviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    
    private Long id;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String userName;
    
    private Long vehicleId;
    private String vehicleNumber;
    
    private Long routeId;
    private String routeName;
    
    private Long transactionId;
    private String transactionRef;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
    
    @NotNull(message = "Review type is required")
    private ReviewType reviewType;
    
    private String title;
    private String comment;
    
    @Min(value = 1) @Max(value = 5)
    private Integer cleanlinessRating;
    
    @Min(value = 1) @Max(value = 5)
    private Integer punctualityRating;
    
    @Min(value = 1) @Max(value = 5)
    private Integer driverBehaviorRating;
    
    @Min(value = 1) @Max(value = 5)
    private Integer comfortRating;
    
    @Min(value = 1) @Max(value = 5)
    private Integer safetyRating;
    
    private Boolean isAnonymous;
    private ReviewStatus status;
    
    private String adminResponse;
    private LocalDateTime adminResponseAt;
    
    private Integer helpfulCount;
    private Integer reportCount;
    private Integer giftPointsAwarded;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
