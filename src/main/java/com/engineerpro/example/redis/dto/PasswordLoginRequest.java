package com.engineerpro.example.redis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordLoginRequest {
  private String userEmail;
  private String password;
}
