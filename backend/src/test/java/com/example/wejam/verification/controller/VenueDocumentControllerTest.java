package com.example.wejam.verification.controller;

import com.example.wejam.IntegrationTest;
import com.example.wejam.common.storage.ObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.exchange;
import static com.example.wejam.TestAuth.randomUid;
import static com.example.wejam.verification.VerificationTestSupport.put;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class VenueDocumentControllerTest {

    private static final byte[] PDF = "%PDF-1.4 fake license scan".getBytes(StandardCharsets.UTF_8);
    private static final HttpClient HTTP = HttpClient.newHttpClient();

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
    void uploadConfirmAndDownloadRoundTrip() throws Exception {
        MvcTestResult start = startUpload(owner, "FSSAI_CERTIFICATE", "application/pdf", PDF.length);
        assertThat(start).hasStatus(201);

        assertThat(put(start, PDF, "application/pdf")).isEqualTo(200);
        MvcTestResult confirmed = confirm(owner, read(body(start), "$.documentId"));

        assertThat(confirmed).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.type").isEqualTo("FSSAI_CERTIFICATE"),
                json -> json.assertThat().extractingPath("$.sizeBytes").isEqualTo(PDF.length),
                // Venue documents are business records: never scheduled for deletion.
                json -> json.assertThat().extractingPath("$.scheduledDeletionAt").isNull());
        String downloadUrl = read(body(confirmed), "$.downloadUrl");
        HttpResponse<byte[]> download = HTTP.send(HttpRequest.newBuilder(URI.create(downloadUrl)).build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(download.statusCode()).isEqualTo(200);
        assertThat(download.body()).isEqualTo(PDF);
    }

    @Test
    void storageRejectsAFileThatDoesNotMatchTheSignedSizeOrType() throws Exception {
        MvcTestResult start = startUpload(owner, "FSSAI_CERTIFICATE", "application/pdf", PDF.length);

        byte[] bigger = (new String(PDF, StandardCharsets.UTF_8) + "!").getBytes(StandardCharsets.UTF_8);
        assertThat(put(start, bigger, "application/pdf")).isNotEqualTo(200);
        assertThat(put(start, PDF, "image/png")).isNotEqualTo(200);

        // Nothing landed, so confirming fails.
        assertThat(confirm(owner, read(body(start), "$.documentId"))).hasStatus(409);
    }

    @Test
    void declarationsAreValidatedBeforeAnyUrlIsIssued() {
        assertThat(startUpload(owner, "FSSAI_CERTIFICATE", "text/plain", 10)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.contentType").isNotNull();
        assertThat(startUpload(owner, "FSSAI_CERTIFICATE", "application/pdf", 10_485_761)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.sizeBytes").isNotNull();
        assertThat(startUpload(owner, "FSSAI_CERTIFICATE", "application/pdf", 0)).hasStatus(400);
        assertThat(startUpload(owner, "ID_FRONT", "application/pdf", 10)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.type").isEqualTo("is not a venue document type");
    }

    @Test
    void uploadingTheSameTypeAgainReplacesTheOldDocumentAndItsFile() throws Exception {
        String firstKey = uploadAndConfirm("LEASE_AGREEMENT");
        String secondKey = uploadAndConfirm("LEASE_AGREEMENT");

        assertThat(get("/api/v1/venues/" + venueId + "/documents", owner)).hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(1);
        assertThat(storage.head(firstKey)).isEmpty();
        assertThat(storage.head(secondKey)).isPresent();
    }

    @Test
    void ownerCanDeleteADocumentAndItsFile() throws Exception {
        String key = uploadAndConfirm("LEASE_AGREEMENT");
        String id = jdbc.queryForObject("SELECT id::text FROM verification_documents WHERE storage_key = ?",
                String.class, key);

        assertThat(mvc.delete().uri("/api/v1/venues/{v}/documents/{d}", venueId, id)
                .header(HttpHeaders.AUTHORIZATION, bearer(owner))).hasStatus(204);

        assertThat(storage.head(key)).isEmpty();
        assertThat(get("/api/v1/venues/" + venueId + "/documents", owner)).bodyJson()
                .extractingPath("$.length()").isEqualTo(0);
    }

    @Test
    void onlyTheOwnerOfAnUnverifiedVenueCanUpload() {
        String stranger = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
        assertThat(startUpload(stranger, "LEASE_AGREEMENT", "application/pdf", PDF.length)).hasStatus(404);

        jdbc.update("UPDATE venues SET verification_status = 'VERIFIED' WHERE id = ?", UUID.fromString(venueId));
        assertThat(startUpload(owner, "LEASE_AGREEMENT", "application/pdf", PDF.length)).hasStatus(409);
    }

    @Test
    void adminSeesDocumentsAndFlagsWhatToFixWhichResubmitClears() throws Exception {
        uploadAndConfirm("FSSAI_CERTIFICATE");
        String admin = read(body(exchange(mvc, "fake:platform-admin:+919999999901")), "$.accessToken");

        assertThat(get("/api/v1/admin/venues/" + venueId, admin)).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.documents[0].type").isEqualTo("FSSAI_CERTIFICATE"),
                json -> json.assertThat().extractingPath("$.documents[0].downloadUrl").asString().startsWith("http"));

        assertThat(mvc.post().uri("/api/v1/admin/venues/{id}/reject", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Certificate photo is blurry\",\"issues\":[\"FSSAI_CERTIFICATE\",\"FSSAI_NUMBER\"]}"))
                .hasStatusOk();
        assertThat(get("/api/v1/venues/" + venueId, owner)).bodyJson()
                .extractingPath("$.verificationIssues").asArray().containsExactly("FSSAI_NUMBER", "FSSAI_CERTIFICATE");

        mvc.post().uri("/api/v1/venues/{id}/resubmit", venueId).header(HttpHeaders.AUTHORIZATION, bearer(owner)).exchange();
        assertThat(get("/api/v1/venues/" + venueId, owner)).bodyJson()
                .extractingPath("$.verificationIssues").asArray().isEmpty();
    }

    @Test
    void deletingTheVenueRemovesItsDocumentsAndFiles() throws Exception {
        String key = uploadAndConfirm("FSSAI_CERTIFICATE");

        mvc.delete().uri("/api/v1/venues/{id}", venueId).header(HttpHeaders.AUTHORIZATION, bearer(owner)).exchange();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM verification_documents WHERE owner_id = ?",
                Integer.class, UUID.fromString(venueId))).isZero();
        assertThat(storage.head(key)).isEmpty();
    }

    // --- helpers

    private String uploadAndConfirm(String type) throws Exception {
        MvcTestResult start = startUpload(owner, type, "application/pdf", PDF.length);
        assertThat(put(start, PDF, "application/pdf")).isEqualTo(200);
        String documentId = read(body(start), "$.documentId");
        assertThat(confirm(owner, documentId)).hasStatusOk();
        return jdbc.queryForObject("SELECT storage_key FROM verification_documents WHERE id = ?", String.class,
                UUID.fromString(documentId));
    }

    private MvcTestResult startUpload(String token, String type, String contentType, long size) {
        return mvc.post().uri("/api/v1/venues/{id}/documents", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"%s\",\"contentType\":\"%s\",\"sizeBytes\":%d}".formatted(type, contentType, size))
                .exchange();
    }

    private MvcTestResult confirm(String token, String documentId) {
        return mvc.post().uri("/api/v1/venues/{v}/documents/{d}/confirm", venueId, documentId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private String createVenue(String token) {
        String fssai = "129" + String.format("%011d", ThreadLocalRandom.current().nextLong(100_000_000_000L));
        MvcTestResult result = mvc.post().uri("/api/v1/venues")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Amber Room","addressLine":"12th Main","city":"Bengaluru","latitude":12.97,
                         "longitude":77.64,"fssaiNumber":"%s","hostingMode":"OPEN"}
                        """.formatted(fssai))
                .exchange();
        assertThat(result).hasStatus(201);
        return read(body(result), "$.id");
    }
}
