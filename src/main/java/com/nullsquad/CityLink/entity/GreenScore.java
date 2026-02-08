package com.nullsquad.CityLink.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "green_scores")
public class GreenScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_trips", nullable = false)
    private Integer totalTrips = 0;

    @Column(name = "carbon_saved_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbonSavedKg = BigDecimal.ZERO;

    @Column(name = "green_score", nullable = false)
    private Integer greenScore = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
        if (totalTrips == null) totalTrips = 0;
        if (carbonSavedKg == null) carbonSavedKg = BigDecimal.ZERO;
        if (greenScore == null) greenScore = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
