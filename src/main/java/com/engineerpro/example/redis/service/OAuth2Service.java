package com.engineerpro.example.redis.service;

import com.engineerpro.example.redis.dto.oauth_profile.GoogleProfile;
import com.engineerpro.example.redis.dto.OAuthUrlResponse;
import com.engineerpro.example.redis.dto.oauth_profile.OauthProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {
  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;
  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String googleClientSecret;
  @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
  private String googleRedirectUri;
  @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
  private String googleUserInfoUri;

  public OAuthUrlResponse generateAuthUrl(String providerId) {
    if (providerId.equalsIgnoreCase("google")) {// Create Google OAuth2 URL with required parameters
      String url = new GoogleAuthorizationCodeRequestUrl(
          googleClientId,
          googleRedirectUri,
          Arrays.asList("email", "profile")) // Common scopes for user info
          .build();
      return OAuthUrlResponse.builder()
          .url(url)
          .build();
      // TODO: implement login with Facebook, GitHub,...
    }
    return OAuthUrlResponse.builder()
        .url("")
        .build();
  }

  public OauthProfile authenticateAndFetchProfile(String accessToken, String provider) {
    return switch (provider.toLowerCase()) {
      case "google" -> authenticateAndFetchGoogleProfile(accessToken);
      case "facebook" -> authenticateAndFetchFacebookProfile(accessToken);
      default -> throw new UnsupportedOperationException("Unsupported OAuth provider: " + provider);
    };
  }

  private GoogleProfile authenticateAndFetchGoogleProfile(String code) {
//    String accessToken = getGoogleAccessToken(code);
    return fetchGoogleUserProfile(code);
  }

  private String getGoogleAccessToken(String code) {
    try {
      return new GoogleAuthorizationCodeTokenRequest(
          new NetHttpTransport(),
          new GsonFactory(),
          googleClientId,
          googleClientSecret,
          code,
          googleRedirectUri
      ).execute().getAccessToken();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private GoogleProfile fetchGoogleUserProfile(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

    restTemplate.getInterceptors().add((request, body, execution) -> {
      request.getHeaders().set("Authorization", "Bearer " + accessToken);
      return execution.execute(request, body);
    });

    try {
      String response = restTemplate.getForEntity(googleUserInfoUri, String.class).getBody();
      return new ObjectMapper().readValue(response, GoogleProfile.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private OauthProfile authenticateAndFetchFacebookProfile(String code) {
    // TODO: Implement Facebook authentication
    throw new UnsupportedOperationException("Facebook authentication not implemented yet");
  }

}
