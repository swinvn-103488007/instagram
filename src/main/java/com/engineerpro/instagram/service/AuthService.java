package com.engineerpro.instagram.service;

import com.engineerpro.instagram.dto.AuthenticationResponse;
import com.engineerpro.instagram.dto.oauth_login_request.Oauth2LoginRequest;
import com.engineerpro.instagram.dto.PasswordLoginRequest;
import com.engineerpro.instagram.dto.RegisterRequest;

import java.util.UUID;

public interface AuthService {
  public void signUp(RegisterRequest registerRequest);
//  public AuthenticationResponse login(PasswordLoginRequest passwordLoginRequest);

  public AuthenticationResponse loginWithPassword(PasswordLoginRequest passwordLoginRequest);

  public AuthenticationResponse loginOauth(Oauth2LoginRequest oauth2LoginRequest);
  public void verifyAccount(String token);
  public void logout(UUID userId);
  public String refreshToken(String refreshToken);
}
