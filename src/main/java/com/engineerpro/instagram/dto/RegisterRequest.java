package com.engineerpro.instagram.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "email must not be empty")
    private String email;
    @NotBlank(message = "email must not be empty")
    private String displayName;
    @NotBlank(message = "email must not be empty")
    private String password;
}
