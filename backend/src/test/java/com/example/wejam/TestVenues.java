package com.example.wejam;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/** Shortcuts for tests that need a venue in a later state than the one they're testing. */
public final class TestVenues {

    private TestVenues() {
    }

    /**
     * Everything submit needs beyond the license fields, recorded directly in the database: 3 photos counted and an
     * FSSAI certificate on file (no files uploaded). VenueDraftControllerTest does the same with real uploads.
     */
    public static void markReadyToSubmit(JdbcTemplate jdbc, String venueId) {
        UUID id = UUID.fromString(venueId);
        jdbc.update("UPDATE venues SET photo_count = 3 WHERE id = ?", id);
        jdbc.update("""
                INSERT INTO verification_documents (id, owner_type, owner_id, doc_type, storage_key, content_type,
                                                    size_bytes, status, uploaded_at)
                VALUES (gen_random_uuid(), 'VENUE', ?, 'FSSAI_CERTIFICATE', 'test-only/' || gen_random_uuid(),
                        'application/pdf', 1, 'UPLOADED', now())
                ON CONFLICT DO NOTHING
                """, id);
    }
}
