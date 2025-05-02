package com.engineerpro.instagram.dto.profile;

import com.engineerpro.instagram.model.Profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UpdateProfileResponse {
  private Profile profile;
}
