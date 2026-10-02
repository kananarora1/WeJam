package com.example.wejam.host.controller;

import com.example.wejam.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

import static com.example.wejam.TestAuth.accessTokenFor;
import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.randomUid;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class HostProfileControllerTest {

    private static final String GROUP = """
            {"type":"GROUP","groupKind":"BAND","groupName":"  The Low Notes ",
             "bio":"Four friends running slow blues nights in Indiranagar since 2024.","area":"Indiranagar",
             "instagramHandle":"@thelownotes","genres":["SOUL","BLUES"],
             "mediaLinks":[{"url":"https://youtu.be/one","title":"Live at Amber Room"},
                           {"url":"https://instagram.com/p/two","title":null}]}
            """;
    private static final String INDIVIDUAL = """
            {"type":"INDIVIDUAL","groupKind":"BAND","groupName":"ignored","genres":[],"mediaLinks":[]}
            """;

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    String host;

    @BeforeEach
    void setUp() {
        host = accessTokenWithRole(mvc, randomUid(), "HOST");
    }

    @Test
    void userWithoutHostRoleCannotCreateProfile() {
        assertThat(put(accessTokenFor(mvc, randomUid()), GROUP)).hasStatus(403);
    }

    @Test
    void groupProfileRoundTripsWithOrderedLinksAndSortedGenres() {
        assertThat(put(host, GROUP))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.type").isEqualTo("GROUP"),
                        json -> json.assertThat().extractingPath("$.groupKind").isEqualTo("BAND"),
                        json -> json.assertThat().extractingPath("$.displayName").isEqualTo("The Low Notes"),
                        json -> json.assertThat().extractingPath("$.instagramHandle").isEqualTo("thelownotes"),
                        json -> json.assertThat().extractingPath("$.genres").asArray().containsExactly("BLUES", "SOUL"),
                        json -> json.assertThat().extractingPath("$.mediaLinks[*].url").asArray()
                                .containsExactly("https://youtu.be/one", "https://instagram.com/p/two"),
                        json -> json.assertThat().extractingPath("$.mediaLinks[1].title").isNull());
    }

    @Test
    void individualProfileUsesOwnersNameAndIgnoresGroupFields() {
        mvc.patch().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(host))
                .contentType(MediaType.APPLICATION_JSON).content("{\"displayName\":\"Kanan\"}").exchange();

        assertThat(put(host, INDIVIDUAL))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.type").isEqualTo("INDIVIDUAL"),
                        json -> json.assertThat().extractingPath("$.displayName").isEqualTo("Kanan"),
                        json -> json.assertThat().extractingPath("$.groupKind").isNull());
    }

    @Test
    void groupNeedsKindAndName() {
        assertThat(put(host, """
                {"type":"GROUP","groupName":"  ","genres":[],"mediaLinks":[]}
                """))
                .hasStatus(400)
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.errors.groupKind").isNotNull(),
                        json -> json.assertThat().extractingPath("$.errors.groupName").isNotNull());
    }

    @Test
    void savingAgainReplacesTheSameProfileAndCanSwitchToIndividual() {
        String firstId = read(body(put(host, GROUP)), "$.id");

        MvcTestResult second = put(host, INDIVIDUAL);

        assertThat(second).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.id").isEqualTo(firstId),
                json -> json.assertThat().extractingPath("$.genres").asArray().isEmpty(),
                json -> json.assertThat().extractingPath("$.mediaLinks").asArray().isEmpty());
        UUID id = UUID.fromString(firstId);
        assertThat(jdbc.queryForObject("SELECT group_name FROM host_profiles WHERE id = ?", String.class, id)).isNull();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM host_media_links WHERE host_profile_id = ?",
                Integer.class, id)).isZero();
    }

    @Test
    void anySignedInUserCanReadAHostProfile() {
        String id = read(body(put(host, GROUP)), "$.id");
        String reader = accessTokenFor(mvc, randomUid());

        assertThat(get("/api/v1/host-profiles/" + id, reader))
                .hasStatusOk()
                .bodyJson().extractingPath("$.displayName").isEqualTo("The Low Notes");
        assertThat(get("/api/v1/host-profiles/" + UUID.randomUUID(), reader)).hasStatus(404);
    }

    @Test
    void myProfileIs404UntilCreated() {
        assertThat(get("/api/v1/me/host-profile", host)).hasStatus(404);
        put(host, GROUP);
        assertThat(get("/api/v1/me/host-profile", host)).hasStatusOk();
    }

    @Test
    void invalidAboutFieldsAreRejected() {
        assertThat(put(host, GROUP.replace("@thelownotes", "the low notes!"))).hasStatus(400)
                .bodyJson().extractingPath("$.errors.instagramHandle").isNotNull();
        assertThat(put(host, GROUP.replace("https://youtu.be/one", "ftp://youtu.be/one"))).hasStatus(400)
                .bodyJson().extractingPath("$.errors['mediaLinks[0].url']").isNotNull();
        assertThat(put(host, GROUP.replace("[\"SOUL\",\"BLUES\"]",
                "[\"SOUL\",\"BLUES\",\"JAZZ\",\"ROCK\",\"INDIE\",\"FOLK\"]"))).hasStatus(400)
                .bodyJson().extractingPath("$.errors.genres").isNotNull();
        assertThat(put(host, GROUP.replace("\"BLUES\"", "\"POLKA\""))).hasStatus(400);
        assertThat(put(host, GROUP.replace("Four friends", "x".repeat(201)))).hasStatus(400)
                .bodyJson().extractingPath("$.errors.bio").isNotNull();
    }

    @Test
    void databaseEnforcesOneProfilePerUserAndGroupConsistency() {
        String id = read(body(put(host, GROUP)), "$.id");
        UUID owner = jdbc.queryForObject("SELECT owner_id FROM host_profiles WHERE id = ?", UUID.class,
                UUID.fromString(id));

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO host_profiles (owner_id, type) VALUES (?, 'INDIVIDUAL')", owner))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                "UPDATE host_profiles SET group_name = NULL WHERE id = ?", UUID.fromString(id)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private MvcTestResult put(String token, String json) {
        return mvc.put().uri("/api/v1/me/host-profile")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }
}
