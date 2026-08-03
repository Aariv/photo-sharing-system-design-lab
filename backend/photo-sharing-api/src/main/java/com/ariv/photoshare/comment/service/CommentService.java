package com.ariv.photoshare.comment.service;

import com.ariv.photoshare.cache.service.CacheService;
import com.ariv.photoshare.comment.dto.CommentResponse;
import com.ariv.photoshare.comment.dto.CommentsResponse;
import com.ariv.photoshare.comment.dto.CreateCommentRequest;
import com.ariv.photoshare.comment.entity.CommentEntity;
import com.ariv.photoshare.comment.repository.CommentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class CommentService {

    @Inject
    CommentRepository repository;

    @Inject
    CacheService cacheService;

    @Transactional
    public CommentResponse create(
            UUID postId,
            CreateCommentRequest request) {

        CommentEntity entity =
                new CommentEntity();

        entity.id = UUID.randomUUID();
        entity.postId = postId;
        entity.userId = request.userId();
        entity.comment = request.comment();
        entity.createdAt = Instant.now();

        repository.persist(entity);

        cacheService.evictFeed(request.userId());

        return new CommentResponse(
                entity.id,
                entity.userId,
                entity.postId,
                entity.comment,
                entity.createdAt
        );
    }

    public CommentsResponse getComments(
            UUID postId) {

        var items = repository
                .findByPost(postId)
                .stream()
                .map(comment ->
                        new CommentResponse(
                                comment.id,
                                comment.userId,
                                comment.postId,
                                comment.comment,
                                comment.createdAt
                        ))
                .toList();

        return new CommentsResponse(items);
    }

    public long count(UUID postId) {
        return repository.countComments(postId);
    }
}