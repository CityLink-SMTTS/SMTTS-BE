package com.nullsquad.CityLink.entity;

public enum RewardTransactionType {
    EARNED,      // Points earned from trips, reviews, etc.
    REDEEMED,    // Points spent on rewards
    EXPIRED,     // Points expired
    BONUS,       // Bonus points (promotions, referrals)
    ADJUSTED     // Manual adjustment by admin
}
