package com.ariv.photoshare.post.service;

import com.ariv.photoshare.post.dto.CreatePostRequest;
import com.ariv.photoshare.post.dto.CreatePostResponse;
import com.ariv.photoshare.post.dto.PostResponse;
import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class PostService {

    @Inject
    private PostRepository repository;

    @Transactional
    public CreatePostResponse create(
            CreatePostRequest request) {

        PostEntity post = new PostEntity();

        post.id = UUID.randomUUID();
        post.userId = request.userId();
        post.imageUrl = request.imageUrl();
        post.caption = request.caption();
        post.createdAt = Instant.now();

        repository.persist(post);

        return new CreatePostResponse(post.id);
    }

    public PostResponse get(UUID postId) {

        PostEntity post =
                repository.findById(postId);

        if (post == null) {
            throw new NotFoundException();
        }

        return new PostResponse(
                post.id,
                post.userId,
                post.imageUrl,
                post.caption,
                post.createdAt
        );
    }
}