package com.example.wejam.auth.controller;

import com.example.wejam.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.wejam.TestAuth.accessTokenFor;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.randomUid;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class MeControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JwtEncoder jwtEncoder;

    @Autowired
    JwtDecoder jwtDecoder;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void meWithoutTokenReturns401() {
        assertThat(mvc.get().uri("/api/v1/me")).hasStatus(401);
    }

    @Test
    void meWithTamperedTokenReturns401() {
        String token = accessTokenFor(mvc, randomUid());
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThat(getMe(tampered)).hasStatus(401);
    }

    @Test
    void meWithExpiredTokenReturns401() {
        Instant past = Instant.now().minus(Duration.ofHours(2));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("wejam")
                .subject(UUID.randomUUID().toString())
                .issuedAt(past)
                .expiresAt(past.plus(Duration.ofHours(1)))
                .claim("roles", List.of("USER"))
                .build();
        String expired = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        assertThat(getMe(expired)).hasStatus(401);
    }

    @Test
    void meReturnsProfileOfTokenOwner() {
        String uid = randomUid();
        String token = accessTokenFor(mvc, uid);
        UUID userId = jdbc.queryForObject("SELECT id FROM users WHERE firebase_uid = ?", UUID.class, uid);

        assertThat(getMe(token)).hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.id").isEqualTo(userId.toString()),
                        json -> json.assertThat().extractingPath("$.roles").asArray().containsExactly("USER"));
    }

    @Test
    void addingHostRoleReturnsTokenThatPassesHasRoleHost() {
        String oldToken = accessTokenFor(mvc, randomUid());

        MvcTestResult result = addRole(oldToken, "HOST");

        assertThat(result).hasStatusOk();
        String newToken = read(body(result), "$.accessToken");
        assertThat(jwtDecoder.decode(newToken).getClaimAsStringList("roles")).containsExactly("HOST", "USER");

        assertThat(mvc.get().uri("/test/host-only").header(HttpHeaders.AUTHORIZATION, bearer(newToken)))
                .hasStatusOk();
        // Roles are frozen in the old token until it is replaced.
        assertThat(mvc.get().uri("/test/host-only").header(HttpHeaders.AUTHORIZATION, bearer(oldToken)))
                .hasStatus(403);
    }

    @Test
    void addingSameRoleTwiceIsIdempotent() {
        String uid = randomUid();
        String token = accessTokenFor(mvc, uid);

        assertThat(addRole(token, "VENUE_ADMIN")).hasStatusOk();
        assertThat(addRole(token, "VENUE_ADMIN")).hasStatusOk();

        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM user_roles r JOIN users u ON u.id = r.user_id
                WHERE u.firebase_uid = ? AND r.role = 'VENUE_ADMIN'
                """, Integer.class, uid)).isEqualTo(1);
    }

    @Test
    void addingUserRoleReturns400() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(addRole(token, "USER")).hasStatus(400)
                .bodyJson().extractingPath("$.detail").isEqualTo("Role USER cannot be self-assigned");
    }

    @Test
    void addingUnknownRoleReturns400() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(addRole(token, "SUPERUSER")).hasStatus(400);
    }

    @Test
    void addingRoleWithoutTokenReturns401() {
        assertThat(mvc.post().uri("/api/v1/me/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"HOST\"}"))
                .hasStatus(401);
    }

    @Test
    void newUserHasNoDisplayName() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(getMe(token)).hasStatusOk()
                .bodyJson().extractingPath("$.displayName").isNull();
    }

    @Test
    void updatingDisplayNameStoresTrimmedValue() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(updateProfile(token, "{\"displayName\":\"  Bob Marley  \"}")).hasStatusOk()
                .bodyJson().extractingPath("$.displayName").isEqualTo("Bob Marley");
        assertThat(getMe(token)).hasStatusOk()
                .bodyJson().extractingPath("$.displayName").isEqualTo("Bob Marley");
    }

    @Test
    void blankOrMissingDisplayNameReturns400() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(updateProfile(token, "{\"displayName\":\"   \"}")).hasStatus(400)
                .bodyJson().extractingPath("$.errors.displayName").isNotNull();
        assertThat(updateProfile(token, "{}")).hasStatus(400)
                .bodyJson().extractingPath("$.errors.displayName").isNotNull();
    }

    @Test
    void displayNameLongerThan50Returns400() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(updateProfile(token, "{\"displayName\":\"" + "a".repeat(51) + "\"}")).hasStatus(400);
        assertThat(updateProfile(token, "{\"displayName\":\"" + "a".repeat(50) + "\"}")).hasStatusOk();
    }

    @Test
    void updatingProfileWithoutTokenReturns401() {
        assertThat(mvc.patch().uri("/api/v1/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Bob\"}"))
                .hasStatus(401);
    }

    private MvcTestResult updateProfile(String token, String json) {
        return mvc.patch().uri("/api/v1/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult getMe(String token) {
        return mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private MvcTestResult addRole(String token, String role) {
        return mvc.post().uri("/api/v1/me/roles")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"" + role + "\"}")
                .exchange();
    }
}
