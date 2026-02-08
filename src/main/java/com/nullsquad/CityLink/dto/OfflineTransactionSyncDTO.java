package com.nullsquad.CityLink.dto;

import com.nullsquad.CityLink.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineTransactionSyncDTO {
    
    private List<OfflineTransactionData> transactions;
    private String deviceId;
    private LocalDateTime syncTimestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OfflineTransactionData {
        @NotNull
        private Long userId;
        private Long vehicleId;
        private Long routeId;
        private Long fromStopId;
        private Long toStopId;
        @NotNull
        private BigDecimal totalFare;
        private PaymentMethod paymentMethod;
        private LocalDateTime transactionDate;
        private String localTransactionId;
    }
}
