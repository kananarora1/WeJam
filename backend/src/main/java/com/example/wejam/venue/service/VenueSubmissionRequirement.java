package com.example.wejam.venue.service;

import java.util.Map;
import java.util.UUID;

/**
 * Something another module needs before a venue can be submitted for review — e.g. the FSSAI certificate, which
 * lives in the verification module. Those modules depend on this one, so instead of the venue module calling
 * them, they plug in here (dependency inversion); Spring hands VenueService every implementation.
 */
public interface VenueSubmissionRequirement {

    /** Field → what's missing (same shape as validation errors); empty when satisfied. */
    Map<String, String> missing(UUID venueId);
}
