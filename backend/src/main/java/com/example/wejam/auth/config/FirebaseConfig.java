package com.example.wejam.auth.config;

import com.example.wejam.auth.firebase.FirebaseAdminTokenVerifier;
import com.example.wejam.auth.firebase.FirebaseTokenVerifier;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Real Firebase verification; active unless the fake verifier is enabled.
 * Credentials come from GOOGLE_APPLICATION_CREDENTIALS (service-account JSON kept outside the repo).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "wejam.auth.fake-firebase", havingValue = "false", matchIfMissing = true)
@EnableConfigurationProperties(FirebaseProperties.class)
class FirebaseConfig {

    private static final String APP_NAME = "wejam";

    // Deleting on shutdown lets DevTools restarts re-initialise without "FirebaseApp already exists".
    @Bean(destroyMethod = "delete")
    FirebaseApp firebaseApp(FirebaseProperties properties) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials(properties))
                .setProjectId(properties.projectId())
                .build();
        return FirebaseApp.initializeApp(options, APP_NAME);
    }

    /**
     * The Google SDK reads GOOGLE_APPLICATION_CREDENTIALS from the OS environment only, which a .env file
     * imported by Spring can't provide, so an explicit path from Spring config wins when set.
     */
    private static GoogleCredentials credentials(FirebaseProperties properties) throws IOException {
        String file = properties.credentialsFile();
        if (file == null || file.isBlank()) {
            return GoogleCredentials.getApplicationDefault();
        }
        try (InputStream in = Files.newInputStream(Path.of(file.replaceFirst("^~", System.getProperty("user.home"))))) {
            return GoogleCredentials.fromStream(in);
        }
    }

    @Bean
    FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    FirebaseTokenVerifier firebaseTokenVerifier(FirebaseAuth firebaseAuth) {
        return new FirebaseAdminTokenVerifier(firebaseAuth);
    }
}
