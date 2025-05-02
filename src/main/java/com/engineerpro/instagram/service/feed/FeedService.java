package com.engineerpro.instagram.service.feed;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.feed.GetFeedResponse;

public interface FeedService {
  GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page);
}
