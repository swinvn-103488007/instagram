package com.engineerpro.instagram.service.feed;

import java.util.List;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.feed.CreatePostRequest;
import com.engineerpro.instagram.model.Post;

public interface PostService {
  Post createPost(UserPrincipal userPrincipal, CreatePostRequest request);

  Post getPost(int postId);

  void deletePost(UserPrincipal userPrincipal, int postId);

  Post likePost(UserPrincipal userPrincipal, int postId);

  Post unlikePost(UserPrincipal userPrincipal, int postId);

  List<Post> getUserPosts(int userId);
}
