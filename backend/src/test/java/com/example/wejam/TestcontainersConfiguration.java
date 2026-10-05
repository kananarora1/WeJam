package com.example.wejam;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // Same images as infra/docker-compose.yml so tests and local dev run identical versions.
    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");
    // MinIO no longer publishes free images; Chainguard's build is the same server.
    private static final DockerImageName MINIO_IMAGE =
            DockerImageName.parse("cgr.dev/chainguard/minio:latest").asCompatibleSubstituteFor("minio/minio");
    private static final String PRIVATE_BUCKET = "wejam-private";

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(POSTGIS_IMAGE);
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(REDIS_IMAGE).withExposedPorts(6379);
    }

    /** Started here (not lazily) so the bucket exists before the app's storage beans are used. */
    @Bean
    MinIOContainer minioContainer() {
        MinIOContainer minio = new MinIOContainer(MINIO_IMAGE).withUserName("wejam").withPassword("wejam-dev-secret");
        minio.start();
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(minio.getS3URL()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(minio.getUserName(), minio.getPassword())))
                .forcePathStyle(true)
                .build()) {
            s3.createBucket(b -> b.bucket(PRIVATE_BUCKET));
        }
        return minio;
    }

    /** Spring Boot has no S3 "service connection", so point the storage properties at the container ourselves. */
    @Bean
    DynamicPropertyRegistrar storageProperties(MinIOContainer minio) {
        return registry -> {
            registry.add("wejam.storage.endpoint", minio::getS3URL);
            registry.add("wejam.storage.public-endpoint", minio::getS3URL);
            registry.add("wejam.storage.access-key", minio::getUserName);
            registry.add("wejam.storage.secret-key", minio::getPassword);
            registry.add("wejam.storage.private-bucket", () -> PRIVATE_BUCKET);
        };
    }
}
