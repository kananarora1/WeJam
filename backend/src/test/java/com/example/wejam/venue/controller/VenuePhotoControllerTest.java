package com.example.wejam.venue.controller;

import com.example.wejam.IntegrationTest;
import com.example.wejam.common.storage.Bucket;
import com.example.wejam.common.storage.ObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.randomUid;
import static com.example.wejam.TestUploads.JPEG;
import static com.example.wejam.TestUploads.anonymousGet;
import static com.example.wejam.TestUploads.put;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class VenuePhotoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectStorage storage;

    String owner;
    String venueId;

    @BeforeEach
    void setUp() {
        owner = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
        venueId = createVenue(owner);
    }

    @Test
    void anUploadedPhotoIsPubliclyReadableByItsUrl() throws Exception {
        MvcTestResult confirmed = uploadPhoto();
        assertThat(confirmed).hasStatusOk();
        String url = read(body(confirmed), "$.url");

        HttpResponse<byte[]> download = anonymousGet(url);
        assertThat(download.statusCode()).isEqualTo(200);
        assertThat(download.body()).isEqualTo(JPEG);
        assertThat(venue(owner)).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.photos[0].id").isEqualTo(read(body(confirmed), "$.id")),
                json -> json.assertThat().extractingPath("$.photos[0].url").isEqualTo(url));
    }

    @Test
    void nobodyCanListThePublicBucketAndThePrivateOneStaysPrivate() throws Exception {
        String url = read(body(uploadPhoto()), "$.url");
        String publicBucketUrl = url.substring(0, url.indexOf("/venues/"));
        String storageHost = publicBucketUrl.substring(0, publicBucketUrl.lastIndexOf('/'));

        assertThat(anonymousGet(publicBucketUrl + "/").statusCode()).isEqualTo(403);
        assertThat(anonymousGet(storageHost + "/wejam-private/").statusCode()).isEqualTo(403);
    }

    @Test
    void photosComeInUploadOrderSoTheFirstIsTheCover() throws Exception {
        List<String> uploaded = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            uploaded.add(read(body(uploadPhoto()), "$.id"));
        }

        List<String> listed = read(body(venue(owner)), "$.photos[*].id");
        assertThat(listed).containsExactlyElementsOf(uploaded);
    }

    @Test
    void aVenueHasAtMostTenPhotos() throws Exception {
        List<MvcTestResult> started = new ArrayList<>();
        for (int i = 0; i < 11; i++) { // all started before any is confirmed, so the cheap early check passes
            MvcTestResult start = startUpload(owner, "image/jpeg", JPEG.length);
            assertThat(put(start, JPEG, "image/jpeg")).isEqualTo(200);
            started.add(start);
        }
        for (int i = 0; i < 10; i++) {
            assertThat(confirm(started.get(i))).hasStatusOk();
        }

        assertThat(confirm(started.get(10))).hasStatus(409);
        assertThat(startUpload(owner, "image/jpeg", JPEG.length)).hasStatus(409);

        // Deleting one frees its slot.
        assertThat(delete(read(body(started.get(0)), "$.photoId"))).hasStatus(204);
        assertThat(confirm(started.get(10))).hasStatusOk();
        assertThat(photoCount()).isEqualTo(10);
    }

    @Test
    void parallelConfirmsNeverExceedTheCap() throws Exception {
        List<MvcTestResult> started = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            MvcTestResult start = startUpload(owner, "image/jpeg", JPEG.length);
            assertThat(put(start, JPEG, "image/jpeg")).isEqualTo(200);
            started.add(start);
        }

        List<Callable<Integer>> confirms = started.stream()
                .<Callable<Integer>>map(start -> () -> confirm(start).getResponse().getStatus()).toList();
        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(12)) {
            for (Future<Integer> f : pool.invokeAll(confirms)) {
                statuses.add(f.get());
            }
        }

        assertThat(statuses).filteredOn(s -> s == 200).hasSize(10);
        assertThat(statuses).filteredOn(s -> s == 409).hasSize(2);
        assertThat(photoCount()).isEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM venue_photos WHERE venue_id = ? AND status = 'UPLOADED'",
                Integer.class, UUID.fromString(venueId))).isEqualTo(10);
    }

    @Test
    void onlyImagesUpToFiveMegabytes() {
        assertThat(startUpload(owner, "image/gif", 100)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.contentType").isNotNull();
        assertThat(startUpload(owner, "application/pdf", 100)).hasStatus(400);
        assertThat(startUpload(owner, "image/jpeg", 5_242_881)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.sizeBytes").isNotNull();
    }

    @Test
    void onlyTheOwnerManagesPhotos() throws Exception {
        String photoId = read(body(uploadPhoto()), "$.id");
        String stranger = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");

        assertThat(startUpload(stranger, "image/jpeg", JPEG.length)).hasStatus(404);
        assertThat(mvc.delete().uri("/api/v1/venues/{v}/photos/{p}", venueId, photoId)
                .header(HttpHeaders.AUTHORIZATION, bearer(stranger))).hasStatus(404);
    }

    @Test
    void photosCanChangeAfterVerificationWithoutAffectingIt() throws Exception {
        jdbc.update("UPDATE venues SET verification_status = 'VERIFIED', verification_requested_at = now() "
                + "WHERE id = ?", UUID.fromString(venueId));

        String photoId = read(body(uploadPhoto()), "$.id");
        assertThat(delete(photoId)).hasStatus(204);

        assertThat(venue(owner)).bodyJson().extractingPath("$.verificationStatus").isEqualTo("VERIFIED");
    }

    @Test
    void deletingAPhotoRemovesItsFile() throws Exception {
        MvcTestResult confirmed = uploadPhoto();
        String url = read(body(confirmed), "$.url");

        assertThat(delete(read(body(confirmed), "$.id"))).hasStatus(204);

        assertThat(anonymousGet(url).statusCode()).isEqualTo(404);
        assertThat(photoCount()).isZero();
        assertThat(venue(owner)).bodyJson().extractingPath("$.photos").asArray().isEmpty();
    }

    @Test
    void deletingTheVenueRemovesItsPhotoFiles() throws Exception {
        uploadPhoto();
        String key = jdbc.queryForObject("SELECT storage_key FROM venue_photos WHERE venue_id = ?", String.class,
                UUID.fromString(venueId));

        assertThat(mvc.delete().uri("/api/v1/venues/{id}", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(owner))).hasStatus(204);

        assertThat(storage.head(Bucket.PUBLIC, key)).isEmpty();
    }

    // --- helpers

    private MvcTestResult uploadPhoto() throws Exception {
        MvcTestResult start = startUpload(owner, "image/jpeg", JPEG.length);
        assertThat(start).hasStatus(201);
        assertThat(put(start, JPEG, "image/jpeg")).isEqualTo(200);
        return confirm(start);
    }

    private MvcTestResult startUpload(String token, String contentType, long size) {
        return mvc.post().uri("/api/v1/venues/{id}/photos", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"contentType\":\"%s\",\"sizeBytes\":%d}".formatted(contentType, size))
                .exchange();
    }

    private MvcTestResult confirm(MvcTestResult start) {
        return mvc.post().uri("/api/v1/venues/{v}/photos/{p}/confirm", venueId, (String) read(body(start), "$.photoId"))
                .header(HttpHeaders.AUTHORIZATION, bearer(owner)).exchange();
    }

    private MvcTestResult delete(String photoId) {
        return mvc.delete().uri("/api/v1/venues/{v}/photos/{p}", venueId, photoId)
                .header(HttpHeaders.AUTHORIZATION, bearer(owner)).exchange();
    }

    private MvcTestResult venue(String token) {
        return mvc.get().uri("/api/v1/venues/{id}", venueId).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private int photoCount() {
        return jdbc.queryForObject("SELECT photo_count FROM venues WHERE id = ?", Integer.class, UUID.fromString(venueId));
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
