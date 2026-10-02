package com.example.wejam.venue.controller;

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
class VenueControllerTest {

    private static final String FSSAI = "12345678901234";
    private static final String VENUE = """
            {"name":"  Amber Room ","description":"Cosy upstairs room","addressLine":"12th Main, HAL 2nd Stage",
             "city":"Bengaluru","latitude":12.9784,"longitude":77.6408,
             "fssaiNumber":"12345678901234","hostingMode":"OPEN"}
            """;
    private static final String SPACE = """
            {"name":"Main floor","capacity":60,"soundPolicy":"AMPLIFIED_ALLOWED","soundCurfew":"23:00",
             "houseRules":"18+ after 9 PM"}
            """;

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    String admin;

    @BeforeEach
    void setUp() {
        admin = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");
    }

    @Test
    void plainUserCannotCreateVenue() {
        String user = accessTokenFor(mvc, randomUid());

        assertThat(send(mvc.post().uri("/api/v1/venues"), user, VENUE)).hasStatus(403);
    }

    @Test
    void createThenGetRoundTripsVenueAndSpaces() {
        String venueId = createVenue(admin);
        assertThat(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, SPACE)).hasStatus(201);

        // Any signed-in user can read a venue.
        String reader = accessTokenFor(mvc, randomUid());
        assertThat(get("/api/v1/venues/" + venueId, reader))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.name").isEqualTo("Amber Room"),
                        json -> json.assertThat().extractingPath("$.latitude").isEqualTo(12.9784),
                        json -> json.assertThat().extractingPath("$.spaces[0].name").isEqualTo("Main floor"),
                        json -> json.assertThat().extractingPath("$.spaces[0].capacity").isEqualTo(60));
    }

    @Test
    void postgisLocationIsDerivedFromLatLng() {
        String venueId = createVenue(admin);

        String point = jdbc.queryForObject(
                "SELECT ST_AsText(location::geometry) FROM venues WHERE id = ?", String.class, UUID.fromString(venueId));

        assertThat(point).isEqualTo("POINT(77.6408 12.9784)");
    }

    @Test
    void updatingVenueMovesItsLocation() {
        String venueId = createVenue(admin);

        assertThat(send(mvc.put().uri("/api/v1/venues/{id}", venueId), admin,
                VENUE.replace("12.9784", "12.9352").replace("77.6408", "77.6245"))).hasStatusOk();

        assertThat(jdbc.queryForObject("SELECT ST_AsText(location::geometry) FROM venues WHERE id = ?",
                String.class, UUID.fromString(venueId))).isEqualTo("POINT(77.6245 12.9352)");
    }

    @Test
    void nonOwnerGets404OnWritesEvenWithVenueAdminRole() {
        String venueId = createVenue(admin);
        String otherAdmin = accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN");

        assertThat(send(mvc.put().uri("/api/v1/venues/{id}", venueId), otherAdmin, VENUE)).hasStatus(404);
        assertThat(mvc.delete().uri("/api/v1/venues/{id}", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(otherAdmin))).hasStatus(404);
        assertThat(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), otherAdmin, SPACE)).hasStatus(404);
    }

    @Test
    void invalidVenueIsRejectedWithFieldErrors() {
        String invalid = """
                {"name":"","addressLine":"x","city":"x","latitude":95,"longitude":77}
                """;

        assertThat(send(mvc.post().uri("/api/v1/venues"), admin, invalid))
                .hasStatus(400)
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.errors.name").isNotNull(),
                        json -> json.assertThat().extractingPath("$.errors.latitude").isNotNull());
    }

    @Test
    void invalidSpaceIsRejected() {
        String venueId = createVenue(admin);
        String invalid = """
                {"name":"","capacity":0}
                """;

        assertThat(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, invalid))
                .hasStatus(400)
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.errors.name").isNotNull(),
                        json -> json.assertThat().extractingPath("$.errors.capacity").isNotNull());
    }

    @Test
    void databaseRejectsOutOfRangeCoordinatesEvenWithoutTheApi() {
        UUID owner = jdbc.queryForObject("SELECT id FROM users LIMIT 1", UUID.class);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO venues (owner_id, name, address_line, city, latitude, longitude)
                VALUES (?, 'x', 'x', 'x', 91, 0)
                """, owner)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updatingSpaceChangesNameAndCapacity() {
        String venueId = createVenue(admin);
        String spaceId = read(body(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, SPACE)), "$.id");

        assertThat(send(mvc.put().uri("/api/v1/venues/{v}/spaces/{s}", venueId, spaceId), admin, """
                {"name":"Rooftop","capacity":80,"soundPolicy":"ACOUSTIC_ONLY","soundCurfew":"22:00"}
                """))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.name").isEqualTo("Rooftop"),
                        json -> json.assertThat().extractingPath("$.capacity").isEqualTo(80),
                        json -> json.assertThat().extractingPath("$.soundPolicy").isEqualTo("ACOUSTIC_ONLY"),
                        json -> json.assertThat().extractingPath("$.soundCurfew").isEqualTo("22:00"),
                        json -> json.assertThat().extractingPath("$.houseRules").isNull());
    }

    @Test
    void deletingVenueCascadesToSpaces() {
        String venueId = createVenue(admin);
        String spaceId = read(body(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, SPACE)), "$.id");

        assertThat(mvc.delete().uri("/api/v1/venues/{id}", venueId)
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))).hasStatus(204);

        assertThat(get("/api/v1/venues/" + venueId, admin)).hasStatus(404);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM spaces WHERE id = ?",
                Integer.class, UUID.fromString(spaceId))).isZero();
    }

    @Test
    void myVenuesListsOnlyOwnVenuesWithSpaceCounts() {
        String first = createVenue(admin);
        createVenue(admin);
        send(mvc.post().uri("/api/v1/venues/{id}/spaces", first), admin, SPACE);
        send(mvc.post().uri("/api/v1/venues/{id}/spaces", first), admin, SPACE.replace("Main floor", "Rooftop"));
        createVenue(accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN"));

        assertThat(get("/api/v1/me/venues", admin))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.length()").isEqualTo(2),
                        json -> json.assertThat().extractingPath("$[0].id").isEqualTo(first),
                        json -> json.assertThat().extractingPath("$[0].spaceCount").isEqualTo(2),
                        json -> json.assertThat().extractingPath("$[1].spaceCount").isEqualTo(0));
    }

    @Test
    void newVenueStartsPendingWithItsHostingMode() {
        String venueId = createVenue(admin);

        assertThat(get("/api/v1/venues/" + venueId, admin))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.fssaiNumber").isEqualTo(FSSAI),
                        json -> json.assertThat().extractingPath("$.hostingMode").isEqualTo("OPEN"),
                        json -> json.assertThat().extractingPath("$.verificationStatus").isEqualTo("PENDING"),
                        json -> json.assertThat().extractingPath("$.rejectionReason").isNull());
    }

    @Test
    void eachSpaceHasItsOwnSoundPolicyCurfewAndRules() {
        String venueId = createVenue(admin);
        send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, SPACE);
        send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, """
                {"name":"Rooftop","capacity":30,"soundPolicy":"ACOUSTIC_ONLY"}
                """);

        assertThat(get("/api/v1/venues/" + venueId, admin))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.spaces[0].soundPolicy").isEqualTo("AMPLIFIED_ALLOWED"),
                        json -> json.assertThat().extractingPath("$.spaces[0].soundCurfew").isEqualTo("23:00"),
                        json -> json.assertThat().extractingPath("$.spaces[0].houseRules").isEqualTo("18+ after 9 PM"),
                        // Curfew and house rules are optional.
                        json -> json.assertThat().extractingPath("$.spaces[1].soundPolicy").isEqualTo("ACOUSTIC_ONLY"),
                        json -> json.assertThat().extractingPath("$.spaces[1].soundCurfew").isNull(),
                        json -> json.assertThat().extractingPath("$.spaces[1].houseRules").isNull(),
                        // Nothing sound-related is left on the venue itself.
                        json -> json.assertThat().doesNotHavePath("$.soundPolicy"));
    }

    @Test
    void fssaiNumberMustBeExactly14Digits() {
        for (String bad : new String[] {"1234567890123", "123456789012345", "1234567890123A", ""}) {
            assertThat(send(mvc.post().uri("/api/v1/venues"), admin, VENUE.replace(FSSAI, bad)))
                    .hasStatus(400)
                    .bodyJson().extractingPath("$.errors.fssaiNumber").isNotNull();
        }
    }

    @Test
    void unknownOrMissingPolicyValuesAreRejected() {
        String venueId = createVenue(admin);

        assertThat(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin,
                SPACE.replace("\"AMPLIFIED_ALLOWED\"", "\"LOUD\""))).hasStatus(400);
        assertThat(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, """
                {"name":"Rooftop","capacity":30}
                """)).hasStatus(400).bodyJson().extractingPath("$.errors.soundPolicy").isNotNull();
        assertThat(send(mvc.post().uri("/api/v1/venues"), admin,
                VENUE.replace(",\"hostingMode\":\"OPEN\"", ""))).hasStatus(400);
    }

    @Test
    void verificationStatusCannotBeSetThroughTheApi() {
        MvcTestResult result = send(mvc.post().uri("/api/v1/venues"), admin,
                VENUE.replace("\"name\":", "\"verificationStatus\":\"VERIFIED\",\"name\":"));

        assertThat(result).hasStatus(201)
                .bodyJson().extractingPath("$.verificationStatus").isEqualTo("PENDING");
    }

    @Test
    void changingFssaiNumberResetsVerificationButOtherEditsDoNot() {
        String venueId = createVenue(admin);
        markVerified(venueId);

        assertThat(send(mvc.put().uri("/api/v1/venues/{id}", venueId), admin,
                VENUE.replace("Amber Room", "Amber Room & Kitchen")))
                .hasStatusOk()
                .bodyJson().extractingPath("$.verificationStatus").isEqualTo("VERIFIED");

        jdbc.update("UPDATE venues SET verification_status = 'REJECTED', rejection_reason = 'Blurry license' "
                + "WHERE id = ?", UUID.fromString(venueId));
        assertThat(send(mvc.put().uri("/api/v1/venues/{id}", venueId), admin,
                VENUE.replace(FSSAI, "99999999999999")))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.verificationStatus").isEqualTo("PENDING"),
                        json -> json.assertThat().extractingPath("$.rejectionReason").isNull());
    }

    @Test
    void sameFssaiNumberCanBePendingTwiceButVerifiedOnlyOnce() {
        String first = createVenue(admin);
        String second = createVenue(accessTokenWithRole(mvc, randomUid(), "VENUE_ADMIN"));
        String fssai = "55555555555555";
        jdbc.update("UPDATE venues SET fssai_number = ? WHERE id IN (?, ?)",
                fssai, UUID.fromString(first), UUID.fromString(second));

        markVerified(first);

        assertThatThrownBy(() -> markVerified(second)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsUnknownPolicyValuesEvenWithoutTheApi() {
        String venueId = createVenue(admin);
        String spaceId = read(body(send(mvc.post().uri("/api/v1/venues/{id}/spaces", venueId), admin, SPACE)), "$.id");

        assertThatThrownBy(() -> jdbc.update("UPDATE spaces SET sound_policy = 'LOUD' WHERE id = ?",
                UUID.fromString(spaceId))).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE venues SET verification_status = 'MAYBE' WHERE id = ?",
                UUID.fromString(venueId))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void myVenuesShowsVerificationStatus() {
        String venueId = createVenue(admin);
        markVerified(venueId);

        assertThat(get("/api/v1/me/venues", admin))
                .hasStatusOk()
                .bodyJson().extractingPath("$[0].verificationStatus").isEqualTo("VERIFIED");
    }

    private void markVerified(String venueId) {
        jdbc.update("UPDATE venues SET verification_status = 'VERIFIED' WHERE id = ?", UUID.fromString(venueId));
    }

    private String createVenue(String token) {
        MvcTestResult result = send(mvc.post().uri("/api/v1/venues"), token, VENUE);
        assertThat(result).hasStatus(201);
        return read(body(result), "$.id");
    }

    private MvcTestResult send(MockMvcTester.MockMvcRequestBuilder request, String token, String json) {
        return request.header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }
}
