package com.engineerpro.instagram.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginFailResponse {
  private String message;
}
