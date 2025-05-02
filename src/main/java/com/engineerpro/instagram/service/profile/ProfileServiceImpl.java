package com.engineerpro.instagram.service.profile;

import com.engineerpro.instagram.exception.UserNotFoundException;
import com.engineerpro.instagram.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.profile.UpdateProfileImageRequest;
import com.engineerpro.instagram.dto.profile.UpdateProfileRequest;
import com.engineerpro.instagram.model.Profile;
import com.engineerpro.instagram.service.UploadService;

@Service
public class ProfileServiceImpl implements ProfileService {
  @Autowired
  private UploadService uploadService;
  @Autowired
  private ProfileRepository profileRepository;

  @Override
  public Profile getUserProfile(UserPrincipal userPrincipal) {
    Profile profile = profileRepository.findOneByUserId(userPrincipal.getId().toString());
//    if (Objects.isNull(profile)) {
//      profile = new Profile();
//      profile.setUserId(userPrincipal.getId().toString());
//      profile.setDisplayName(userPrincipal.getName());
//      profileRepository.save(profile);
//    }
    return profile;
  }

  @Override
  public Profile getUserProfile(int id) {
    return profileRepository.findById(id).orElseThrow(UserNotFoundException::new);
  }

  @Override
  public Profile updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
    Profile profile = this.getUserProfile(userPrincipal);
    profile.setBio(request.getBio());
    profile.setDisplayName(request.getDisplayName());
    profile.setUsername(request.getUsername());
    profileRepository.save(profile);
    return profile;
  }

  @Override
  public Profile updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request) {
    String url = uploadService.uploadImage(request.getBase64ImageString());
    Profile profile = this.getUserProfile(userPrincipal);
    profile.setProfileImageUrl(url);
    profileRepository.save(profile);
    return profile;
  }
}