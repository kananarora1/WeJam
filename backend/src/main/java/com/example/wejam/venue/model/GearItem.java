package com.example.wejam.venue.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** One line of a space's gear list, e.g. "Drum kit" / "5-pc". Descriptive only. */
@Embeddable
public record GearItem(
        @Column(name = "name", nullable = false) String name,
        @Column(name = "details") String details) {
}
