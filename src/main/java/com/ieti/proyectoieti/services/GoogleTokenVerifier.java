package com.ieti.proyectoieti.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleTokenVerifier {

  @Value("${spring.security.oauth2.client.registration.google.client-id:}")
  private String clientId;

  private final GoogleIdTokenVerifier verifier;

  public GoogleTokenVerifier(
      @Value("${spring.security.oauth2.client.registration.google.client-id:}") String clientId) {
    this.clientId = clientId;
    this.verifier =
        new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build();
  }

  /**
   * Verifies a Google ID token and returns the token payload
   *
   * @param idTokenString The ID token string from Google Sign-In
   * @return GoogleIdToken.Payload containing user information
   * @throws GeneralSecurityException if token verification fails
   * @throws IOException if there's a network error
   */
  public GoogleIdToken.Payload verify(String idTokenString)
      throws GeneralSecurityException, IOException {
    if (idTokenString == null || idTokenString.isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }

    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken != null) {
      return idToken.getPayload();
    } else {
      throw new SecurityException("Invalid ID token");
    }
  }

  /**
   * Verifies a Google ID token and returns whether it's valid
   *
   * @param idTokenString The ID token string from Google Sign-In
   * @return true if the token is valid, false otherwise
   */
  public boolean isValid(String idTokenString) {
    try {
      return verify(idTokenString) != null;
    } catch (Exception e) {
      return false;
    }
  }
}
