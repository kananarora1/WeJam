package com.example.wejam.venue.controller;

import com.example.wejam.IntegrationTest;
import com.example.wejam.TestVenues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.exchange;
import static com.example.wejam.TestAuth.randomUid;
import static com.example.wejam.TestUploads.JPEG;
import static com.example.wejam.TestUploads.put;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Venues are private drafts until the owner submits them for review (design 7.1–7.4). */
@IntegrationTest
class VenueDraftControllerTest {

    /** Onboarding step 1 (design 7.1): no license details yet. */
    private static final String DETAILS_ONLY = """
            {"name":"Amber Room","addressLine":"100 Ft Road","city":"Bengaluru","latitude":12.97,
             "longitude":77.64,"hostingMode":"OPEN"}
            """;

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    String owner;
    String venueId;

    @BeforeEach
    void setUp() {
        owner = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
        MvcTestResult created = send(mvc.post().uri("/api/v1/venues"), owner, DETAILS_ONLY);
        assertThat(created).hasStatus(201).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.verificationStatus").isEqualTo("DRAFT"),
                json -> json.assertThat().extractingPath("$.fssaiNumber").isNull());
        venueId = read(body(created), "$.id");
    }

    @Test
    void submittingListsEverythingStillMissing() {
        assertThat(submit(owner)).hasStatus(400).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.errors.fssaiNumber").isNotNull(),
                json -> json.assertThat().extractingPath("$.errors.fssaiLicenseName").isNotNull(),
                json -> json.assertThat().extractingPath("$.errors.fssaiValidUntil").isNotNull(),
                json -> json.assertThat().extractingPath("$.errors.fssaiCertificate").isNotNull(),
                json -> json.assertThat().extractingPath("$.errors.photos").isEqualTo("add at least 3 (you have 0)"));
        assertThat(status()).isEqualTo("DRAFT");
    }

    @Test
    void aCompleteDraftWithThreePhotosGoesToReview() throws Exception {
        assertThat(send(mvc.put().uri("/api/v1/venues/{id}", venueId), owner, withLicense())).hasStatusOk()
                .bodyJson().extractingPath("$.verificationStatus").isEqualTo("DRAFT");
        uploadPhoto();
        uploadPhoto();
        assertThat(submit(owner)).hasStatus(400).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.errors.photos").isEqualTo("add at least 3 (you have 2)"),
                json -> json.assertThat().extractingPath("$.errors.fssaiCertificate").isNotNull(),
                json -> json.assertThat().doesNotHavePath("$.errors.fssaiNumber"));

        uploadPhoto();
        uploadCertificate();
        assertThat(submit(owner)).hasStatusOk().bodyJson().extractingPath("$.verificationStatus").isEqualTo("PENDING");
        assertThat(submit(owner)).hasStatus(409);

        String admin = read(body(exchange(mvc, "fake:platform-admin:+919999999901")), "$.accessToken");
        List<String> queued = read(body(get("/api/v1/admin/venue-verifications", admin)), "$[*].venueId");
        assertThat(queued).contains(venueId);
    }

    @Test
    void draftsNeverReachTheAdminQueue() {
        String admin = read(body(exchange(mvc, "fake:platform-admin:+919999999901")), "$.accessToken");

        List<String> queued = read(body(get("/api/v1/admin/venue-verifications", admin)), "$[*].venueId");
        assertThat(queued).doesNotContain(venueId);
    }

    @Test
    void licenseDetailsCannotBeRemovedOnceSubmitted() {
        send(mvc.put().uri("/api/v1/venues/{id}", venueId), owner, withLicense());
        TestVenues.markReadyToSubmit(jdbc, venueId);
        assertThat(submit(owner)).hasStatusOk();

        assertThat(send(mvc.put().uri("/api/v1/venues/{id}", venueId), owner, DETAILS_ONLY)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.fssaiLicenseName").isEqualTo("can't be removed once submitted");
    }

    @Test
    void aLicenseThatExpiredWhileDraftingCantBeSubmitted() {
        send(mvc.put().uri("/api/v1/venues/{id}", venueId), owner, withLicense());
        jdbc.update("UPDATE venues SET photo_count = 3, fssai_valid_until = DATE '2020-01-31' WHERE id = ?",
                UUID.fromString(venueId));

        assertThat(submit(owner)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.fssaiValidUntil").asString().startsWith("has passed");
    }

    @Test
    void theCertificateCanBeReplacedButNotRemovedOnceSubmitted() throws Exception {
        send(mvc.put().uri("/api/v1/venues/{id}", venueId), owner, withLicense());
        String certificateId = uploadCertificate();
        jdbc.update("UPDATE venues SET photo_count = 3 WHERE id = ?", UUID.fromString(venueId));
        assertThat(submit(owner)).hasStatusOk();

        assertThat(send(mvc.delete().uri("/api/v1/venues/{v}/documents/{d}", venueId, certificateId), owner, null))
                .hasStatus(409);
        uploadCertificate(); // replacing is fine
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM verification_documents
                WHERE owner_id = ? AND doc_type = 'FSSAI_CERTIFICATE' AND status = 'UPLOADED'
                """, Integer.class, UUID.fromString(venueId))).isEqualTo(1);
    }

    @Test
    void onlyTheOwnerCanSubmit() {
        assertThat(submit(accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN"))).hasStatus(404);
    }

    @Test
    void theDatabaseRequiresLicenseDetailsOutsideDrafts() {
        assertThatThrownBy(() -> jdbc.update("UPDATE venues SET verification_status = 'PENDING' WHERE id = ?",
                UUID.fromString(venueId))).isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- helpers

    private static String withLicense() {
        String fssai = "129" + String.format("%011d", ThreadLocalRandom.current().nextLong(100_000_000_000L));
        return DETAILS_ONLY.replace("\"hostingMode\":\"OPEN\"", "\"hostingMode\":\"OPEN\",\"fssaiNumber\":\"" + fssai
                + "\",\"fssaiLicenseName\":\"Amber Hospitality LLP\",\"fssaiValidUntil\":\"2030-03-31\"");
    }

    private void uploadPhoto() throws Exception {
        MvcTestResult start = send(mvc.post().uri("/api/v1/venues/{id}/photos", venueId), owner,
                "{\"contentType\":\"image/jpeg\",\"sizeBytes\":%d}".formatted(JPEG.length));
        assertThat(put(start, JPEG, "image/jpeg")).isEqualTo(200);
        assertThat(send(mvc.post().uri("/api/v1/venues/{v}/photos/{p}/confirm", venueId,
                (String) read(body(start), "$.photoId")), owner, null)).hasStatusOk();
    }

    /** Returns the document id. */
    private String uploadCertificate() throws Exception {
        MvcTestResult start = send(mvc.post().uri("/api/v1/venues/{id}/documents", venueId), owner,
                "{\"type\":\"FSSAI_CERTIFICATE\",\"contentType\":\"image/jpeg\",\"sizeBytes\":%d}"
                        .formatted(JPEG.length));
        assertThat(put(start, JPEG, "image/jpeg")).isEqualTo(200);
        String documentId = read(body(start), "$.documentId");
        assertThat(send(mvc.post().uri("/api/v1/venues/{v}/documents/{d}/confirm", venueId, documentId), owner, null))
                .hasStatusOk();
        return documentId;
    }

    private MvcTestResult submit(String token) {
        return send(mvc.post().uri("/api/v1/venues/{id}/submit", venueId), token, null);
    }

    private String status() {
        return jdbc.queryForObject("SELECT verification_status FROM venues WHERE id = ?", String.class,
                UUID.fromString(venueId));
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private MvcTestResult send(MockMvcTester.MockMvcRequestBuilder request, String token, String json) {
        request.header(HttpHeaders.AUTHORIZATION, bearer(token));
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return request.exchange();
    }
}
