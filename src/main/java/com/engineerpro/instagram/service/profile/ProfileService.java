package com.engineerpro.instagram.service.profile;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.profile.UpdateProfileImageRequest;
import com.engineerpro.instagram.dto.profile.UpdateProfileRequest;
import com.engineerpro.instagram.model.Profile;

public interface ProfileService {
  Profile getUserProfile(UserPrincipal userPrincipal);

  Profile getUserProfile(int id);

  Profile updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request);

  Profile updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request);
}