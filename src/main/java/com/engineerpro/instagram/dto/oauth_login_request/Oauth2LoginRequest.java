package com.engineerpro.instagram.dto.oauth_login_request;

import com.engineerpro.instagram.dto.oauth_profile.GoogleProfile;
import com.engineerpro.instagram.dto.oauth_profile.OauthProfile;
import lombok.Data;

@Data
public abstract class Oauth2LoginRequest {
  private String provider;
  private String providerId;
  public static Oauth2LoginRequest create(OauthProfile profile) {
    String profileType = profile.getClass().getSimpleName();
    return switch (profileType) {
      case "GoogleProfile" -> createGoogleLoginRequest((GoogleProfile) profile);
      default -> throw new IllegalArgumentException("Unsupported profile type: " + profileType);
    };
  }

  private static GoogleLoginRequest createGoogleLoginRequest(GoogleProfile googleProfile) {
    GoogleLoginRequest request = new GoogleLoginRequest();
    request.setProvider("google");
    request.setProviderId(googleProfile.getSub());
    request.setEmail(googleProfile.getEmail());
    request.setName(googleProfile.getName());
    request.setPicture(googleProfile.getPicture());
    return request;
  }
}
