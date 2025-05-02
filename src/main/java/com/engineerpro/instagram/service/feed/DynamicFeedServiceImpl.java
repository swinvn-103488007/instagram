package com.engineerpro.instagram.service.feed;

import java.util.List;

import com.engineerpro.instagram.repository.FollowerRepository;
import com.engineerpro.instagram.repository.PostRepository;
import com.engineerpro.instagram.service.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.feed.GetFeedResponse;
import com.engineerpro.instagram.model.Post;
import com.engineerpro.instagram.model.Profile;
import com.engineerpro.instagram.model.UserFollowing;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("dynamicFeedService")
public class DynamicFeedServiceImpl implements FeedService {
  @Autowired
  private ProfileService profileService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private FollowerRepository followerRepository;

  @Override
  public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
    Profile profile = profileService.getUserProfile(userPrincipal);

    List<UserFollowing> followings = followerRepository.findByFollowerUserId(profile.getId());
    List<Integer> followingProfileIdList = followings.stream().map(following -> following.getFollowingUserId())
        .toList();
    log.info("followingProfileIdList={}", followingProfileIdList);
    int totalPost = postRepository.countByCreatedByIn(followingProfileIdList);
    log.info("totalPost={}", totalPost);
    int totalPage = (int) Math.ceil((double) totalPost / limit);
    int offset = (page - 1) * limit;

    List<Post> posts = postRepository
        .findByCreatedBy(followingProfileIdList, limit, offset);

    return GetFeedResponse.builder()
        .posts(posts).totalPage(totalPage).build();
  }

}
