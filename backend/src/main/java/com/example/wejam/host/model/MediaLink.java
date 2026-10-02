package com.example.wejam.host.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** A performance video or other link. URL only — nothing is fetched or embedded. */
@Embeddable
public record MediaLink(
        @Column(name = "url", nullable = false) String url,
        @Column(name = "title") String title) {
}
