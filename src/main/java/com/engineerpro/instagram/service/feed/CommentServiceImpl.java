package com.engineerpro.instagram.service.feed;

import java.util.Date;

import com.engineerpro.instagram.exception.CommentNotFoundException;
import com.engineerpro.instagram.exception.NoPermissionException;
import com.engineerpro.instagram.exception.PostNotFoundException;
import com.engineerpro.instagram.repository.CommentRepository;
import com.engineerpro.instagram.repository.PostRepository;
import com.engineerpro.instagram.service.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.feed.CreateCommentRequest;
import com.engineerpro.instagram.model.Comment;
import com.engineerpro.instagram.model.Post;
import com.engineerpro.instagram.model.Profile;

@Service
public class CommentServiceImpl implements CommentService {
  @Autowired
  private ProfileService profileService;


  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Override
  public Post createComment(UserPrincipal userPrincipal, CreateCommentRequest request) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);
    Comment comment = new Comment();
    comment.setComment(request.getComment());
    comment.setCreatedAt(new Date());
    comment.setCreatedBy(profile);
    comment.setPost(post);
    commentRepository.save(comment);
    return post;
  }

  @Override
  public Post deleteComment(UserPrincipal userPrincipal, int commentId) {
    Profile profile = profileService.getUserProfile(userPrincipal);
    Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
    if (comment.getCreatedBy().getId() != profile.getId()) {
      throw new NoPermissionException();
    }
    commentRepository.delete(comment);
    return comment.getPost();
  }

}
