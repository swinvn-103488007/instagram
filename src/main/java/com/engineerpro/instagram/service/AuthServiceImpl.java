package com.engineerpro.instagram.service;

import com.engineerpro.instagram.dto.AuthenticationResponse;
import com.engineerpro.instagram.dto.PasswordLoginRequest;
import com.engineerpro.instagram.dto.RegisterRequest;
import com.engineerpro.instagram.dto.oauth_login_request.GoogleLoginRequest;
import com.engineerpro.instagram.dto.oauth_login_request.Oauth2LoginRequest;
import com.engineerpro.example.redis.exception.*;
import com.engineerpro.example.redis.model.*;
import com.engineerpro.instagram.exception.*;
import com.engineerpro.instagram.model.*;
import com.engineerpro.instagram.repository.ProfileRepository;
import com.engineerpro.instagram.repository.RefreshTokenRepository;
import com.engineerpro.instagram.repository.UserRepository;
import com.engineerpro.instagram.repository.VerificationTokenRepository;
import com.engineerpro.instagram.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final MailService mailService;
  private final PasswordEncoder passwordEncoder;

  @Value("${spring.security.jwt.access-token.expiration}")
  private Long accessTokenExpireMillis;

  @Value("${spring.security.jwt.refresh-token.expiration}")
  private Long refreshTokenExpireMillis;

  @Value("${spring.security.verification-token.expiration}")
  private Long verificationTokenExpireMillis;

  @Override
  @Transactional
  public void signUp(RegisterRequest registerRequest) {

    if (userRepository.findByUsername(registerRequest.getEmail()).isPresent()) {
      throw new UsernameRegisteredException();
    }
    User user = new User();
    user.setUsername(registerRequest.getEmail());
    user.setId(UUID.randomUUID());
    user.setUsername(registerRequest.getEmail());
    user.setName(registerRequest.getDisplayName());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    user.setEnabled(false);

    Profile profile = Profile.builder()
        .userId(user.getId().toString())
        .username(user.getUsername())
        .displayName(user.getName())
        .profileImageUrl(user.getPicture())
        .build();
    userRepository.save(user);
    profileRepository.save(profile);
    String token = generateVerificationToken(user);
    mailService.sendMail(new NotificationEmail("Please Activate your Account",
        user.getUsername(), "Thank you for signing up to Spring Reddit, " +
        "please click on the below url to activate your account : " +
        "http://localhost:8080/api/auth/account-verification/" + token + ". This verification link will be expired in 15 minutes"));
  }

  @Override
  public AuthenticationResponse loginWithPassword(PasswordLoginRequest pwdLoginRequest) {
    User user = this.handlePasswordLogin(pwdLoginRequest);
    String accessToken = generateAccessToken(user);
    log.info(String.format("access token: %s", accessToken));
    String refreshToken = generateRefreshToken(user);

    saveAccessTokenToRedis(accessToken, user.getId().toString());

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .username(user.getUsername())
        .expiresAt(jwtProvider.extractExpirationTime(accessToken))
        .build();
  }

  @Override
  public AuthenticationResponse loginOauth(Oauth2LoginRequest oauth2LoginRequest) {
    User user = this.handleOauthLogin(oauth2LoginRequest);
    String accessToken = generateAccessToken(user);
    log.info(String.format("access token: %s", accessToken));
    String refreshToken = generateRefreshToken(user);

    saveAccessTokenToRedis(accessToken, user.getId().toString());

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .username(user.getUsername())
        .expiresAt(jwtProvider.extractExpirationTime(accessToken))
        .build();
  }

  @Override
  public void verifyAccount(String token) {
    Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
    verificationToken.orElseThrow(VerificationTokenNotFoundException::new);
    if (verificationToken.get().getExpiryDate().isBefore(Instant.now())) {
      throw new VerificationTokenExpiredException();
    }
    fetchUserAndEnable(verificationToken.get());
  }

  @Override
  public String refreshToken(String refreshToken) {
    // User user = refreshTokenRepository.findByToken(refreshToken).orElseThrow()
    RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElseThrow(RefreshTokenNotFoundException::new);
    return this.generateAccessToken(token.getUser());
  }

  @Transactional
  private void fetchUserAndEnable(VerificationToken verificationToken) {
    String username = verificationToken.getUser().getUsername();
    User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    this.activateUser(user);
    userRepository.save(user);
  }

  @Override
  public void logout(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).orElseThrow(RefreshTokenNotFoundException::new);
    refreshTokenRepository.delete(refreshToken);
    // Find and delete all access tokens for the user
    String accessTokenPattern = "access_token:" + userId + "*";
    Set<String> keys = redisTemplate.keys(accessTokenPattern);
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  private String generateVerificationToken(User user) {
    String token = UUID.randomUUID().toString();
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setExpiryDate(Instant.now().plusMillis(verificationTokenExpireMillis));
    verificationTokenRepository.save(verificationToken);
    return token;
  }

  private String generateRefreshToken(User user) {
    String tokenValue = UUID.randomUUID().toString();
    // Check for existing refresh token
    Optional<RefreshToken> existingToken = refreshTokenRepository.findByUserId(user.getId());

    RefreshToken refreshToken;
    if (existingToken.isPresent()) {
      refreshToken = existingToken.get();
      refreshToken.setExpirationTime(new Date(System.currentTimeMillis() + refreshTokenExpireMillis));
    } else {
      refreshToken = RefreshToken.builder()
          .token(tokenValue)
          .user(user)
          .expirationTime(new Date(System.currentTimeMillis() + refreshTokenExpireMillis))
          .build();
    }
    refreshTokenRepository.save(refreshToken);
    return refreshToken.getToken();
  }

  private User handlePasswordLogin(PasswordLoginRequest passwordLoginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            passwordLoginRequest.getUserEmail(),
            passwordLoginRequest.getPassword()
        )
    );
    return userRepository.findByUsername(passwordLoginRequest.getUserEmail()).orElseThrow(
        () -> new BadCredentialsException("Invalid email or password")
    );
  }

  private User handleOauthLogin(Oauth2LoginRequest oauth2LoginRequest) {
    // Find existing user or create a new one
    return userRepository.findByProviderAndProviderId(
        oauth2LoginRequest.getProvider(),
        oauth2LoginRequest.getProviderId()
    ).orElseGet(() -> {
      User newUser = createNewUser(oauth2LoginRequest);
      Profile profile = Profile.builder()
          .username(newUser.getUsername())
          .displayName(newUser.getName())
          .profileImageUrl(newUser.getPicture())
          .build();
      profileRepository.save(profile);
      return userRepository.save(newUser);
    });
  }

  private void saveAccessTokenToRedis(String accessToken, String userId) {
    String sid = jwtProvider.extractSidFromToken(accessToken);
    // Create Redis key
    String redisKey = String.format("accessToken:%s_%s", userId, sid);
    redisTemplate.opsForValue().set(redisKey, "VALID");
    redisTemplate.expire(redisKey, this.accessTokenExpireMillis, TimeUnit.MILLISECONDS);
    Set<String> accessTokenIds = redisTemplate.keys(redisKey);
    if (accessTokenIds != null) {
      for (String id : accessTokenIds) log.info(String.format("Saved access token id: %s", id));
    }
  }

  private String generateAccessToken(User user) {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("scope", "ROLE_USER");
    claims.put("userId", user.getId());
    claims.put("name", user.getName());

    // the id of the token should be generated and add to claims when we create the access token
    claims.put("sid", UUID.randomUUID().toString());

    claims.put("username", user.getUsername());
    // Email-password specific claim
    if (user.getProvider() == null) {
      return jwtProvider.generateAccessToken(user.getUsername(), claims);
    }
    // OAuth specific claim
    else {
      String subject = String.format("%s:%s", user.getProvider(), user.getProviderId());
      claims.put("sub", subject);
      return jwtProvider.generateAccessToken(subject, claims);
    }
  }

  private User createNewUser(Oauth2LoginRequest oauth2LoginRequest) {
    String requestType = oauth2LoginRequest.getClass().getSimpleName();
    return switch (requestType) {
      case "GoogleLoginRequest" -> this.createLoginGoogleUser((GoogleLoginRequest) oauth2LoginRequest);
      default -> throw new IllegalArgumentException("Unsupported profile type: " + requestType);
    };
  }

  private User createLoginGoogleUser(GoogleLoginRequest googleLoginRequest) {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setProvider(googleLoginRequest.getProvider());
    user.setProviderId(googleLoginRequest.getProviderId());
    user.setUsername(googleLoginRequest.getEmail());
    user.setName(googleLoginRequest.getName());
    user.setPicture(googleLoginRequest.getPicture());
    this.activateUser(user);
    return user;
  }

  private void activateUser(User user) {
    user.setEnabled(true);
    user.setAccountNonLocked(true);
    user.setCredentialsNonExpired(true);
    user.setAccountNonExpired(true);
  }
}

