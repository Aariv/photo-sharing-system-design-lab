package com.ariv.photoshare.like.repository;

import com.ariv.photoshare.like.entity.LikeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class LikeRepository
        implements PanacheRepositoryBase<LikeEntity, UUID> {

    public LikeEntity findLike(
            UUID userId,
            UUID postId) {

        return find(
                "userId = ?1 and postId = ?2",
                userId,
                postId)
                .firstResult();
    }

    public long countLikes(UUID postId) {

        return count(
                "postId",
                postId
        );
    }

    public boolean exists(
            UUID userId,
            UUID postId) {

        return count(
                "userId = ?1 and postId = ?2",
                userId,
                postId
        ) > 0;
    }

    public boolean likedByUser(
        UUID userId,
        UUID postId)
    {
                return count(
                        "userId = ?1 and postId = ?2",
                        userId,
                        postId
                ) > 0;
    }
}