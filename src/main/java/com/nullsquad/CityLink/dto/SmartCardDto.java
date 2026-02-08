package com.nullsquad.CityLink.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmartCardDto {
    private Long cardId;
    private String cardUid;
    private BigDecimal balance;
    private String status;
    private LocalDateTime issuedAt;
}
