package com.example.wejam.host.controller;

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
class HostMembershipControllerTest {

    private static final String GROUP = """
            {"type":"GROUP","groupKind":"BAND","groupName":"The Low Notes","genres":[],"mediaLinks":[]}
            """;

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcTemplate jdbc;

    String owner;
    String groupId;

    @BeforeEach
    void setUp() {
        owner = accessTokenWithRole(mvc, randomUid(), "HOST");
        groupId = read(body(send(mvc.put().uri("/api/v1/me/host-profile"), owner, GROUP)), "$.id");
        setName(owner, "Kanan");
    }

    @Test
    void invitedUserAcceptsAndAppearsOnThePublicProfile() {
        Person rhea = person("Rhea M.");

        assertThat(invite(rhea.phone)).hasStatus(201).bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.userId").isEqualTo(rhea.id),
                json -> json.assertThat().extractingPath("$.maskedPhone").asString().contains("•").doesNotContain(rhea.phone.substring(5, 10)));
        // Pending invites are not public.
        assertThat(get("/api/v1/host-profiles/" + groupId, rhea.token)).bodyJson()
                .extractingPath("$.members.length()").isEqualTo(1);

        assertThat(post("/api/v1/me/host-invites/" + groupId + "/accept", rhea.token)).hasStatus(204);

