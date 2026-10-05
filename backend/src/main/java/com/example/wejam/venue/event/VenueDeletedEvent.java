package com.example.wejam.venue.event;

import java.util.UUID;

/**
 * Published inside the deleting transaction, so listeners (e.g. verification documents) clean up their own
 * data without the venue module depending on them.
 */
public record VenueDeletedEvent(UUID venueId) {
}
