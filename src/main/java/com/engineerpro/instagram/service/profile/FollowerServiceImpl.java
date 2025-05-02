package com.engineerpro.instagram.service.profile;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.engineerpro.instagram.exception.InvalidInputException;
import com.engineerpro.instagram.repository.FollowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.profile.GetFollowerResponse;
import com.engineerpro.instagram.dto.profile.GetFollowingResponse;
import com.engineerpro.instagram.model.Profile;
import com.engineerpro.instagram.model.UserFollowing;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FollowerServiceImpl implements FollowerService {
  @Autowired
  private ProfileService profileService;
  @Autowired
  private FollowerRepository followerRepository;

  @Override
  public void folowUser(UserPrincipal userPrincipal, int profileId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    if (profile.getId() == profileId) {
      throw new InvalidInputException();
    }
    UserFollowing existedUserFollowing = followerRepository.findByFollowerUserIdAndFollowingUserId(profile.getId(),
        profileId);
    if (Objects.nonNull(existedUserFollowing)) {
      return;
    }

    UserFollowing userFollowing = new UserFollowing();
    userFollowing.setFollowerUserId(profile.getId());
    userFollowing.setFollowingUserId(profileId);
    userFollowing.setCreatedAt(new Date());
    followerRepository.save(userFollowing);
  }

  @Override
  public void unfolowUser(UserPrincipal userPrincipal, int profileId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    UserFollowing existedUserFollowing = followerRepository.findByFollowerUserIdAndFollowingUserId(profile.getId(),
        profileId);
    if (Objects.isNull(existedUserFollowing)) {
      return;
    }
    followerRepository.delete(existedUserFollowing);
  }

  @Override
  public GetFollowerResponse getFollowers(int profileId, int page, int limit) {
    profileService.getUserProfile(profileId);
    int totalFollower = followerRepository.countByFollowingUserId(profileId);
    log.info("totalFollower={}", totalFollower);
    int totalPage = (int) Math.ceil((double) totalFollower / limit);
    int offset = (page - 1) * limit;
    List<Profile> followerProfiles = followerRepository.findByFollowingUserId(profileId, limit, offset).stream()
        .map(userFollowing -> profileService.getUserProfile(userFollowing.getFollowerUserId())).toList();

    return GetFollowerResponse.builder()
        .totalPage(totalPage)
        .followers(followerProfiles)
        .build();
  }

  @Override
  public GetFollowingResponse getFollowings(int profileId, int page, int limit) {
    profileService.getUserProfile(profileId);
    int totalFollowing = followerRepository.countByFollowerUserId(profileId);
    log.info("totalFollowing={}", totalFollowing);
    int totalPage = (int) Math.ceil((double) totalFollowing / limit);
    int offset = (page - 1) * limit;
    List<Profile> followingProfiles = followerRepository.findByFollowerUserId(profileId, limit, offset).stream()
        .map(userFollowing -> profileService.getUserProfile(userFollowing.getFollowingUserId())).toList();

    return GetFollowingResponse.builder()
        .totalPage(totalPage)
        .followings(followingProfiles)
        .build();
  }

}