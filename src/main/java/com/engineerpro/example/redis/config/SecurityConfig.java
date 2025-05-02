package com.engineerpro.example.redis.config;

import com.engineerpro.example.redis.dto.UserPrincipal;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${spring.security.public-key-path}")
  private RSAPublicKey publicKey;
  @Value("${spring.security.private-key-path}")
  private RSAPrivateKey privateKey;

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.warn("Configuring http filterChain");
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                "/swagger-ui/**",
                "/api-docs/**"
            ).permitAll()
            .requestMatchers("/auth/**").permitAll()
            .anyRequest().permitAll()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(
                jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
    JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }

  @Bean
  public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return new Converter<>() {
      @Override
      public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        String username = jwt.getClaimAsString("username");

        UserPrincipal userPrincipal = new UserPrincipal(
            userId,
            username,
            "", // password not needed for JWT
            authorities
        );

        // If you need to set additional attributes
        Map<String, Object> attributes = new HashMap<>(jwt.getClaims());
        userPrincipal.setAttributes(attributes);

        return new JwtAuthenticationToken(jwt, authorities, username) {
          @Override
          public Object getPrincipal() {
            return userPrincipal;
          }
        };
      }

      private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extract authorities from your JWT claims
        // This is an example - modify according to your JWT structure
        Collection<String> authorities = jwt.getClaimAsStringList("scope");
        if (authorities == null) {
          authorities = Collections.singletonList("ROLE_USER");
        }

        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
      }
    };
  }
}
