package com.example.wejam.verification.job;

import com.example.wejam.IntegrationTest;
import com.example.wejam.common.storage.ObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.List;
import java.util.UUID;

import static com.example.wejam.TestAuth.body;
import static com.example.wejam.verification.VerificationTestSupport.JPEG;
import static com.example.wejam.verification.VerificationTestSupport.adminToken;
import static com.example.wejam.verification.VerificationTestSupport.get;
import static com.example.wejam.verification.VerificationTestSupport.hostProfileId;
import static com.example.wejam.verification.VerificationTestSupport.newHost;
import static com.example.wejam.verification.VerificationTestSupport.pendingHost;
import static com.example.wejam.verification.VerificationTestSupport.post;
import static com.example.wejam.verification.VerificationTestSupport.put;
import static com.example.wejam.verification.VerificationTestSupport.requestVerification;
import static com.example.wejam.verification.VerificationTestSupport.startHostUpload;
import static com.example.wejam.verification.VerificationTestSupport.uploadHostId;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

/** Scheduling is off in tests; we call the job ourselves and move timestamps into the past with SQL. */
@IntegrationTest
class DocumentCleanupJobTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ObjectStorage storage;

    @Autowired
    DocumentCleanupJob job;

    String admin;

    @BeforeEach
    void setUp() {
        admin = adminToken(mvc);
    }

    @Test
    void deletesTheIdThirtyDaysAfterApprovalButTheBadgeStays() throws Exception {
        String host = pendingHost(mvc);
        String id = hostProfileId(mvc, host);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/approve", admin, null)).hasStatusOk();
        List<String> keys = keysOf(id);
        assertThat(keys).hasSize(1);

        expire(id);
        job.run();

        assertThat(keysOf(id)).isEmpty();
        assertThat(storage.head(keys.getFirst())).isEmpty();
        assertThat(get(mvc, "/api/v1/host-profiles/" + id, host)).bodyJson()
                .extractingPath("$.verified").isEqualTo(true);
    }

    @Test
    void afterARejectedIdIsDeletedTheHostMustUploadAgain() throws Exception {
        String host = pendingHost(mvc);
        String id = hostProfileId(mvc, host);
        assertThat(post(mvc, "/api/v1/admin/hosts/" + id + "/reject", admin, "{\"reason\":\"Blurry\"}")).hasStatusOk();

        expire(id);
        job.run();

        assertThat(requestVerification(mvc, host, "COLLEGE_ID")).hasStatus(409);
        assertThat(uploadHostId(mvc, host, "ID_FRONT")).hasStatusOk();
        assertThat(requestVerification(mvc, host, "COLLEGE_ID")).hasStatusOk();
    }

    @Test
    void anIdThatWasNeverSubmittedIsDeletedToo() throws Exception {
        String host = newHost(mvc);
        String id = hostProfileId(mvc, host);
        assertThat(uploadHostId(mvc, host, "ID_FRONT")).hasStatusOk();

        expire(id);
        job.run();

        assertThat(keysOf(id)).isEmpty();
    }

    @Test
    void keepsIdsUnderReview() throws Exception {
        String id = hostProfileId(mvc, pendingHost(mvc));

        job.run();

        assertThat(keysOf(id)).hasSize(1);
    }

    @Test
    void sweepsUploadsThatWereNeverConfirmedAndKeepsFreshOnes() throws Exception {
        String host = newHost(mvc);
        MvcTestResult abandoned = startHostUpload(mvc, host, "ID_FRONT", "image/jpeg", JPEG.length);
        assertThat(put(abandoned, JPEG, "image/jpeg")).isEqualTo(200); // file landed, app never confirmed
        MvcTestResult fresh = startHostUpload(mvc, host, "ID_BACK", "image/jpeg", JPEG.length);
        UUID abandonedId = UUID.fromString(read(body(abandoned), "$.documentId"));
        String abandonedKey = jdbc.queryForObject("SELECT storage_key FROM verification_documents WHERE id = ?",
                String.class, abandonedId);
        assertThat(storage.head(abandonedKey)).isPresent();

        jdbc.update("UPDATE verification_documents SET created_at = now() - interval '25 hours' WHERE id = ?",
                abandonedId);
        job.run();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM verification_documents WHERE id = ?", Integer.class,
                abandonedId)).isZero();
        assertThat(storage.head(abandonedKey)).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM verification_documents WHERE id = ?", Integer.class,
                UUID.fromString(read(body(fresh), "$.documentId")))).isEqualTo(1);
    }

    /** Pretend the retention period is over. */
    private void expire(String hostProfileId) {
        assertThat(jdbc.update("""
                UPDATE verification_documents SET delete_after = now() - interval '1 minute'
                WHERE owner_type = 'HOST_PROFILE' AND owner_id = ? AND delete_after IS NOT NULL
                """, UUID.fromString(hostProfileId))).isPositive();
    }

    private List<String> keysOf(String hostProfileId) {
        return jdbc.queryForList("SELECT storage_key FROM verification_documents WHERE owner_id = ?", String.class,
                UUID.fromString(hostProfileId));
    }
}
