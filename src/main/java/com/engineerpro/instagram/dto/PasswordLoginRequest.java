package com.engineerpro.instagram.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordLoginRequest {
  private String userEmail;
  private String password;
}
