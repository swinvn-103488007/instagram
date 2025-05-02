package com.engineerpro.example.redis.service;

import com.engineerpro.example.redis.dto.AuthenticationResponse;
import com.engineerpro.example.redis.dto.oauth_login_request.Oauth2LoginRequest;
import com.engineerpro.example.redis.dto.PasswordLoginRequest;
import com.engineerpro.example.redis.dto.RegisterRequest;

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
