package com.example.wejam.venue.model;

/**
 * DRAFT → PENDING when the owner submits; PENDING → VERIFIED | REJECTED by a platform admin; REJECTED → PENDING
 * when the owner resubmits. A venue admin never sets VERIFIED or REJECTED.
 */
public enum VerificationStatus {
    DRAFT,
    PENDING,
    VERIFIED,
    REJECTED
}
