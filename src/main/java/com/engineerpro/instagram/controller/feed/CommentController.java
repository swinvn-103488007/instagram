package com.engineerpro.instagram.controller.feed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.engineerpro.instagram.dto.UserPrincipal;
import com.engineerpro.instagram.dto.feed.CreateCommentRequest;
import com.engineerpro.instagram.dto.feed.GetPostResponse;
import com.engineerpro.instagram.model.Post;
import com.engineerpro.instagram.service.feed.CommentService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(path = "/comments")
public class CommentController {
  @Autowired
  CommentService commentService;

  @PostMapping()
  public ResponseEntity<GetPostResponse> createComment(
      @Valid @RequestBody CreateCommentRequest request, Authentication authentication) {
    log.info(String.format("authentication principal %s", authentication.getPrincipal().toString()));
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Post post = commentService.createComment(userPrincipal, request);
    return ResponseEntity.ok().body(GetPostResponse.builder().post(post).build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<GetPostResponse> deleteComment(@PathVariable int id, Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Post post = commentService.deleteComment(userPrincipal, id);
    return ResponseEntity.ok().body(GetPostResponse.builder().post(post).build());
  }
}
