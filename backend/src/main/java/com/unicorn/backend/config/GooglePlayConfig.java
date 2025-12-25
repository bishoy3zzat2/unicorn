package com.unicorn.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Configuration class for Google Play Developer API integration.
 * Loads service account credentials and configures the AndroidPublisher client.
 */
@Slf4j
@Configuration
public class GooglePlayConfig {

    @Value("${google.play.package-name:com.unicorn.app}")
    private String packageName;

    @Value("${google.play.service-account-path:service-account.json}")
    private String serviceAccountPath;

    /**
     * Creates and configures the AndroidPublisher bean for Google Play API access.
     * 
     * @return Configured AndroidPublisher instance
     * @throws IOException              If service account file cannot be read
     * @throws GeneralSecurityException If there's a security configuration issue
     */
    @Bean
    public AndroidPublisher androidPublisher() throws IOException, GeneralSecurityException {
        try {
            ClassPathResource resource = new ClassPathResource(serviceAccountPath);

            if (!resource.exists()) {
                log.warn("Google Play service account file not found at: {}. " +
                        "Google Play verification will not work until file is provided.", serviceAccountPath);
                return createDummyPublisher();
            }

            try (InputStream credentialsStream = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(credentialsStream)
                        .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

                return new AndroidPublisher.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                        .setApplicationName("Unicorn Backend")
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to initialize AndroidPublisher: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a dummy publisher for development when credentials are missing.
     * This allows the application to start without credentials but will fail on
     * actual API calls.
     */
    private AndroidPublisher createDummyPublisher() throws GeneralSecurityException, IOException {
        return new AndroidPublisher.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> {
                })
                .setApplicationName("Unicorn Backend (No Credentials)")
                .build();
    }

    /**
     * Gets the configured package name for the Android application.
     * 
     * @return The package name
     */
    public String getPackageName() {
        return packageName;
    }
}
