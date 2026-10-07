package com.example.wejam.venue.job;

import com.example.wejam.IntegrationTest;
import com.example.wejam.common.storage.Bucket;
import com.example.wejam.common.storage.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.randomUid;
import static com.example.wejam.TestUploads.JPEG;
import static com.example.wejam.TestUploads.put;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

/** Scheduling is off in tests; we call the job ourselves and age rows with SQL. */
@IntegrationTest
class VenuePhotoCleanupJobTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectStorage storage;

    @Autowired
    VenuePhotoCleanupJob job;

    @Test
    void sweepsPhotoUploadsThatWereNeverConfirmedAndKeepsTheRest() throws Exception {
        String owner = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
        String venueId = createVenue(owner);
        MvcTestResult abandoned = startUpload(owner, venueId);
        assertThat(put(abandoned, JPEG, "image/jpeg")).isEqualTo(200); // file landed, app never confirmed
        MvcTestResult fresh = startUpload(owner, venueId);
        MvcTestResult kept = startUpload(owner, venueId);
        assertThat(put(kept, JPEG, "image/jpeg")).isEqualTo(200);
        assertThat(mvc.post().uri("/api/v1/venues/{v}/photos/{p}/confirm", venueId, (String) read(body(kept), "$.photoId"))
                .header(HttpHeaders.AUTHORIZATION, bearer(owner))).hasStatusOk();

        UUID abandonedId = UUID.fromString(read(body(abandoned), "$.photoId"));
        String abandonedKey = keyOf(abandonedId);
        jdbc.update("UPDATE venue_photos SET created_at = now() - interval '25 hours' WHERE id IN (?, ?)",
                abandonedId, UUID.fromString(read(body(kept), "$.photoId"))); // old, but confirmed: must stay
        job.run();

        assertThat(exists(abandonedId)).isFalse();
        assertThat(storage.head(Bucket.PUBLIC, abandonedKey)).isEmpty();
        assertThat(exists(UUID.fromString(read(body(fresh), "$.photoId")))).isTrue();
        assertThat(exists(UUID.fromString(read(body(kept), "$.photoId")))).isTrue();
    }

    private MvcTestResult startUpload(String token, String venueId) {
        return mvc.post().uri("/api/v1/venues/{id}/photos", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"contentType\":\"image/jpeg\",\"sizeBytes\":%d}".formatted(JPEG.length))
                .exchange();
    }

    private String keyOf(UUID photoId) {
        return jdbc.queryForObject("SELECT storage_key FROM venue_photos WHERE id = ?", String.class, photoId);
    }

    private boolean exists(UUID photoId) {
        return jdbc.queryForObject("SELECT count(*) FROM venue_photos WHERE id = ?", Integer.class, photoId) == 1;
    }

    private String createVenue(String token) {
        String fssai = "129" + String.format("%011d", ThreadLocalRandom.current().nextLong(100_000_000_000L));
        MvcTestResult result = mvc.post().uri("/api/v1/venues")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Amber Room","addressLine":"12th Main","city":"Bengaluru","latitude":12.97,
                         "longitude":77.64,"fssaiNumber":"%s","fssaiLicenseName":"Amber Hospitality LLP",
                         "fssaiValidUntil":"2030-03-31","hostingMode":"OPEN"}
                        """.formatted(fssai))
                .exchange();
        assertThat(result).hasStatus(201);
        return read(body(result), "$.id");
    }
}