        assertThat(get("/api/v1/host-profiles/" + groupId, rhea.token))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.members[0].displayName").isEqualTo("Kanan"),
                        json -> json.assertThat().extractingPath("$.members[0].admin").isEqualTo(true),
                        json -> json.assertThat().extractingPath("$.members[1].displayName").isEqualTo("Rhea M."),
                        json -> json.assertThat().extractingPath("$.members[1].admin").isEqualTo(false));
    }

    @Test
    void inviteeSeesWhoInvitedThemAndOwnerSeesOnlyAMaskedPhone() {
        Person rhea = person("Rhea M.");
        invite(rhea.phone);

        assertThat(get("/api/v1/me/host-invites", rhea.token)).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$[0].hostProfileId").isEqualTo(groupId),
                json -> json.assertThat().extractingPath("$[0].groupName").isEqualTo("The Low Notes"),
                json -> json.assertThat().extractingPath("$[0].groupKind").isEqualTo("BAND"),
                json -> json.assertThat().extractingPath("$[0].invitedByName").isEqualTo("Kanan"));
        assertThat(get("/api/v1/me/host-profile/invites", owner)).hasStatusOk().bodyJson().satisfies(
                json -> json.assertThat().extractingPath("$.length()").isEqualTo(1),
                json -> json.assertThat().doesNotHavePath("$[0].displayName"));
    }

    @Test
    void inviteRejectsUnknownNumbersYourselfDuplicatesAndIndividuals() {
        assertThat(invite("+919999000011")).hasStatus(404)
                .bodyJson().extractingPath("$.detail").asString().contains("sign up");
        assertThat(invite("12345")).hasStatus(400);

        String ownerPhone = jdbc.queryForObject("SELECT phone FROM users WHERE id = ?", String.class, idOf(owner));
        assertThat(invite(ownerPhone)).hasStatus(400);

        Person rhea = person("Rhea M.");
        invite(rhea.phone);
        assertThat(invite(rhea.phone)).hasStatus(409);
        assertThat(memberCount()).isEqualTo(1); // the failed duplicate gave its reserved slot back

        String soloHost = accessTokenWithRole(mvc, randomUid(), "HOST");
        send(mvc.put().uri("/api/v1/me/host-profile"), soloHost,
                "{\"type\":\"INDIVIDUAL\",\"genres\":[],\"mediaLinks\":[]}");
        assertThat(send(mvc.post().uri("/api/v1/me/host-profile/invites"), soloHost,
                "{\"phoneNumber\":\"" + person("Arjun").phone + "\"}")).hasStatus(409);
    }

    @Test
    void declineLeaveAndRemoveFreeTheSlot() {
        Person a = person("A");
        Person b = person("B");
        Person c = person("C");
        invite(a.phone);
        invite(b.phone);
        invite(c.phone);
        assertThat(memberCount()).isEqualTo(3);

        assertThat(post("/api/v1/me/host-invites/" + groupId + "/decline", a.token)).hasStatus(204);
        post("/api/v1/me/host-invites/" + groupId + "/accept", b.token);
        assertThat(mvc.delete().uri("/api/v1/me/host-memberships/{id}", groupId)
                .header(HttpHeaders.AUTHORIZATION, bearer(b.token))).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/me/host-profile/members/{userId}", c.id)
                .header(HttpHeaders.AUTHORIZATION, bearer(owner))).hasStatus(204);

        assertThat(memberCount()).isZero();
        // A declined user can be invited again.
        assertThat(invite(a.phone)).hasStatus(201);
    }

    @Test
    void acceptingTwiceOrSomeoneElsesInviteIs404() {
        Person rhea = person("Rhea M.");
        Person stranger = person("Stranger");
        invite(rhea.phone);

        assertThat(post("/api/v1/me/host-invites/" + groupId + "/accept", stranger.token)).hasStatus(404);
        assertThat(post("/api/v1/me/host-invites/" + groupId + "/accept", rhea.token)).hasStatus(204);
        assertThat(post("/api/v1/me/host-invites/" + groupId + "/accept", rhea.token)).hasStatus(404);
        // Leaving requires being an accepted member.
        assertThat(mvc.delete().uri("/api/v1/me/host-memberships/{id}", groupId)
                .header(HttpHeaders.AUTHORIZATION, bearer(stranger.token))).hasStatus(404);
    }

    @Test
    void groupIsCappedAtTenPeopleIncludingPendingInvites() {
        for (int i = 0; i < 9; i++) {
            assertThat(invite(person("M" + i).phone)).hasStatus(201);
        }

        assertThat(invite(person("Tenth").phone)).hasStatus(409)
                .bodyJson().extractingPath("$.detail").asString().contains("10 people");
    }

    @Test
    void concurrentInvitesNeverExceedTheCap() throws Exception {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            people.add(person("P" + i));
        }

        List<Callable<Integer>> invites = people.stream()
                .<Callable<Integer>>map(p -> () -> invite(p.phone).getResponse().getStatus())
                .toList();
        List<Integer> statuses = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(15)) {
            for (Future<Integer> f : pool.invokeAll(invites)) {
                statuses.add(f.get());
            }
        }

        assertThat(statuses).filteredOn(s -> s == 201).hasSize(9);
        assertThat(statuses).filteredOn(s -> s == 409).hasSize(6);
        assertThat(memberCount()).isEqualTo(9);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM host_group_members WHERE host_profile_id = ?",
                Integer.class, UUID.fromString(groupId))).isEqualTo(9);
    }

    @Test
    void groupWithMembersCannotSwitchToIndividualUntilEmpty() {
        Person rhea = person("Rhea M.");
        invite(rhea.phone);
        String individual = "{\"type\":\"INDIVIDUAL\",\"genres\":[],\"mediaLinks\":[]}";

        assertThat(send(mvc.put().uri("/api/v1/me/host-profile"), owner, individual)).hasStatus(409);

        mvc.delete().uri("/api/v1/me/host-profile/members/{userId}", rhea.id)
                .header(HttpHeaders.AUTHORIZATION, bearer(owner)).exchange();
        assertThat(send(mvc.put().uri("/api/v1/me/host-profile"), owner, individual)).hasStatusOk();
    }

    // --- helpers

    record Person(String id, String token, String phone) {
    }

    private Person person(String name) {
        String phone = "+91" + (6_000_000_000L + ThreadLocalRandom.current().nextLong(3_999_999_999L));
        String token = read(body(exchange(mvc, "fake:" + randomUid() + ":" + phone)), "$.accessToken");
        setName(token, name);
        return new Person(idOf(token).toString(), token, phone);
    }

    private UUID idOf(String token) {
        return UUID.fromString(read(body(get("/api/v1/me", token)), "$.id"));
    }

    private void setName(String token, String name) {
        send(mvc.patch().uri("/api/v1/me"), token, "{\"displayName\":\"" + name + "\"}");
    }

    private int memberCount() {
        return jdbc.queryForObject("SELECT member_count FROM host_profiles WHERE id = ?", Integer.class,
                UUID.fromString(groupId));
    }

    private MvcTestResult invite(String phone) {
        return send(mvc.post().uri("/api/v1/me/host-profile/invites"), owner, "{\"phoneNumber\":\"" + phone + "\"}");
    }

    private MvcTestResult post(String uri, String token) {
        return mvc.post().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    private MvcTestResult send(MockMvcTester.MockMvcRequestBuilder request, String token, String json) {
        return request.header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }
}
