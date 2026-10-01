package com.example.wejam;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static com.example.wejam.TestAuth.accessTokenFor;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.randomUid;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class WeJamApplicationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void anonymousHealthShowsOnlyStatus() {
        assertThat(mvc.get().uri("/actuator/health"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.status").isEqualTo("UP"),
                        json -> json.assertThat().doesNotHavePath("$.components"));
    }

    @Test
    void authenticatedHealthShowsDbAndRedisUp() {
        String token = accessTokenFor(mvc, randomUid());

        assertThat(mvc.get().uri("/actuator/health").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.status").isEqualTo("UP"),
                        json -> json.assertThat().extractingPath("$.components.db.status").isEqualTo("UP"),
                        json -> json.assertThat().extractingPath("$.components.redis.status").isEqualTo("UP"));
    }

    @Test
    void flywayMigrationsAppliedAndPostgisIsEnabled() {
        // Every migration on the classpath is applied (no version pinned, so new migrations don't break this).
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().pending()).isEmpty();

        String postgisVersion = jdbc.queryForObject("SELECT PostGIS_Version()", String.class);
        assertThat(postgisVersion).startsWith("3.5");
    }
}
