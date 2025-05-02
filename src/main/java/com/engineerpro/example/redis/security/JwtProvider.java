package com.engineerpro.example.redis.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

@RequiredArgsConstructor
@Service
public class JwtProvider {
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  @Value("${spring.security.jwt.access-token.expiration}")
  private Long jwtExpirationInMillis;

  public String generateAccessToken(String subject, HashMap<String, Object> claims) {
    JwtClaimsSet.Builder claimSetBuilder = JwtClaimsSet.builder()
        .subject(subject)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusMillis(jwtExpirationInMillis));
    if (claims != null) {
      claims.forEach(claimSetBuilder::claim);
    }
    JwtClaimsSet claimsSet = claimSetBuilder.build();
    return this.jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
  }

  public String extractSidFromToken(String accessToken) {
    Jwt jwt = jwtDecoder.decode(accessToken);
    return jwt.getClaim("sid");
  }

  public Instant extractExpirationTime(String accessToken) {
    Jwt jwt = jwtDecoder.decode(accessToken);
    return jwt.getExpiresAt();
  }
  public Long getJwtExpirationInMillis() {
    return jwtExpirationInMillis;
  }
}