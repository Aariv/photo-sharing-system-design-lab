package com.ariv.photoshare.admin.service;

import com.ariv.photoshare.admin.dto.SeedRequest;
import com.ariv.photoshare.comment.entity.CommentEntity;
import com.ariv.photoshare.comment.repository.CommentRepository;
import com.ariv.photoshare.follow.entity.FollowEntity;
import com.ariv.photoshare.follow.repository.FollowRepository;
import com.ariv.photoshare.like.entity.LikeEntity;
import com.ariv.photoshare.like.repository.LikeRepository;
import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.user.entity.UserEntity;
import com.ariv.photoshare.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.*;

@ApplicationScoped
public class SeedDataService {

    @Inject
    UserRepository userRepository;

    @Inject
    PostRepository postRepository;

    @Inject
    FollowRepository followRepository;

    @Inject
    LikeRepository likeRepository;

    @Inject
    CommentRepository commentRepository;

    private final Random random =
            new Random();

    @Transactional
    public void seed(
            SeedRequest request) {

        List<UUID> users =
                createUsers(
                        request.users());

        List<UUID> posts =
                createPosts(
                        users,
                        request.posts());

        createFollowers(
                users,
                request.followers());

        createLikes(
                users,
                posts,
                request.likes());

        createComments(
                users,
                posts,
                request.comments());
    }

    private List<UUID> createUsers(
            int count) {

        List<UUID> userIds =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {

            UUID userId =
                    UUID.randomUUID();

            UserEntity user =
                    new UserEntity();

            user.id = userId;

            user.username =
                    "seed-user-" + i;

            user.email =
                    "seed" + i + "@test.com";

            user.passwordHash =
                    "password";

            user.createdAt =
                    Instant.now();

            userRepository.persist(user);

            userIds.add(userId);
        }

        return userIds;
    }

    private List<UUID> createPosts(
            List<UUID> users,
            int count) {

        List<UUID> postIds =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {

            UUID postId =
                    UUID.randomUUID();

            PostEntity post =
                    new PostEntity();

            post.id = postId;

            post.userId =
                    randomUser(users);

            post.imageUrl =
                    "uploads/post-" + i + ".jpg";

            post.caption =
                    "Synthetic post #" + i;

            post.createdAt =
                    Instant.now();

            postRepository.persist(post);

            postIds.add(postId);
        }

        return postIds;
    }

    private void createFollowers(
            List<UUID> users,
            int count) {

        int created = 0;

        while (created < count) {

            UUID follower =
                    randomUser(users);

            UUID following =
                    randomUser(users);

            if (follower.equals(following)) {
                continue;
            }

            if (followRepository.exists(follower, following)) {
                continue;
            }

            FollowEntity follow =
                    new FollowEntity();

            follow.id =
                    UUID.randomUUID();

            follow.followerId =
                    follower;

            follow.followingId =
                    following;

            follow.createdAt =
                    Instant.now();

            followRepository.persist(follow);

            created++;
        }
    }

    private void createLikes(
            List<UUID> users,
            List<UUID> posts,
            int count) {

        for (int i = 0; i < count; i++) {

            LikeEntity like =
                    new LikeEntity();

            like.id =
                    UUID.randomUUID();

            like.userId =
                    randomUser(users);

            like.postId =
                    randomPost(posts);

            if (likeRepository.exists(like.userId, like.postId)) {
                continue;
            }

            like.createdAt =
                    Instant.now();

            likeRepository.persist(
                    like);
        }
    }

    private void createComments(
            List<UUID> users,
            List<UUID> posts,
            int count) {

        for (int i = 0; i < count; i++) {

            CommentEntity comment =
                    new CommentEntity();

            comment.id =
                    UUID.randomUUID();

            comment.userId =
                    randomUser(users);

            comment.postId =
                    randomPost(posts);

            comment.comment =
                    "Synthetic comment #" + i;

            comment.createdAt =
                    Instant.now();

            commentRepository.persist(
                    comment);
        }
    }

    private UUID randomUser(
            List<UUID> users) {

        return users.get(
                random.nextInt(
                        users.size()));
    }

    private UUID randomPost(
            List<UUID> posts) {

        return posts.get(
                random.nextInt(
                        posts.size()));
    }
}
