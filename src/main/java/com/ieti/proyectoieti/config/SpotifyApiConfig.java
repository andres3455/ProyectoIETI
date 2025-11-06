package com.ieti.proyectoieti.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

@Configuration
public class SpotifyApiConfig {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyApiConfig.class);

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri:http://127.0.0.1:8080/callback}")
    private String redirectUri;

    @Bean
    public SpotifyApi spotifyApi() {
        if (clientId == null || clientId.isEmpty() || clientId.equals("your_client_id_here")) {
            logger.error("Spotify Client ID is not configured properly!");
            throw new IllegalStateException("Spotify Client ID is not configured. Please set spotify.client.id in application.properties");
        }

        if (clientSecret == null || clientSecret.isEmpty() || clientSecret.equals("your_client_secret_here")) {
            logger.error("Spotify Client Secret is not configured properly!");
            throw new IllegalStateException("Spotify Client Secret is not configured. Please set spotify.client.secret in application.properties");
        }

        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(SpotifyHttpManager.makeUri(redirectUri))
                .build();
    }

    /**
     * Initializes Spotify API with client credentials authentication
     * This is suitable for server-to-server requests without user context
     */
    public void authenticateClientCredentials(SpotifyApi spotifyApi) {
        try {
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (Exception e) {
            logger.error("Failed to authenticate with Spotify API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to authenticate with Spotify API: " + e.getMessage(), e);
        }
    }
}