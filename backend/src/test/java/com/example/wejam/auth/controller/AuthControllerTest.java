package com.example.wejam.auth.controller;

import com.example.wejam.IntegrationTest;
import com.example.wejam.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.exchange;
import static com.example.wejam.TestAuth.randomUid;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class AuthControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JwtDecoder jwtDecoder;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    AuthService authService;

    @Test
    void firstExchangeCreatesUserWithUserRoleAndReturnsJwt() {
        String uid = randomUid();

        MvcTestResult result = exchange(mvc, "fake:" + uid + ":+919876543210");

        assertThat(result).hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.tokenType").isEqualTo("Bearer"),
                        json -> json.assertThat().extractingPath("$.expiresIn").isEqualTo(3600));

        Jwt jwt = jwtDecoder.decode(read(body(result), "$.accessToken"));
        UUID userId = jdbc.queryForObject(
                "SELECT id FROM users WHERE firebase_uid = ? AND phone = '+919876543210'", UUID.class, uid);
        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("wejam");
    }

    @Test
    void repeatExchangeReturnsSameUser() {
        String uid = randomUid();

        String firstSubject = subjectOf(exchange(mvc, "fake:" + uid));
        String secondSubject = subjectOf(exchange(mvc, "fake:" + uid));

        assertThat(secondSubject).isEqualTo(firstSubject);
        assertThat(countUsers(uid)).isEqualTo(1);
    }

    @Test
    void concurrentFirstLoginsCreateExactlyOneUser() throws Exception {
        String uid = randomUid();
        int attempts = 10;

        List<Callable<String>> logins = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            logins.add(() -> jwtDecoder.decode(authService.exchange("fake:" + uid).accessToken()).getSubject());
        }
        Set<String> subjects = new HashSet<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(attempts)) {
            for (Future<String> f : pool.invokeAll(logins)) {
                subjects.add(f.get());
            }
        }

        assertThat(subjects).hasSize(1);
        assertThat(countUsers(uid)).isEqualTo(1);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM user_roles r JOIN users u ON u.id = r.user_id WHERE u.firebase_uid = ?
                """, Integer.class, uid)).isEqualTo(1);
    }

    @Test
    void invalidFirebaseTokenReturns401Problem() {
        assertThat(exchange(mvc, "not-a-valid-token"))
                .hasStatus(401)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.detail").isEqualTo("Firebase ID token is invalid or expired");
    }

    @Test
    void blankFirebaseTokenReturns400WithFieldError() {
        assertThat(exchange(mvc, ""))
                .hasStatus(400)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.errors.firebaseIdToken").isNotNull();
    }

    private String subjectOf(MvcTestResult result) {
        assertThat(result).hasStatusOk();
        return jwtDecoder.decode(read(body(result), "$.accessToken")).getSubject();
    }

    private int countUsers(String uid) {
        return jdbc.queryForObject("SELECT count(*) FROM users WHERE firebase_uid = ?", Integer.class, uid);
    }

    @Test
    void allowListedPhoneGetsPlatformAdminAtLogin() {
        // Same identity as the verification tests: users.phone is unique.
        MvcTestResult result = exchange(mvc, "fake:platform-admin:+919999999901");

        assertThat(jwtDecoder.decode(read(body(result), "$.accessToken")).getClaimAsStringList("roles"))
                .contains("PLATFORM_ADMIN");
    }

    @Test
    void platformAdminIsRevokedAtLoginWhenNoLongerListed() {
        String uid = randomUid();
        exchange(mvc, "fake:" + uid + ":+919000011111");
        jdbc.update("INSERT INTO user_roles (user_id, role) SELECT id, 'PLATFORM_ADMIN' FROM users WHERE firebase_uid = ?", uid);

        MvcTestResult again = exchange(mvc, "fake:" + uid);

        assertThat(jwtDecoder.decode(read(body(again), "$.accessToken")).getClaimAsStringList("roles"))
                .doesNotContain("PLATFORM_ADMIN");
    }

    @Test
    void recreatedFirebaseAccountWithSamePhoneKeepsTheExistingUser() {
        String phone = "+91" + (7_000_000_000L + ThreadLocalRandom.current().nextLong(999_999_999L));
        String oldUid = randomUid();
        String firstSubject = subjectOf(exchange(mvc, "fake:" + oldUid + ":" + phone));

        // Same phone, new Firebase uid (account deleted and recreated in Firebase).
        String newUid = randomUid();
        MvcTestResult again = exchange(mvc, "fake:" + newUid + ":" + phone);

        assertThat(again).hasStatusOk();
        assertThat(subjectOf(again)).isEqualTo(firstSubject);
        assertThat(jdbc.queryForObject("SELECT firebase_uid FROM users WHERE phone = ?", String.class, phone))
                .isEqualTo(newUid);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE phone = ?", Integer.class, phone)).isEqualTo(1);
    }
}

