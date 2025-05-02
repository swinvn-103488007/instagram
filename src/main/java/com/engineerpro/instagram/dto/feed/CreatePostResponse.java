package com.engineerpro.instagram.dto.feed;

import com.engineerpro.instagram.model.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreatePostResponse {
  private Post post;
}
