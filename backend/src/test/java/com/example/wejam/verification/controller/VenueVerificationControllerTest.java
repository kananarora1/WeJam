package com.example.wejam.verification.controller;

import com.example.wejam.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

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
import static com.example.wejam.TestAuth.exchange;
import static com.example.wejam.TestAuth.randomUid;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class VenueVerificationControllerTest {

    /** Listed in application-test.properties under wejam.admin.phones. */
    private static final String ADMIN_PHONE = "+919999999901";

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    String admin;
    String owner;

    @BeforeEach
    void setUp() {
        admin = read(body(exchange(mvc, "fake:platform-admin:" + ADMIN_PHONE)), "$.accessToken");
        send(mvc.patch().uri("/api/v1/me"), admin, "{\"displayName\":\"Asha (T&S)\"}");
        owner = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
    }

    @Test
    void nonAdminsGet403OnEveryAdminEndpoint() {
        String venueId = createVenue(owner, randomFssai());

        assertThat(get("/api/v1/admin/venue-verifications", owner)).hasStatus(403);
        assertThat(get("/api/v1/admin/venues/" + venueId, owner)).hasStatus(403);
        assertThat(post("/api/v1/admin/venues/" + venueId + "/approve", owner, "")).hasStatus(403);
        assertThat(post("/api/v1/admin/venues/" + venueId + "/reject", owner, "{\"reason\":\"x\"}")).hasStatus(403);
        assertThat(get("/api/v1/admin/actions", owner)).hasStatus(403);
    }

    @Test
    void queueIsOldestFirstWithAutomatedChecks() {
        String fssai = randomFssai();
        String first = createVenue(owner, fssai);
        String second = createVenue(owner, "39" + fssai.substring(2)); // licence type 3 → structure check fails
        createVenue(accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN"), fssai); // a duplicate of `first`

        MvcTestResult queue = get("/api/v1/admin/venue-verifications", admin);

        assertThat(queue).hasStatusOk();
        List<String> ids = read(body(queue), "$[*].venueId");
        assertThat(ids.indexOf(first)).isLessThan(ids.indexOf(second));
        assertThat(queue).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$[?(@.venueId=='" + first + "')].checks.fssaiStructureLooksValid")
                        .asArray().containsExactly(true),
                json -> json.assertThat().extractingPath("$[?(@.venueId=='" + first + "')].checks.otherVenuesWithSameFssai")
                        .asArray().containsExactly(1),
                json -> json.assertThat().extractingPath("$[?(@.venueId=='" + second + "')].checks.fssaiStructureLooksValid")
                        .asArray().containsExactly(false));
    }

    @Test
    void approveIsLoggedAndVisibleToTheOwner() {
        String venueId = createVenue(owner, randomFssai());

        assertThat(post("/api/v1/admin/venues/" + venueId + "/approve", admin, "")).hasStatusOk()
                .bodyJson().extractingPath("$.verificationStatus").isEqualTo("VERIFIED");

        assertThat(get("/api/v1/venues/" + venueId, owner)).bodyJson()
                .extractingPath("$.verificationStatus").isEqualTo("VERIFIED");
        assertThat(get("/api/v1/admin/actions", admin)).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.items[0].action").isEqualTo("VENUE_APPROVED"),
                json -> json.assertThat().extractingPath("$.items[0].targetId").isEqualTo(venueId),
                json -> json.assertThat().extractingPath("$.items[0].adminName").isEqualTo("Asha (T&S)"));
    }

    @Test
    void rejectNeedsAReasonAndTheOwnerCanResubmit() {
        String venueId = createVenue(owner, randomFssai());

        assertThat(post("/api/v1/admin/venues/" + venueId + "/reject", admin, "{\"reason\":\"  \"}")).hasStatus(400);
        assertThat(post("/api/v1/admin/venues/" + venueId + "/reject", admin,
                "{\"reason\":\" Number doesn't match the license photo \"}"))
                .hasStatusOk().bodyJson().satisfies(
                        json -> json.assertThat().extractingPath("$.verificationStatus").isEqualTo("REJECTED"),
                        json -> json.assertThat().extractingPath("$.rejectionReason")
                                .isEqualTo("Number doesn't match the license photo"));

        assertThat(post("/api/v1/venues/" + venueId + "/resubmit", owner, "")).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.verificationStatus").isEqualTo("PENDING"),
                json -> json.assertThat().extractingPath("$.rejectionReason").isNull());
        // Only a rejected venue can be resubmitted.
        assertThat(post("/api/v1/venues/" + venueId + "/resubmit", owner, "")).hasStatus(409);
        // And only by its owner.
        assertThat(post("/api/v1/venues/" + venueId + "/resubmit",
                accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN"), "")).hasStatus(404);
    }

    @Test
    void aDecidedVenueCannotBeDecidedAgain() {
        String venueId = createVenue(owner, randomFssai());
        post("/api/v1/admin/venues/" + venueId + "/approve", admin, "");

        assertThat(post("/api/v1/admin/venues/" + venueId + "/approve", admin, "")).hasStatus(409);
        assertThat(post("/api/v1/admin/venues/" + venueId + "/reject", admin, "{\"reason\":\"x\"}")).hasStatus(409);
        assertThat(post("/api/v1/admin/venues/" + UUID.randomUUID() + "/approve", admin, "")).hasStatus(404);
    }

    @Test
    void approvingASecondVenueWithAnAlreadyVerifiedFssaiNumberIs409() {
        String fssai = randomFssai();
        String first = createVenue(owner, fssai);
        String second = createVenue(accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN"), fssai);
        post("/api/v1/admin/venues/" + first + "/approve", admin, "");

        assertThat(post("/api/v1/admin/venues/" + second + "/approve", admin, "")).hasStatus(409)
                .bodyJson().extractingPath("$.detail").asString().contains("already verified");
        assertThat(get("/api/v1/venues/" + second, owner)).bodyJson()
                .extractingPath("$.verificationStatus").isEqualTo("PENDING");
    }

    @Test
    void concurrentApproveAndRejectHaveExactlyOneWinner() throws Exception {
        String venueId = createVenue(owner, randomFssai());
        String otherAdmin = admin; // same admin, two devices — the race is on the venue row either way

        List<Callable<Integer>> decisions = List.of(
                () -> post("/api/v1/admin/venues/" + venueId + "/approve", admin, "").getResponse().getStatus(),
                () -> post("/api/v1/admin/venues/" + venueId + "/reject", otherAdmin, "{\"reason\":\"x\"}")
                        .getResponse().getStatus());
        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (Future<Integer> f : pool.invokeAll(decisions)) {
                statuses.add(f.get());
            }
        }

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM admin_actions WHERE target_id = ?", Integer.class,
                UUID.fromString(venueId))).isEqualTo(1);
    }

    @Test
    void actionLogPagesBackwardsWithAKeysetCursor() {
        String older = createVenue(owner, randomFssai());
        String newer = createVenue(owner, randomFssai());
        post("/api/v1/admin/venues/" + older + "/approve", admin, "");
        post("/api/v1/admin/venues/" + newer + "/reject", admin, "{\"reason\":\"x\"}");

        MvcTestResult firstPage = get("/api/v1/admin/actions", admin);
        List<String> targets = read(body(firstPage), "$.items[*].targetId");
        assertThat(targets.indexOf(newer)).isLessThan(targets.indexOf(older));

        // Everything strictly older than the "newer" decision: contains "older", never "newer".
        // A JsonPath filter always yields an array, hence List + get(0).
        List<String> newerCreatedAt = read(body(firstPage), "$.items[?(@.targetId=='" + newer + "')].createdAt");
        List<String> newerId = read(body(firstPage), "$.items[?(@.targetId=='" + newer + "')].id");
        MvcTestResult olderPage = mvc.get().uri("/api/v1/admin/actions")
                .param("before", newerCreatedAt.getFirst()).param("beforeId", newerId.getFirst())
                .header(HttpHeaders.AUTHORIZATION, bearer(admin)).exchange();
        List<String> olderTargets = read(body(olderPage), "$.items[*].targetId");
        assertThat(olderTargets).contains(older).doesNotContain(newer);
    }

    private static String randomFssai() {
        // Licence type 1, state code 29 (Karnataka), random rest — unique per test, so duplicate counts are exact.
        return "129" + String.format("%011d", ThreadLocalRandom.current().nextLong(100_000_000_000L));
    }

    private String createVenue(String token, String fssai) {
        MvcTestResult result = send(mvc.post().uri("/api/v1/venues"), token, """
                {"name":"Amber Room","addressLine":"12th Main","city":"Bengaluru","latitude":12.97,"longitude":77.64,
                 "fssaiNumber":"%s","hostingMode":"OPEN"}
                """.formatted(fssai));
        assertThat(result).hasStatus(201);
        return read(body(result), "$.id");
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private MvcTestResult post(String uri, String token, String json) {
        var request = mvc.post().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token));
        return json.isEmpty() ? request.exchange()
                : request.contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private MvcTestResult send(MockMvcTester.MockMvcRequestBuilder request, String token, String json) {
        return request.header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }
}
