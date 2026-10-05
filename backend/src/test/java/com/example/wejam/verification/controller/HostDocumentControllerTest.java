package com.example.wejam.verification.controller;

import com.example.wejam.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;
import java.time.Instant;

import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.randomUid;
import static com.example.wejam.verification.VerificationTestSupport.get;
import static com.example.wejam.verification.VerificationTestSupport.hostProfileId;
import static com.example.wejam.verification.VerificationTestSupport.newHost;
import static com.example.wejam.verification.VerificationTestSupport.requestVerification;
import static com.example.wejam.verification.VerificationTestSupport.startHostUpload;
import static com.example.wejam.verification.VerificationTestSupport.uploadHostId;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@IntegrationTest
class HostDocumentControllerTest {

    private static final String VERIFICATION = "/api/v1/me/host-profile/verification";

    @Autowired
    MockMvcTester mvc;

    String host;

    @BeforeEach
    void setUp() {
        host = newHost(mvc);
    }

    @Test
    void verificationIsOptionalAndOthersOnlyEverSeeAPositiveFlag() {
        assertThat(get(mvc, VERIFICATION, host)).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.status").isEqualTo("NOT_REQUESTED"),
                json -> json.assertThat().extractingPath("$.documents").asArray().isEmpty());

        String reader = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
        String publicProfile = body(get(mvc, "/api/v1/host-profiles/" + hostProfileId(mvc, host), reader));
        assertThat((Boolean) read(publicProfile, "$.verified")).isFalse();
        assertThat(publicProfile).doesNotContain("NOT_REQUESTED", "verificationStatus", "rejectionReason");
    }

    @Test
    void uploadTheIdThenAskForTheBadge() throws Exception {
        MvcTestResult front = uploadHostId(mvc, host, "ID_FRONT");
        assertThat(front).hasStatusOk();
        // Not submitted yet: an uploaded ID is still deleted after the retention period.
        Instant deletesAt = Instant.parse(read(body(front), "$.scheduledDeletionAt"));
        assertThat(deletesAt).isCloseTo(Instant.now().plus(Duration.ofDays(30)), within(Duration.ofMinutes(1)));
        assertThat(uploadHostId(mvc, host, "ID_BACK")).hasStatusOk();

        assertThat(requestVerification(mvc, host, "COLLEGE_ID")).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.status").isEqualTo("PENDING"),
                json -> json.assertThat().extractingPath("$.idType").isEqualTo("COLLEGE_ID"),
                json -> json.assertThat().extractingPath("$.requestedAt").isNotNull(),
                json -> json.assertThat().extractingPath("$.documents[*].type").asArray()
                        .containsExactlyInAnyOrder("ID_FRONT", "ID_BACK"),
                // Under review: kept until there's a decision.
                json -> json.assertThat().extractingPath("$.documents[*].scheduledDeletionAt").asArray()
                        .containsOnlyNulls());
    }

    @Test
    void askingNeedsTheFrontOfTheId() throws Exception {
        assertThat(uploadHostId(mvc, host, "ID_BACK")).hasStatusOk();

        assertThat(requestVerification(mvc, host, "COMPANY_ID")).hasStatus(409)
                .bodyJson().extractingPath("$.detail").isEqualTo("Upload the front of your ID first");
        assertThat(get(mvc, VERIFICATION, host)).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.status").isEqualTo("NOT_REQUESTED"),
                // The refused request rolled back entirely: the back side is still scheduled for deletion.
                json -> json.assertThat().extractingPath("$.documents[0].scheduledDeletionAt").isNotNull());
    }

    @Test
    void theIdIsLockedWhileUnderReview() throws Exception {
        String frontId = read(body(uploadHostId(mvc, host, "ID_FRONT")), "$.id");
        assertThat(requestVerification(mvc, host, "COLLEGE_ID")).hasStatusOk();

        assertThat(startHostUpload(mvc, host, "ID_BACK", "image/jpeg", 10)).hasStatus(409);
        assertThat(mvc.delete().uri("/api/v1/me/host-profile/documents/{id}", frontId)
                .header(HttpHeaders.AUTHORIZATION, bearer(host))).hasStatus(409);
        assertThat(requestVerification(mvc, host, "COLLEGE_ID")).hasStatus(409);
    }

    @Test
    void onlyIdSidesAndOnlyForYourOwnHostProfile() {
        assertThat(startHostUpload(mvc, host, "FSSAI_CERTIFICATE", "application/pdf", 10)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.type").isNotNull();
        assertThat(startHostUpload(mvc, host, "ID_FRONT", "image/gif", 10)).hasStatus(400)
                .bodyJson().extractingPath("$.errors.contentType").isNotNull();

        String noProfile = accessTokenWithRole(mvc, randomUid(), "HOST");
        assertThat(get(mvc, VERIFICATION, noProfile)).hasStatus(404);
        assertThat(startHostUpload(mvc, noProfile, "ID_FRONT", "image/jpeg", 10)).hasStatus(404);
        assertThat(requestVerification(mvc, noProfile, "COLLEGE_ID")).hasStatus(404);
    }

    @Test
    void ownerCanRemoveAnUploadedSideBeforeAsking() throws Exception {
        String backId = read(body(uploadHostId(mvc, host, "ID_BACK")), "$.id");

        assertThat(mvc.delete().uri("/api/v1/me/host-profile/documents/{id}", backId)
                .header(HttpHeaders.AUTHORIZATION, bearer(host))).hasStatus(204);
        assertThat(get(mvc, VERIFICATION, host)).bodyJson().extractingPath("$.documents").asArray().isEmpty();
    }
}
