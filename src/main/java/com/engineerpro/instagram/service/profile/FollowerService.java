package com.engineerpro.instagram.service.profile;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.profile.GetFollowerResponse;
import com.engineerpro.instagram.dto.profile.GetFollowingResponse;

public interface FollowerService {
  void folowUser(UserPrincipal userPrincipal, int profileId);

  void unfolowUser(UserPrincipal userPrincipal, int profileId);

  GetFollowerResponse getFollowers(int profileId, int page, int limit);

  GetFollowingResponse getFollowings(int profileId, int page, int limit);

}