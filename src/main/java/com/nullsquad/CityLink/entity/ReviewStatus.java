package com.nullsquad.CityLink.entity;

public enum ReviewStatus {
    PENDING,     // Awaiting moderation
    APPROVED,    // Published
    REJECTED,    // Not published
    FLAGGED,     // Reported by users
    HIDDEN       // Hidden by admin
}
