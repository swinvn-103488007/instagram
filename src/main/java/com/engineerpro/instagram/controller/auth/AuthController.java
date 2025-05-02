package com.engineerpro.instagram.controller.auth;

import com.engineerpro.instagram.dto.*;
import com.engineerpro.instagram.dto.oauth_login_request.RegisteredUserOauth2LoginRequest;
import com.engineerpro.instagram.dto.oauth_login_request.Oauth2LoginRequest;
import com.engineerpro.instagram.dto.oauth_profile.GoogleProfile;
import com.engineerpro.instagram.dto.oauth_profile.OauthProfile;
import com.engineerpro.instagram.service.AuthService;
import com.engineerpro.instagram.service.OAuth2Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@Slf4j
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final OAuth2Service oAuth2Service;
  @GetMapping("/inspect")
  public ResponseEntity<UserPrincipal> inspect(Authentication authentication) {
    log.info(String.format("authentication principal %s", authentication.getPrincipal().toString()));
    return ResponseEntity.ok().body((UserPrincipal) authentication.getPrincipal());
  }

  @GetMapping("/check-has-admin-role")
  @PreAuthorize("hasRole('ADMIN')")
  public Object sayHello(Authentication authentication) {
    return ResponseEntity.ok().body("has admin role");
  }

  @GetMapping("/check-has-user-role")
  @PreAuthorize("hasRole('ROLE_USER')")
  public Object getPrincipal(Authentication authentication) {
    return ResponseEntity.ok().body("has user role");
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(@RequestBody PasswordLoginRequest passwordLoginRequest, HttpServletResponse response) {
    AuthenticationResponse authenticationResponse = authService.loginWithPassword(passwordLoginRequest);
    this.setRefreshTokenCookie(response, authenticationResponse.getRefreshToken());
    return ResponseEntity.ok().body(authenticationResponse);
  }

  @PostMapping("/oauth/login")
  public ResponseEntity<AuthenticationResponse> login(@RequestBody RegisteredUserOauth2LoginRequest oauth2LoginRequest, HttpServletResponse response) {
    AuthenticationResponse authenticationResponse = authService.loginOauth(oauth2LoginRequest);
    this.setRefreshTokenCookie(response, authenticationResponse.getRefreshToken());
    return ResponseEntity.ok().body(authenticationResponse);
  }

  // NOTE: Have to cache and validate the accessToken once it is acquired
  @PostMapping("/oauth/callback")
  public ResponseEntity<AuthenticationResponse> oauthCallback(@RequestParam String accessToken, @RequestParam String provider) {
    OauthProfile profile = oAuth2Service.authenticateAndFetchProfile(accessToken, provider);

    Oauth2LoginRequest loginRequest = Oauth2LoginRequest.create(profile);
    return ResponseEntity.ok().body(this.authService.loginOauth(loginRequest));
  }

  @PostMapping("/oauth/auth-url")
  public ResponseEntity<OAuthUrlResponse> getOAuthUrl(@RequestParam String provider) {
    return ResponseEntity.ok().body(oAuth2Service.generateAuthUrl(provider));
  }

  @PostMapping("/sign-up")
  public ResponseEntity<String> signUp(@RequestBody RegisterRequest registerRequest) {
    authService.signUp(registerRequest);
    return new ResponseEntity<>("User Registration Successful", OK);
  }

  @PostMapping("/account-verification/{token}")
  public ResponseEntity<String> verifyAccount(@PathVariable String token) {
    authService.verifyAccount(token);
    return new ResponseEntity<>("Account Activated Successfully", OK);
  }

  @PostMapping("/refresh/token")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    // Get refresh token from httpOnly cookie
    Cookie[] cookies = request.getCookies();
    String refreshToken = null;

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token not found");
    }

    try {
      // Get new access token
      String newAccessToken = authService.refreshToken(refreshToken);
      return ResponseEntity.ok()
          .body(new HashMap<String, String>() {{
            put("accessToken", newAccessToken);
          }});

    } catch (RuntimeException e) {
      // Clear the invalid refresh token cookie
      Cookie cookie = new Cookie("refreshToken", null);
      cookie.setHttpOnly(true);
      cookie.setSecure(true); // for HTTPS
      cookie.setPath("/");
      cookie.setMaxAge(0);
      response.addCookie(cookie);

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
  }

  @PostMapping("/logout/{userId}")
  public ResponseEntity<String> logout(@PathVariable UUID userId) {
    authService.logout(userId);
    return new ResponseEntity<>("Log out successfully", OK);
  }

  private String extractProviderId(OauthProfile profile) {
    if (profile instanceof GoogleProfile googleProfile) {
      return googleProfile.getSub();
    }
    throw new UnsupportedOperationException("Unsupported OAuth profile type: " + profile.getClass().getName());
  }

  // Method to set refresh token as httpOnly cookie when logging in
  private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true); // for HTTPS
    cookie.setPath("/");
    cookie.setMaxAge(30 * 7 * 24 * 60 * 60); // 7 days
    response.addCookie(cookie);
  }


}
