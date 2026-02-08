package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.RewardTransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardTransactionDTO {
    
    private Long id;
    private Long userId;
    private RewardTransactionType transactionType;
    private Integer pointsAmount;
    private Integer greenScoreAmount;
    private String description;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime transactionDate;
    private LocalDateTime expiryDate;
    private Boolean isExpired;
    private LocalDateTime createdAt;
}
