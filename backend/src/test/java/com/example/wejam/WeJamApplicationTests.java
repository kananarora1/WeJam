package com.example.wejam;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class WeJamApplicationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void healthIsUpWithDbAndRedis() {
        assertThat(mvc.get().uri("/actuator/health"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.status").isEqualTo("UP"),
                        json -> json.assertThat().extractingPath("$.components.db.status").isEqualTo("UP"),
                        json -> json.assertThat().extractingPath("$.components.redis.status").isEqualTo("UP"));
    }

    @Test
    void flywayAppliedBaselineAndPostgisIsEnabled() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");

        String postgisVersion = jdbc.queryForObject("SELECT PostGIS_Version()", String.class);
        assertThat(postgisVersion).startsWith("3.5");
    }
}
