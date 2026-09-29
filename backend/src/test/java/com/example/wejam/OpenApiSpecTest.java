package com.example.wejam;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.example.wejam.TestAuth.body;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regenerates the committed API contract (docs/api/openapi.yaml) on every test run; the mobile client is generated
 * from that file. A changed file in `git status` after `./mvnw test` means the API changed.
 */
@IntegrationTest
class OpenApiSpecTest {

    // Surefire runs with backend/ as the working directory.
    private static final Path SPEC_FILE = Path.of("..", "docs", "api", "openapi.yaml");

    @Autowired
    MockMvcTester mvc;

    @Test
    void writesSpecToDocs() throws IOException {
        String yaml = body(mvc.get().uri("/v3/api-docs.yaml").exchange());
        assertThat(yaml).startsWith("openapi: 3.0");

        Files.createDirectories(SPEC_FILE.getParent());
        Files.writeString(SPEC_FILE, yaml);
    }

    @Test
    void specDescribesAuthApiForClientGeneration() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatusOk()
                .bodyJson()
                .satisfies(
                        json -> json.assertThat().extractingPath("$.paths['/api/v1/auth/token'].post.operationId")
                                .isEqualTo("exchange"),
                        json -> json.assertThat().extractingPath("$.paths['/api/v1/auth/token'].post.security")
                                .asArray().isEmpty(),
                        json -> json.assertThat().extractingPath("$.paths['/api/v1/me'].get.operationId")
                                .isEqualTo("me"),
                        json -> json.assertThat().extractingPath("$.paths['/api/v1/me'].patch.operationId")
                                .isEqualTo("updateProfile"),
                        json -> json.assertThat().extractingPath("$.paths['/api/v1/me/roles'].post.operationId")
                                .isEqualTo("addRole"),
                        json -> json.assertThat().extractingPath("$.security[0].bearerAuth").isNotNull(),
                        json -> json.assertThat().extractingPath("$.components.securitySchemes.bearerAuth.scheme")
                                .isEqualTo("bearer"),
                        json -> json.assertThat().extractingPath("$.components.schemas.MeResponse.required")
                                .asArray().containsExactlyInAnyOrder("id", "phone", "displayName", "roles"),
                        json -> json.assertThat().extractingPath("$.components.schemas.MeResponse.properties.phone.nullable")
                                .isEqualTo(true),
                        json -> json.assertThat().extractingPath("$.components.schemas.MeResponse.properties.displayName.nullable")
                                .isEqualTo(true),
                        json -> json.assertThat().extractingPath("$.servers[0].url")
                                .isEqualTo("http://localhost:8080"));
    }

    @Test
    void swaggerUiIsPublic() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatusOk();
    }
}
