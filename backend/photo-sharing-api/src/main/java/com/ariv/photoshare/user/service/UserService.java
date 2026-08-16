package com.ariv.photoshare.user.service;

import com.ariv.photoshare.common.exception.UserAlreadyExistsException;
import com.ariv.photoshare.follow.repository.FollowRepository;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.user.dto.ProfileResponse;
import com.ariv.photoshare.user.dto.SignupRequest;
import com.ariv.photoshare.user.dto.SignupResponse;
import com.ariv.photoshare.user.entity.UserEntity;
import com.ariv.photoshare.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    PostRepository postRepository;

    @Inject
    FollowRepository followRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        UserEntity user = new UserEntity();

        user.id = UUID.randomUUID();
        user.username = request.username();
        user.email = request.email();

        // plain text for phase-01
        // we'll add bcrypt later
        user.passwordHash = request.password();

        user.createdAt = Instant.now();

        if(userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(request.username());
        }

        userRepository.persist(user);

        return new SignupResponse(
                user.id,
                user.username,
                user.email
        );
    }

    public ProfileResponse profile(
            UUID userId) {

        UserEntity user =
                userRepository.findById(userId);

        if (user == null) {
            throw new NotFoundException(
                    "User not found");
        }

        long postsCount =
                postRepository.countPosts(
                        userId);

        long followersCount =
                followRepository.countFollowers(
                        userId);

        long followingCount =
                followRepository.countFollowing(
                        userId);

        return new ProfileResponse(

                user.id,

                user.username,

                user.email,

                postsCount,

                followersCount,

                followingCount
        );
    }
}