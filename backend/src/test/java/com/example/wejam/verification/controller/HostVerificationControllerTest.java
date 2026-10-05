package com.example.wejam.verification.controller;

import com.example.wejam.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.wejam.TestAuth.body;
import static com.example.wejam.verification.VerificationTestSupport.adminToken;
import static com.example.wejam.verification.VerificationTestSupport.get;
import static com.example.wejam.verification.VerificationTestSupport.hostProfileId;
import static com.example.wejam.verification.VerificationTestSupport.newHost;
import static com.example.wejam.verification.VerificationTestSupport.pendingHost;
import static com.example.wejam.verification.VerificationTestSupport.post;
import static com.example.wejam.verification.VerificationTestSupport.requestVerification;
import static com.example.wejam.verification.VerificationTestSupport.startHostUpload;
import static com.example.wejam.verification.VerificationTestSupport.uploadHostId;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegrationTest
class HostVerificationControllerTest {

    private static final String VERIFICATION = "/api/v1/me/host-profile/verification";

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    String admin;

    @BeforeEach
    void setUp() {
        admin = adminToken(mvc);
    }

    @Test
    void nonAdminsGet403() throws Exception {
        String host = pendingHost(mvc);
        String id = hostProfileId(mvc, host);

        assertThat(get(mvc, "/api/v1/admin/host-verifications", host)).hasStatus(403);
        assertThat(get(mvc, "/api/v1/admin/hosts/" + id, host)).hasStatus(403);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/approve", host, null)).hasStatus(403);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/reject", host, "{\"reason\":\"x\"}")).hasStatus(403);
    }

    @Test
    void adminReviewsTheIdAndApproves() throws Exception {
        String host = pendingHost(mvc);
        String id = hostProfileId(mvc, host);

        List<Map<String, Object>> queue = read(body(get(mvc, "/api/v1/admin/host-verifications", admin)),
                "$[?(@.hostProfileId == '" + id + "')]");
        assertThat(queue).singleElement().satisfies(item -> {
            assertThat(item).containsEntry("displayName", "The Low Notes").containsEntry("type", "GROUP")
                    .containsEntry("idType", "COLLEGE_ID").containsEntry("status", "PENDING");
        });
        assertThat(get(mvc, "/api/v1/admin/hosts/" + id, admin)).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.profile.displayName").isEqualTo("The Low Notes"),
                json -> json.assertThat().extractingPath("$.status").isEqualTo("PENDING"),
                json -> json.assertThat().extractingPath("$.idType").isEqualTo("COLLEGE_ID"),
                json -> json.assertThat().extractingPath("$.documents[0].type").isEqualTo("ID_FRONT"),
                json -> json.assertThat().extractingPath("$.documents[0].downloadUrl").asString().startsWith("http"));

        String approved = body(post(mvc, "/api/v1/admin/hosts/" + id + "/approve", admin, null));
        assertThat((String) read(approved, "$.status")).isEqualTo("VERIFIED");
        assertThat((Boolean) read(approved, "$.profile.verified")).isTrue();
        // The 30-day clock starts at the decision.
        assertThat(Instant.parse(read(approved, "$.documents[0].scheduledDeletionAt")))
                .isCloseTo(Instant.now().plus(Duration.ofDays(30)), within(Duration.ofMinutes(1)));
        assertThat(jdbc.queryForObject(
                "SELECT action || ':' || target_type FROM admin_actions WHERE target_id = ?", String.class,
                UUID.fromString(id))).isEqualTo("HOST_APPROVED:HOST_PROFILE");

        assertThat(get(mvc, "/api/v1/host-profiles/" + id, host)).bodyJson()
                .extractingPath("$.verified").isEqualTo(true);
        assertThat(startHostUpload(mvc, host, "ID_BACK", "image/jpeg", 10)).hasStatus(409);
    }

    @Test
    void rejectionIsPrivateAndTheHostCanTryAgain() throws Exception {
        String host = pendingHost(mvc);
        String id = hostProfileId(mvc, host);

        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/reject", admin,
                "{\"reason\":\"  The photo is too blurry to read the name.  \"}")).hasStatusOk();

        assertThat(get(mvc, VERIFICATION, host)).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.status").isEqualTo("REJECTED"),
                json -> json.assertThat().extractingPath("$.rejectionReason")
                        .isEqualTo("The photo is too blurry to read the name."),
                json -> json.assertThat().extractingPath("$.documents[0].scheduledDeletionAt").isNotNull());
        // To everyone else a rejected host looks exactly like one who never asked.
        String publicProfile = body(get(mvc, "/api/v1/host-profiles/" + id, admin));
        assertThat((Boolean) read(publicProfile, "$.verified")).isFalse();
        assertThat(publicProfile).doesNotContain("blurry", "REJECTED");

        assertThat(uploadHostId(mvc, host, "ID_FRONT")).hasStatusOk();
        assertThat(requestVerification(mvc, host, "COMPANY_ID")).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.status").isEqualTo("PENDING"),
                json -> json.assertThat().extractingPath("$.idType").isEqualTo("COMPANY_ID"),
                json -> json.assertThat().extractingPath("$.rejectionReason").isNull(),
                json -> json.assertThat().extractingPath("$.documents[0].scheduledDeletionAt").isNull());
    }

    @Test
    void decisionsNeedAPendingRequest() {
        String id = hostProfileId(mvc, newHost(mvc));

        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/approve", admin, null)).hasStatus(409);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/reject", admin, "{\"reason\":\"x\"}")).hasStatus(409);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + UUID.randomUUID() + "/approve", admin, null)).hasStatus(404);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/reject", admin, "{\"reason\":\" \"}")).hasStatus(400);
    }

    @Test
    void concurrentApproveAndRejectHaveExactlyOneWinner() throws Exception {
        String id = hostProfileId(mvc, pendingHost(mvc));

        List<Callable<Integer>> decisions = List.of(
                () -> post(mvc, "/api/v1/admin/hosts/" + id + "/approve", admin, null).getResponse().getStatus(),
                () -> post(mvc, "/api/v1/admin/hosts/" + id + "/reject", admin, "{\"reason\":\"x\"}")
                        .getResponse().getStatus());
        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (Future<Integer> f : pool.invokeAll(decisions)) {
                statuses.add(f.get());
            }
        }

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM admin_actions WHERE target_id = ?", Integer.class,
                UUID.fromString(id))).isEqualTo(1);
    }
}
