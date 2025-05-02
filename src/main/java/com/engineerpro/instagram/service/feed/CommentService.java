package com.engineerpro.instagram.service.feed;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.feed.CreateCommentRequest;
import com.engineerpro.instagram.model.Post;

public interface CommentService {
  Post createComment(UserPrincipal userPrincipal, CreateCommentRequest request);

  Post deleteComment(UserPrincipal userPrincipal, int commentId);
}
