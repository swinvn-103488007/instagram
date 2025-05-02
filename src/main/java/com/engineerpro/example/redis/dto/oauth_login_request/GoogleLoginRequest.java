package com.engineerpro.example.redis.dto.oauth_login_request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GoogleLoginRequest extends Oauth2LoginRequest {
  private String email;
  private String name;
  private String picture;
}
