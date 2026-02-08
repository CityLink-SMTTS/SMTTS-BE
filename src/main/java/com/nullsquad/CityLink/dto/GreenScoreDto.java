package com.nullsquad.CityLink.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GreenScoreDto {
    private Long userId;
    private String fullName;
    private Integer totalTrips;
    private BigDecimal carbonSavedKg;
    private Integer greenScore;
    private LocalDateTime lastUpdated;
}
