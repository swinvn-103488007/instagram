package com.engineerpro.instagram.event;

import java.util.List;

import com.engineerpro.instagram.model.Post;
import com.engineerpro.instagram.model.UserFollowing;
import com.engineerpro.instagram.repository.FollowerRepository;
import com.engineerpro.instagram.repository.NotificationRepository;
import com.engineerpro.instagram.service.feed.PostService;
import com.engineerpro.instagram.service.profile.ProfileService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.engineerpro.instagram.config.MessageQueueConfig;
import com.engineerpro.instagram.repository.FeedRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RabbitListener(queues = MessageQueueConfig.AFTER_CREATE_POST_QUEUE)
public class PushFeedConsumer {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProfileService profileService;

    @Autowired
    PostService postService;

    @Autowired
    FollowerRepository followerRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FeedRepository feedRepository;

    @RabbitHandler
    public void receive(Integer postId) throws JsonMappingException, JsonProcessingException {
        log.info(" [x] Received '" + postId + "'");

        Post post = postService.getPost(postId);

        List<UserFollowing> follwerList = followerRepository.findByFollowingUserId(post.getCreatedBy().getId());

        for (UserFollowing userFollowing : follwerList) {
            log.info("userFollowing={}", userFollowing);
            feedRepository.addPostToFeed(post.getId(), userFollowing.getFollowerUserId());
        }
    }
}
